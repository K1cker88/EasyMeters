package org.example.application.service;

import org.example.domain.application.MeterReadingRepositoryPort;
import org.example.domain.application.UpdateMeterReadingUseCase;
import org.springframework.stereotype.Service;


@Service
public class UpdateMeterReadingService
        implements UpdateMeterReadingUseCase {

    private final MeterReadingRepositoryPort repo;

    public UpdateMeterReadingService(MeterReadingRepositoryPort repo) {
        this.repo = repo;
    }

    @Override
    public void update(int apartmentNumber,
                       String meterType,
                       double newValue) {
        // meterType должен совпадать с именем столбца, например "curr_hotWater"
        repo.updateField(apartmentNumber, meterType, newValue);
    }
}