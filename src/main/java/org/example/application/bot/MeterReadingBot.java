package org.example.application.bot;

import org.example.domain.application.*;
import org.example.domain.business.MeterReadingValidator;
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
    private final RegisterAccountHolder registerUC;
    private final SubmitMeterReading submitUC;
    private final UpdateMeterReading updateUC;
    private final AccountHolderRepository accountHolderRepository;
    private final MeterReadingRepository meterReadingRepository;
    private final MeterReadingValidator dateValidator = new MeterReadingValidator();

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    private final Map<Long, UserState> userStateMap = new ConcurrentHashMap<>();

    @Autowired
    public MeterReadingBot(
            RegisterAccountHolder registerUC,
            SubmitMeterReading submitUC,
            UpdateMeterReading updateUC,
            AccountHolderRepository accountHolderRepository,
            MeterReadingRepository meterReadingRepository
    ) {
        this.registerUC = registerUC;
        this.submitUC = submitUC;
        this.updateUC = updateUC;
        this.accountHolderRepository = accountHolderRepository;
        this.meterReadingRepository = meterReadingRepository;
    }

    @Override
    public void onUpdateReceived(Update upd) {
        if (upd.hasMessage() && upd.getMessage().hasText()) {
            Long chatId = upd.getMessage().getChatId();
            String txt = upd.getMessage().getText().trim();

            if (txt.startsWith("/start")) {
                sendMainMenu(chatId);
                userStateMap.remove(chatId);
                return;
            }

            if (userStateMap.containsKey(chatId)) {
                UserState st = userStateMap.get(chatId);
                switch (st.proc) {
                    case "reg"    -> onReg(st, chatId, txt);
                    case "submit" -> onSubmit(st, chatId, txt);
                    case "update" -> handleUpdate(st, chatId, txt);
                }
            } else {
                sendMessage(chatId, "Нажмите /start");
            }

        } else if (upd.hasCallbackQuery()) {
            handleCallback(upd.getCallbackQuery());
        }
    }

    private void handleCallback(CallbackQuery cq) {
        Long chatId = cq.getMessage().getChatId();
        long tgId    = cq.getFrom().getId();
        String data  = cq.getData();

        if ("submit_readings_disabled".equals(data)) {
            sendMessage(chatId, "Вы уже передали показания за этот месяц. " +
                    "Изменить их можно через кнопку «Изменить показания».");
            return;
        }

        switch (data) {
            case "register" ->
                    startRegistration(chatId, tgId);

            case "submit_readings" ->
                    startProcess(chatId, tgId, "submit", "Введите номер квартиры:");

            case "update_readings" ->
                    startProcess(chatId, tgId, "update", "Введите номер квартиры:");

            default -> {
                if (userStateMap.containsKey(chatId)) {
                    UserState st = userStateMap.get(chatId);
                    if ("update".equals(st.proc)) {
                        handleUpdate(st, chatId, data);
                    } else {
                        onReg(st, chatId, data);
                    }
                }
            }
        }
    }

    private void startProcess(Long chatId,
                              long telegramUserId,
                              String proc,
                              String prompt)
    {
        try {
            dateValidator.validateDate(LocalDate.now());
        } catch (IllegalStateException e) {
            sendMessage(chatId, e.getMessage());
            return;
        }

        UserState st = new UserState();
        st.proc = proc;
        st.step = 0;
        st.telegramUserId = telegramUserId;
        userStateMap.put(chatId, st);

        sendMessage(chatId, prompt);
    }

    private void startRegistration(Long chatId, long telegramUserId) {
        UserState st = new UserState();
        st.proc = "reg";
        st.step = 0;
        st.telegramUserId = telegramUserId;
        userStateMap.put(chatId, st);
        sendMessage(chatId, "Введите номер квартиры для регистрации:");
    }

    private void onReg(UserState st, Long chatId, String txt) {
        if (st.step == 0) {
            st.apartmentInput = txt;
            st.step = 1;
            sendMessage(chatId, "Введите номер лицевого счёта:");
        } else {
            try {
                registerUC.register(st.apartmentInput, txt, st.telegramUserId);
                sendMessage(chatId, "✅ Регистрация прошла успешно.");
            } catch (IllegalStateException ex) {
                sendMessage(chatId, ex.getMessage());
            } catch (Exception ex) {
                sendMessage(chatId, "❌ Не удалось завершить регистрацию. Попробуйте позже.");
                ex.printStackTrace();
            } finally {
                userStateMap.remove(chatId);
                sendMainMenu(chatId);
            }
        }
    }

    private void onSubmit(UserState st, Long chatId, String txt) {
        try {
            switch (st.step) {
                case 0 -> {
                    st.apt = Integer.parseInt(txt);
                    if (!accountHolderRepository.existsByUserIdAndApartmentNumber(st.telegramUserId, st.apt)) {
                        sendMessage(chatId, "❌ Вы не зарегистрированы за этой квартирой.");
                        userStateMap.remove(chatId);
                        sendMainMenu(chatId);
                        return;
                    }
                    if (meterReadingRepository.hasUnsubmittedReadings(st.telegramUserId)) {
                        sendMessage(chatId,
                                "ℹ️ Вы уже передали показания за этот месяц.\n" +
                                        "Изменить можно через «Изменить показания».");
                        userStateMap.remove(chatId);
                        sendMainMenu(chatId);
                        return;
                    }
                    st.step = 1;
                    sendMessage(chatId, "Горячая вода:");
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
                    userStateMap.remove(chatId);
                    sendMainMenu(chatId);
                }
            }
        } catch (NumberFormatException ex) {
            sendMessage(chatId, "Неверный формат числа. Попробуйте ещё раз.");
        }
    }

    private void handleUpdate(UserState st, Long chatId, String txt) {
        try {
            switch (st.step) {
                case 0 -> {
                    st.apartmentNumber = Integer.parseInt(txt);
                    if (!accountHolderRepository
                            .existsByUserIdAndApartmentNumber(st.telegramUserId, st.apartmentNumber))
                    {
                        sendMessage(chatId,
                                "❌ Вы не зарегистрированы за данной квартирой. Пройдите регистрацию.");
                        userStateMap.remove(chatId);
                        sendMainMenu(chatId);
                        return;
                    }
                    meterReadingRepository
                            .createMeterReadingFromPrev(st.apartmentNumber)
                            .ifPresentOrElse(prev -> {
                                st.hotWater       = prev.getHotWater();
                                st.coldWater      = prev.getColdWater();
                                st.heating        = prev.getHeating();
                                st.electricityDay   = prev.getElectricityDay();
                                st.electricityNight = prev.getElectricityNight();
                            }, () -> {
                                st.hotWater = st.coldWater = st.heating
                                        = st.electricityDay = st.electricityNight = 0.0;
                            });
                    st.step = 1;
                    sendUpdateMeterTypeKeyboard(chatId);
                }
                case 1 -> {
                    if (!txt.startsWith("update_meter:")) {
                        sendMessage(chatId, "Выберите тип показания кнопкой:");
                        sendUpdateMeterTypeKeyboard(chatId);
                        return;
                    }
                    st.readingType = txt.substring("update_meter:".length());
                    st.previousReading = switch (st.readingType) {
                        case "🔥горячая вода" -> st.hotWater;
                        case "💧холодная вода" -> st.coldWater;
                        case "♨теплоэнергия" -> st.heating;
                        case "💡электричество день" -> st.electricityDay;
                        case "🔌электричество ночь" -> st.electricityNight;
                        default -> 0.0;
                    };
                    st.step = 2;
                    sendMessage(chatId,
                            String.format("Предыдущее значение %s: %.2f\nВведите новое показание:",
                                    st.readingType, st.previousReading));
                }
                case 2 -> {
                    double v = Double.parseDouble(txt);
                    if (v < st.previousReading) {
                        sendMessage(chatId,
                                "Новое значение не может быть меньше предыдущего (" +
                                        st.previousReading + ")");
                        return;
                    }
                    switch (st.readingType) {
                        case "🔥горячая вода"        -> meterReadingRepository.updateHotWater(st.apartmentNumber, v);
                        case "💧холодная вода"       -> meterReadingRepository.updateColdWater(st.apartmentNumber, v);
                        case "♨теплоэнергия"         -> meterReadingRepository.updateHeating(st.apartmentNumber, v);
                        case "💡электричество день"  -> meterReadingRepository.updateElectricityDay(st.apartmentNumber, v);
                        case "🔌электричество ночь" -> meterReadingRepository.updateElectricityNight(st.apartmentNumber, v);
                    }
                    sendMessage(chatId, "✅ Показание «" + st.readingType + "» обновлено.");
                    st.step = 1;
                    sendUpdateMeterTypeKeyboard(chatId);
                }
            }
        } catch (NumberFormatException ex) {
            sendMessage(chatId, "Неверный формат числа. Попробуйте ещё раз.");
        }
    }

    private void sendMainMenu(Long chatId) {
        long userId = chatId; // в приватном чате ChatId == TelegramUserId
        boolean already = meterReadingRepository.hasUnsubmittedReadings(userId);

        InlineKeyboardMarkup kb = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> line1 = List.of(button("📝 Регистрация", "register"));
        InlineKeyboardButton submitBtn = already
                ? button("🏠 Передать показания", "submit_readings_disabled")
                : button("🏠 Передать показания", "submit_readings");
        InlineKeyboardButton updateBtn = button("✏️ Изменить показания", "update_readings");

        kb.setKeyboard(List.of(
                List.of(button("📝 Регистрация", "register")),
                List.of(
                        button("🏠 Передать показания", "submit_readings"),
                        button("✏️ Изменить показания",  "update_readings")
                )
        ));

        executeSafely(SendMessage.builder()
                .chatId(chatId.toString())
                .text("Выберите действие:")
                .replyMarkup(kb)
                .build()
        );
    }

    private void sendUpdateMeterTypeKeyboard(Long chatId) {
        InlineKeyboardMarkup kb = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (String label : List.of(
                "🔥горячая вода",
                "💧холодная вода",
                "♨теплоэнергия",
                "💡электричество день",
                "🔌электричество ночь"
        )) {
            rows.add(List.of(button(label, "update_meter:" + label)));
        }
        kb.setKeyboard(rows);

        executeSafely(SendMessage.builder()
                .chatId(chatId.toString())
                .text("Выберите тип показания:")
                .replyMarkup(kb)
                .build()
        );
    }

    private InlineKeyboardButton button(String text, String data) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(data)
                .build();
    }

    private void sendMessage(Long chatId, String text) {
        executeSafely(SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .build()
        );
    }

    private void executeSafely(SendMessage msg) {
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    private static class UserState {
        String proc;
        int    step;
        long   telegramUserId;
        // для регистрации
        String apartmentInput;
        // для submit
        int    apt;
        double hw, cw, ht, ed, en;
        // для update
        int    apartmentNumber;
        String readingType;
        double hotWater, coldWater, heating, electricityDay, electricityNight;
        double previousReading;
    }
}