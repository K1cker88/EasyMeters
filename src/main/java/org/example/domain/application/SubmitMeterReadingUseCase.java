package org.example.domain.application;

import org.example.domain.model.MeterReading;

public interface SubmitMeterReadingUseCase {
    MeterReading submit(int apartmentNumber,
                        double hotWater,
                        double coldWater,
                        double heating,
                        double dayElect,
                        double nightElect);
}