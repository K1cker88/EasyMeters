package org.example.application.repository;

import org.example.domain.application.AccountHolderRepository;
import org.example.domain.model.AccountHolder;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;


@Repository
public class JdbcAccountHolderRepository
        implements AccountHolderRepository {

    private final JdbcTemplate jdbc;

    public JdbcAccountHolderRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(AccountHolder h) {
        String sql = "INSERT INTO users (Id, apartmentNumber, accountNumber) VALUES (?,?,?)";
        try {
            jdbc.update(sql, h.getUserId(), h.getApartmentNumber(), h.getAccountNumber());
            System.out.println("✅ Данные успешно сохранены!");
        }
        catch (DuplicateKeyException ex) {
            // пользователь с таким userId или apartmentNumber уже есть
            throw new IllegalStateException(
                    "Вы уже зарегистрированы", ex);
        }
        catch (DataAccessException ex) {
            System.err.println("❌ Ошибка сохранения: " + ex.getMessage());
            throw ex;
        }
    }

    public boolean existsByUserIdAndApartmentNumber(long userId, int apartmentNumber) {
        String sql = "SELECT COUNT(*) FROM users WHERE Id = ? AND apartmentNumber = ?";
        Integer cnt = jdbc.queryForObject(sql, Integer.class, userId, apartmentNumber);
        return cnt != null && cnt > 0;
    }
}