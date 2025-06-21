package org.example.application.bot;

import org.example.domain.application.RegisterAccountHolderUseCase;
import org.example.domain.application.SubmitMeterReadingUseCase;
import org.example.domain.application.UpdateMeterReadingUseCase;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MeterReadingBot extends TelegramLongPollingBot {

    private final RegisterAccountHolderUseCase registerUC;
    private final SubmitMeterReadingUseCase  submitUC;
    private final UpdateMeterReadingUseCase  updateUC;

    @Value("${telegram.bot.username}") private String botUsername;
    @Value("${telegram.bot.token}")     private String botToken;

    private final Map<Long, UserState> userState = new ConcurrentHashMap<>();

    @Autowired
    public MeterReadingBot(RegisterAccountHolderUseCase registerUC,
                           SubmitMeterReadingUseCase  submitUC,
                           UpdateMeterReadingUseCase  updateUC) {
        this.registerUC = registerUC;
        this.submitUC   = submitUC;
        this.updateUC   = updateUC;
    }

    @Override
    public void onUpdateReceived(Update upd) {
        if (upd.hasMessage() && upd.getMessage().hasText()) {
            Long chatId = upd.getMessage().getChatId();
            String txt  = upd.getMessage().getText().trim();
            if (txt.startsWith("/start")) {
                sendMainMenu(chatId);
                userState.remove(chatId);
            } else if (userState.containsKey(chatId)) {
                handleText(userState.get(chatId), chatId, txt);
            } else {
                sendMessage(chatId, "Нажмите /start");
            }
        } else if (upd.hasCallbackQuery()) {
            handleCallback(upd.getCallbackQuery());
        }
    }

    private void handleCallback(CallbackQuery cq) {
        Long chatId = cq.getMessage().getChatId();
        String data = cq.getData();

        switch (data) {
            case "register" ->
                    startRegistration(chatId, cq.getFrom().getId());
            case "submit_readings" ->
                    startProcess(chatId, "submit", "Введите номер квартиры:");
            case "update_readings" ->
                    startProcess(chatId, "update", "Введите номер квартиры:");
            default -> {
                if (userState.containsKey(chatId)) {
                    handleText(userState.get(chatId), chatId, data);
                }
            }
        }
    }

    private void startRegistration(Long chatId, long tgId) {
        UserState st = new UserState();
        st.proc = "reg"; st.step = 0; st.telegramUserId = tgId;
        userState.put(chatId, st);
        sendMessage(chatId, "Введите номер квартиры для регистрации:");
    }

    private void startProcess(Long chatId, String type, String prompt) {
        if (LocalDate.now().getDayOfMonth() > 22) {
            sendMessage(chatId, "Передача показаний возможна только до 22 числа.");
            return;
        }
        UserState st = new UserState();
        st.proc = type; st.step = 0;
        userState.put(chatId, st);
        sendMessage(chatId, prompt);
    }

    private void handleText(UserState st, Long chatId, String txt) {
        try {
            switch (st.proc) {
                case "reg"    -> onReg(st, chatId, txt);
                case "submit" -> onSubmit(st, chatId, txt);
                case "update" -> onUpdate(st, chatId, txt);
            }
        } catch (Exception ex) {
            sendMessage(chatId, "Ошибка: " + ex.getMessage());
            userState.remove(chatId);
            sendMainMenu(chatId);
        }
    }

    // — Registration flow
    private void onReg(UserState st, Long chatId, String txt) {
        if (st.step == 0) {
            st.apartmentInput = txt;
            st.step = 1;
            sendMessage(chatId, "Введите номер лицевого счета:");
        } else {
            registerUC.register(st.apartmentInput, txt, st.telegramUserId);
            sendMessage(chatId, "✅ Регистрация успешна.");
            userState.remove(chatId);
            sendMainMenu(chatId);
        }
    }

    // — Submit flow
    private void onSubmit(UserState st, Long chatId, String txt) {
        switch (st.step) {
            case 0 -> {
                st.apt = Integer.parseInt(txt);
                st.step = 1;
                sendMessage(chatId, "Горячая вода (тек.):");
            }
            case 1 -> {
                st.hw = Double.parseDouble(txt);
                st.step = 2;
                sendMessage(chatId, "Холодная вода:");
            }
            case 2 -> {
                st.cw = Double.parseDouble(txt);
                st.step = 3;
                sendMessage(chatId, "Теплоэнергия:");
            }
            case 3 -> {
                st.ht = Double.parseDouble(txt);
                st.step = 4;
                sendMessage(chatId, "Электричество день:");
            }
            case 4 -> {
                st.ed = Double.parseDouble(txt);
                st.step = 5;
                sendMessage(chatId, "Электричество ночь:");
            }
            case 5 -> {
                st.en = Double.parseDouble(txt);
                submitUC.submit(st.apt, st.hw, st.cw, st.ht, st.ed, st.en);
                sendMessage(chatId, "✅ Показания сохранены.");
                userState.remove(chatId);
                sendMainMenu(chatId);
            }
        }
    }

    // — Update flow
    private void onUpdate(UserState st, Long chatId, String txt) {
        if (st.step == 0) {
            st.apt   = Integer.parseInt(txt);
            st.step  = 1;
            // можно тут вызвать отдельную клавиатуру:
            sendUpdateMeterTypeKeyboard(chatId);
        }
        else if (st.step == 1) {
            st.field = switch (txt) {
                case "🔥 горячая вода"       -> "curr_hotWater";
                case "💧 холодная вода"      -> "curr_coldWater";
                case "♨ теплоэнергия"        -> "curr_heating";
                case "💡 эл-во день"         -> "curr_electricityDay";
                case "🔌 эл-во ночь"         -> "curr_electricityNight";
                default -> throw new IllegalArgumentException("Неизвестный тип счётчика: " + txt);
            };
            st.step  = 2;
            sendMessage(chatId, "Введите новое значение:");
        }
        else {
            double v = Double.parseDouble(txt);
            updateUC.update(st.apt, st.field, v);
            sendMessage(chatId, "✅ Значение обновлено.");
            userState.remove(chatId);
            sendMainMenu(chatId);
        }
    }

    // — Inline клавиатура для главного меню
    private void sendMainMenu(Long chatId) {
        InlineKeyboardMarkup kb = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = List.of(
                List.of(button("📝 Регистрация", "register")),
                List.of(
                        button("🏠 Передать показания", "submit_readings"),
                        button("✏️ Изменить показания", "update_readings")
                )
        );
        kb.setKeyboard(rows);

        SendMessage msg = SendMessage.builder()
                .chatId(chatId.toString())
                .text("Выберите действие:")
                .replyMarkup(kb)
                .build();
        executeSafely(msg);
    }

    // — Inline клавиатура для выбора типа счётчика
    private void sendUpdateMeterTypeKeyboard(Long chatId) {
        InlineKeyboardMarkup kb = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (String label : List.of(
                "🔥 горячая вода", "💧 холодная вода",
                "♨ теплоэнергия", "💡 эл-во день", "🔌 эл-во ночь")) {
            rows.add(List.of(button(label, label)));
        }
        kb.setKeyboard(rows);

        SendMessage msg = SendMessage.builder()
                .chatId(chatId.toString())
                .text("Выберите показание для обновления:")
                .replyMarkup(kb)
                .build();
        executeSafely(msg);
    }

    private InlineKeyboardButton button(String text, String data) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(data)
                .build();
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage msg = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .build();
        executeSafely(msg);
    }

    private void executeSafely(SendMessage msg) {
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override public String getBotUsername() { return botUsername; }
    @Override public String getBotToken()    { return botToken; }

    private static class UserState {
        String proc;
        int step;
        int apt;
        double hw, cw, ht, ed, en;
        String apartmentInput;
        long telegramUserId;
        String field;
    }
}