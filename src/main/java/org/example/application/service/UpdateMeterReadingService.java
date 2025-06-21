package org.example.application.service;

import org.example.domain.application.MeterReadingRepository;
import org.example.domain.application.UpdateMeterReading;
import org.example.domain.business.MeterReadingValidator;
import org.example.domain.model.MeterReading;
import org.springframework.stereotype.Service;


@Service
public class UpdateMeterReadingService implements UpdateMeterReading {

    private final MeterReadingRepository repo;
    private final MeterReadingValidator validator = new MeterReadingValidator();

    public UpdateMeterReadingService(MeterReadingRepository repo) {
        this.repo = repo;
    }

    @Override
    public void update(int apartmentNumber, String field, double newValue) {
        MeterReading prev = repo.findPrevious(apartmentNumber)
                .orElseThrow(() -> new IllegalStateException("Нет предыдущих показаний"));

        double oldValue = switch (field) {
            case "curr_hotWater"        -> prev.getHotWater();
            case "curr_coldWater"       -> prev.getColdWater();
            case "curr_heating"         -> prev.getHeating();
            case "curr_electricityDay"  -> prev.getElectricityDay();
            case "curr_electricityNight"-> prev.getElectricityNight();
            default -> throw new IllegalArgumentException("Неизвестный тип: " + field);
        };

        if (newValue < oldValue) {
            throw new IllegalArgumentException(
                    "Новое значение не может быть меньше предыдущего (" + oldValue + ")");
        }

        repo.updateField(apartmentNumber, field, newValue);
    }
}