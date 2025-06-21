package org.example.domain.application;

import org.example.domain.model.MeterReading;

import java.util.Optional;

public interface MeterReadingRepository {
    void save(MeterReading reading);
    Optional<MeterReading> findPrevious(int apartmentNumber);
    void updateField(int apartmentNumber, String field, double value);
    void resetMonthly();
    Optional<MeterReading> createMeterReadingFromPrev(int apartmentNumber);
    void updateHotWater(int apartmentNumber, double value);
    void updateColdWater(int apartmentNumber, double value);
    void updateHeating(int apartmentNumber, double value);
    void updateElectricityDay(int apartmentNumber, double value);
    void updateElectricityNight(int apartmentNumber, double value);
    boolean hasUnsubmittedReadings(long userId, int apartmentNumber);
}
