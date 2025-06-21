package application.service;

import org.example.application.service.RegisterAccountHolderService;
import org.example.domain.application.AccountHolderRepository;
import org.example.domain.model.AccountHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RegisterAccountHolderServiceTest {

    private RegisterAccountHolderService service;
    private InMemoryAccountHolderRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryAccountHolderRepository();
        service = new RegisterAccountHolderService(repo);
    }

    @Test
    void shouldRegisterNewAccountHolder() {
        long userId = 123L;
        String aptInput = "10";
        String accInput = "20";

        AccountHolder result = service.register(aptInput, accInput, userId);

        assertNotNull(result);
        assertEquals(10, result.getApartmentNumber());
        assertEquals(20, result.getAccountNumber());
        assertEquals(userId, result.getUserId());

        assertTrue(repo.saved.contains(result));
    }

    @Test
    void shouldThrowWhenAlreadyRegistered() {
        long userId = 42L;

        repo.preRegister(userId, 5);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.register("5", "100", userId)
        );
        assertEquals("Вы уже зарегистрированы.", ex.getMessage());
    }

    @Test
    void shouldThrowOnInvalidNumberFormat() {
        long userId = 1L;
        assertThrows(IllegalArgumentException.class,
                () -> service.register("abc", "100", userId),
                "Некорректный формат номера квартиры");
        assertThrows(IllegalArgumentException.class,
                () -> service.register("10", "zzz", userId),
                "Некорректный формат номера лицевого счета");
    }

    /**
     * Простейшая in-memory реализация репозитория
     * без использования Mockito.
     */
    static class InMemoryAccountHolderRepository implements AccountHolderRepository {
        // хранит сохранённые объекты
        final java.util.List<AccountHolder> saved = new java.util.ArrayList<>();
        // отмеченные пары (userId, apartment)
        private final java.util.Set<String> registered = new java.util.HashSet<>();

        @Override
        public void save(AccountHolder h) {
            saved.add(h);
            registered.add(key(h.getUserId(), h.getApartmentNumber()));
        }

        @Override
        public boolean existsByUserIdAndApartmentNumber(long telegramUserId, int apt) {
            return registered.contains(key(telegramUserId, apt));
        }

        // позволяет тесту заранее зарегистрировать пользователя
        void preRegister(long userId, int apt) {
            registered.add(key(userId, apt));
        }

        private String key(long userId, int apt) {
            return userId + "#" + apt;
        }
    }
}