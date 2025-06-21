package application.service;

import org.example.application.service.SubmitMeterReadingService;
import org.example.domain.application.MeterReadingRepository;
import org.example.domain.model.MeterReading;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SubmitMeterReadingServiceTest {

    static class InMemoryMeterRepo implements MeterReadingRepository {
        MeterReading saved;
        Optional<MeterReading> prev = Optional.empty();

        @Override
        public void save(MeterReading reading) {
            this.saved = reading;
        }

        @Override
        public Optional<MeterReading> findPrevious(int apartmentNumber) {
            return prev;
        }

        @Override
        public void updateField(int apartmentNumber, String field, double value) {
            // not used in this test
        }

        @Override
        public void resetMonthly() {
            // not used
        }

        @Override
        public boolean hasUnsubmittedReadings(long userId, int apartmentNumber) {
            return false;
        }

        void setPrevious(MeterReading reading) {
            this.prev = Optional.of(reading);
        }
    }

    private InMemoryMeterRepo repo;
    private SubmitMeterReadingService service;

    @BeforeEach
    void setUp() {
        repo = new InMemoryMeterRepo();
        service = new SubmitMeterReadingService(repo);
    }

    @Test
    void savesMeterReadingSuccessfully() {
        int apt      = 101;
        double hot   = 1.1;
        double cold  = 2.2;
        double heat  = 3.3;
        double day   = 4.4;
        double night = 5.5;


        MeterReading result = service.submit(apt, hot, cold, heat, day, night);

        assertNotNull(result);
        assertEquals(apt,     result.getApartmentNumber());
        assertEquals(hot,     result.getHotWater());
        assertEquals(cold,    result.getColdWater());
        assertEquals(heat,    result.getHeating());
        assertEquals(day,     result.getElectricityDay());
        assertEquals(night,   result.getElectricityNight());
        assertSame(result, repo.saved,
                "Сохранённый объект должен совпадать с возвращённым");
    }
}