package org.example.domain.application;


public interface UpdateMeterReadingUseCase {
    void update(int apartmentNumber, String meterType, double newValue);
}