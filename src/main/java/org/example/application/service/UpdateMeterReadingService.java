package org.example.application.service;

import org.example.domain.application.MeterReadingRepository;
import org.example.domain.application.UpdateMeterReading;
import org.springframework.stereotype.Service;


@Service
public class UpdateMeterReadingService
        implements UpdateMeterReading {

    private final MeterReadingRepository repo;

    public UpdateMeterReadingService(MeterReadingRepository repo) {
        this.repo = repo;
    }

    @Override
    public void update(int apartmentNumber, String meterType, double newValue) {
       repo.updateField(apartmentNumber, meterType, newValue);
    }
}