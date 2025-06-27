package application.service;

import org.example.application.service.SubmitMeterReadingService;
import org.example.domain.application.MeterReadingRepositoryPort;
import org.example.domain.model.MeterReading;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class SubmitMeterReadingServiceTest {

    // Фейковая реализация репозитория
    static class InMemoryMeterRepo implements MeterReadingRepositoryPort {
        MeterReading saved;
        Optional<MeterReading> prevReading = Optional.empty();

        @Override
        public void save(MeterReading reading) {
            this.saved = reading;
        }

        @Override
        public Optional<MeterReading> findPrevious(int apartmentNumber) {
            return prevReading;
        }

        @Override public void updateField(int apartmentNumber, String field, double value) {}
        @Override public void resetMonthly() {}

        @Override
        public Optional<MeterReading> createMeterReadingFromPrev(int apartmentNumber) {
            return Optional.empty();
        }

        @Override
        public void updateHotWater(int apartmentNumber, double value) {

        }

        @Override
        public void updateColdWater(int apartmentNumber, double value) {

        }

        @Override
        public void updateHeating(int apartmentNumber, double value) {

        }

        @Override
        public void updateElectricityDay(int apartmentNumber, double value) {

        }

        @Override
        public void updateElectricityNight(int apartmentNumber, double value) {

        }

        @Override
        public boolean hasUnsubmittedReadings(long userId) {
            return false;
        }

        // Для удобства можно добавить сеттер
        public void setPrevious(MeterReading reading) {
            this.prevReading = Optional.of(reading);
        }
    }

    @Test
    void savesMeterReadingSuccessfully() {
        InMemoryMeterRepo repo = new InMemoryMeterRepo();
        SubmitMeterReadingService service = new SubmitMeterReadingService(repo);

        LocalDate today = LocalDate.now();
        int apt = 101;

        MeterReading result = service.submit(apt, 1.1, 2.2, 3.3, 4.4, 5.5);

        assertNotNull(result);
        assertEquals(apt, result.getApartmentNumber());
        assertEquals(1.1, result.getHotWater());
        assertEquals(5.5, result.getElectricityNight());
        assertEquals(result, repo.saved, "Сохранённое показание должно совпадать с возвращённым");
    }

    @Test
    void usesZeroReadingIfPreviousMissing() {
        InMemoryMeterRepo repo = new InMemoryMeterRepo(); // prevReading по умолчанию пустой
        SubmitMeterReadingService service = new SubmitMeterReadingService(repo);

        MeterReading result = service.submit(10, 1, 1, 1, 1, 1);

        assertEquals(10, result.getApartmentNumber());
        assertEquals(1, result.getHotWater());
    }
}