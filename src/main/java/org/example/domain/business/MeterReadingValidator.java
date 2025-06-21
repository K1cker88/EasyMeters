package org.example.domain.business;

import java.time.LocalDate;

public class MeterReadingValidator {
    private static final int LAST_DAY = 22;

    public void validateDate(LocalDate date) {
        if (date.getDayOfMonth() > LAST_DAY) {
            throw new IllegalStateException(
                    "Передача показаний возможна только до 22-го числа.");
        }
    }
}