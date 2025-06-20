package org.example.domain.application;


public interface UpdateMeterReading {
    void update(int apartmentNumber, String meterType, double newValue);
}