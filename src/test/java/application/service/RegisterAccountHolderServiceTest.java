package application.service;

import org.example.application.service.RegisterAccountHolderService;
import org.example.domain.application.AccountHolderRepository;
import org.example.domain.model.AccountHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
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
        long userId   = 123L;
        String apt    = "10";
        String acc    = "43010010010";

        AccountHolder result = service.register(apt, acc, userId);

        assertNotNull(result);
        assertEquals(10,   result.getApartmentNumber());
        assertEquals(43010010010L, result.getAccountNumber());
        assertEquals(userId, result.getUserId());
        assertTrue(repo.saved.contains(result));
    }

    @Test
    void shouldThrowWhenAlreadyRegistered() {
        long   userId = 42L;
        int    apt    = 5;
        String acc    = "43010010005";
        repo.preRegister(userId, apt);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.register(String.valueOf(apt), acc, userId)
        );
        assertEquals("Вы уже зарегистрированы.", ex.getMessage());
    }

    @Test
    void shouldThrowOnInvalidNumberFormat() {
        long userId = 1L;

        IllegalArgumentException ex1 = assertThrows(
                IllegalArgumentException.class,
                () -> service.register("abc", "43010010001", userId)
        );
        assertTrue(ex1.getMessage().toLowerCase().contains("формат"));

        IllegalArgumentException ex2 = assertThrows(
                IllegalArgumentException.class,
                () -> service.register("10", "zzz", userId)
        );
        assertTrue(ex2.getMessage().toLowerCase().contains("формат"));
    }

    static class InMemoryAccountHolderRepository implements AccountHolderRepository {
        final List<AccountHolder> saved      = new ArrayList<>();
        final Set<String>         registered = new HashSet<>();

        @Override
        public void save(AccountHolder h) {
            saved.add(h);
            registered.add(key(h.getUserId(), h.getApartmentNumber()));
        }

        @Override
        public boolean existsByUserIdAndApartmentNumber(long telegramUserId, int apt) {
            return registered.contains(key(telegramUserId, apt));
        }

        void preRegister(long userId, int apt) {
            registered.add(key(userId, apt));
        }

        private String key(long userId, int apt) {
            return userId + "#" + apt;
        }
    }
}