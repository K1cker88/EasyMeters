package org.example.application.service;

import org.example.domain.application.MeterReadingRepository;
import org.example.domain.application.SubmitMeterReading;
import org.example.domain.business.MeterReadingValidator;
import org.example.domain.model.MeterReading;
import org.springframework.stereotype.Service;


import java.time.LocalDate;

@Service
public class SubmitMeterReadingService
        implements SubmitMeterReading {

    private final MeterReadingRepository repo;
    private final MeterReadingValidator validator = new MeterReadingValidator();

    public SubmitMeterReadingService(MeterReadingRepository repo) {
        this.repo = repo;
    }

    @Override
    public MeterReading submit(int apartmentNumber, double hotWater, double coldWater, double heating, double dayElect, double nightElect) {
        LocalDate today = LocalDate.now();
        validator.validateDate(today);
        MeterReading prev = repo.findPrevious(apartmentNumber)
                .orElse(MeterReading.of(today, apartmentNumber, 0,0,0,0,0));
        MeterReading current = MeterReading.of(today, apartmentNumber, hotWater, coldWater, heating, dayElect, nightElect);
        repo.save(current);
        return current;
    }
}