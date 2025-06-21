package org.example.application.repository;

import org.example.domain.application.MeterReadingRepositoryPort;
import org.example.domain.model.MeterReading;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;


import java.time.LocalDate;
import java.util.Optional;

@Repository
public class JdbcMeterReadingRepository
        implements MeterReadingRepositoryPort {

    private final JdbcTemplate jdbc;

    public JdbcMeterReadingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(MeterReading r) {
        String sql = "INSERT INTO meters "
                + "(apartmentNumber, curr_hotWater, curr_coldWater, curr_heating, "
                + "curr_electricityDay, curr_electricityNight) VALUES (?,?,?,?,?,?)";
        try {
            jdbc.update(sql,
                    r.getApartmentNumber(),
                    r.getHotWater(),
                    r.getColdWater(),
                    r.getHeating(),
                    r.getElectricityDay(),
                    r.getElectricityNight()
            );
            System.out.println("✅ Показания сохранены");
        } catch (DuplicateKeyException ex) {
            System.err.println("❌ Дубликат записи");
        } catch (DataAccessException ex) {
            System.err.println("❌ Ошибка: " + ex.getMessage());
            throw ex;
        }
    }

    @Override
    public Optional<MeterReading> findPrevious(int apartmentNumber) {
        String sql = """
        SELECT prev_hotWater, prev_coldWater, prev_heating,
               prev_electricityDay, prev_electricityNight
        FROM meters
        WHERE apartmentNumber = ?
    """;

        try {
            MeterReading mr = jdbc.queryForObject(
                    sql,
                    new Object[]{apartmentNumber},
                    (rs, rowNum) -> {
                        double prevHot   = rs.getDouble("prev_hotWater");
                        double prevCold  = rs.getDouble("prev_coldWater");
                        double prevHeat  = rs.getDouble("prev_heating");
                        double prevDay   = rs.getDouble("prev_electricityDay");
                        double prevNight = rs.getDouble("prev_electricityNight");

                        // Передаем сначала дату, затем apartmentNumber, потом все счётчики
                        return MeterReading.of(
                                LocalDate.now(),
                                apartmentNumber,
                                prevHot,
                                prevCold,
                                prevHeat,
                                prevDay,
                                prevNight
                        );
                    }
            );
            return Optional.of(mr);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void updateField(int apt, String field, double v) {
        String sql = "UPDATE meters SET " + field + " = ? WHERE apartmentNumber = ?";
        jdbc.update(sql, v, apt);
    }

    @Override
    public void resetMonthly() {
        String updateSql =
                "UPDATE meters SET "
                        + "prev_hotWater = curr_hotWater, prev_coldWater = curr_coldWater, "
                        + "prev_heating = curr_heating, "
                        + "prev_electricityDay = curr_electricityDay, "
                        + "prev_electricityNight = curr_electricityNight, "
                        + "curr_hotWater = 0, curr_coldWater = 0, curr_heating = 0, "
                        + "curr_electricityDay = 0, curr_electricityNight = 0";
        jdbc.update(updateSql);
    }

    @Scheduled(cron = "0 0 0 1 * ?")
    public void scheduledUpdatePrevReadings() {
        resetMonthly();
    }
}