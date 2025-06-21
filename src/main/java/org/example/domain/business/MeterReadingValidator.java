package org.example.domain.business;

import org.example.domain.model.MeterReading;

import java.time.LocalDate;

public class MeterReadingValidator {
    private static final int LAST_DAY = 22;

    public void validateDate(LocalDate date) {
        if (date.getDayOfMonth() > LAST_DAY) {
            throw new IllegalStateException(
                    "Передача показаний возможна только до 22-го числа.");
        }
    }
    public void validateReadings(MeterReading prev,
                                 double hotWater,
                                 double coldWater,
                                 double heating,
                                 double electricityDay,
                                 double electricityNight) {
        if (hotWater < prev.getHotWater()
                || coldWater < prev.getColdWater()
                || heating < prev.getHeating()
                || electricityDay < prev.getElectricityDay()
                || electricityNight < prev.getElectricityNight()) {
            throw new IllegalArgumentException(
                    "Новые показания не могут быть меньше предыдущих.");
        }
    }
}
