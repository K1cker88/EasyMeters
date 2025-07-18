package org.example.domain.application;

import org.example.domain.model.MeterReading;

import java.util.Optional;

public interface MeterReadingRepository {
    void save(MeterReading reading);
    Optional<MeterReading> findPrevious(int apartmentNumber);
    void updateField(int apartmentNumber, String field, double value);
    void resetMonthly();
    boolean hasUnsubmittedReadings(long userId, int apartmentNumber);
}
