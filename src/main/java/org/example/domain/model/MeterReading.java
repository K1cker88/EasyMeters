package org.example.domain.model;

import java.time.LocalDate;
import java.util.Objects;

public class MeterReading {
    private final LocalDate date;
    private final int apartmentNumber;
    private final double hotWater;
    private final double coldWater;
    private final double heating;
    private final double electricityDay;
    private final double electricityNight;

    private MeterReading(LocalDate date,
                         int apartmentNumber,
                         double hotWater,
                         double coldWater,
                         double heating,
                         double electricityDay,
                         double electricityNight) {
        this.date               = date;
        this.apartmentNumber    = apartmentNumber;
        this.hotWater           = hotWater;
        this.coldWater          = coldWater;
        this.heating            = heating;
        this.electricityDay     = electricityDay;
        this.electricityNight   = electricityNight;
    }

    /** Создать новое чтение с сегодняшней датой */
    public static MeterReading of(LocalDate date,
                                  int apt,
                                  double hw, double cw,
                                  double ht, double ed, double en) {
        return new MeterReading(date, apt, hw, cw, ht, ed, en);
    }

    public LocalDate getDate()           { return date; }
    public int getApartmentNumber()      { return apartmentNumber; }
    public double getHotWater()          { return hotWater; }
    public double getColdWater()         { return coldWater; }
    public double getHeating()           { return heating; }
    public double getElectricityDay()    { return electricityDay; }
    public double getElectricityNight()  { return electricityNight; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MeterReading)) return false;
        MeterReading m = (MeterReading) o;
        return apartmentNumber == m.apartmentNumber &&
                Double.compare(m.hotWater, hotWater) == 0 &&
                Double.compare(m.coldWater, coldWater) == 0 &&
                Double.compare(m.heating, heating) == 0 &&
                Double.compare(m.electricityDay, electricityDay) == 0 &&
                Double.compare(m.electricityNight, electricityNight) == 0 &&
                date.equals(m.date);
    }
    @Override public int hashCode() {
        return Objects.hash(date, apartmentNumber,
                hotWater, coldWater, heating,
                electricityDay, electricityNight);
    }
}