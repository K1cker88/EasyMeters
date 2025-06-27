package application.service;

import org.example.application.service.RegisterAccountHolderService;
import org.example.domain.application.AccountHolderRepositoryPort;
import org.example.domain.model.AccountHolder;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class RegisterAccountHolderServiceTest {
    public class RegisterAccountHolderServiceSimpleTest {

        static class InMemoryRepo implements AccountHolderRepositoryPort {
            List<AccountHolder> holders = new ArrayList<>();

            @Override
            public void save(AccountHolder holder) {
                holders.add(holder);
            }

            @Override
            public boolean existsByUserIdAndApartmentNumber(long userId, int apt) {
                return holders.stream()
                        .anyMatch(h -> h.getUserId() == userId && h.getApartmentNumber() == apt);
            }
        }

        @Test
        void registersNewAccountHolderIfNotAlreadyRegistered() {
            var repo = new InMemoryRepo();
            var service = new RegisterAccountHolderService(repo);

            var holder = service.register("10", "20", 123L);

            assertEquals(10, holder.getApartmentNumber());
            assertEquals(20, holder.getAccountNumber());
            assertEquals(123L, holder.getUserId());
        }

        @Test
        void throwsExceptionIfUserIsAlreadyRegistered() {
            var repo = new InMemoryRepo();
            var service = new RegisterAccountHolderService(repo);

            repo.save(new AccountHolder(10, 20, 123L));

            var ex = assertThrows(IllegalStateException.class, () ->
                    service.register("10", "20", 123L)
            );

            assertEquals("Вы уже зарегистрированы для этой квартиры.", ex.getMessage());
        }
    }