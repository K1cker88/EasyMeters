package application.service;

import org.example.application.service.UpdateMeterReadingService;
import org.example.domain.application.MeterReadingRepository;
import org.example.domain.model.MeterReading;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UpdateMeterReadingServiceTest {

    static class FakeRepo implements MeterReadingRepository {
        int calledApartment = -1;
        String calledField = null;
        double calledValue = -1;

        @Override
        public void updateField(int apartmentNumber, String field, double value) {
            this.calledApartment = apartmentNumber;
            this.calledField = field;
            this.calledValue = value;
        }

        // неиспользуемые методы — можно оставить пустыми
        @Override public void save(MeterReading r) {}
        @Override public Optional<MeterReading> findPrevious(int apartmentNumber) { return Optional.empty(); }
        @Override public void resetMonthly() {}
        @Override public Optional<MeterReading> createMeterReadingFromPrev(int apartmentNumber) { return Optional.empty(); }
        @Override public void updateHotWater(int apartmentNumber, double value) {}
        @Override public void updateColdWater(int apartmentNumber, double value) {}
        @Override public void updateHeating(int apartmentNumber, double value) {}
        @Override public void updateElectricityDay(int apartmentNumber, double value) {}
        @Override public void updateElectricityNight(int apartmentNumber, double value) {}

        @Override
        public boolean hasUnsubmittedReadings(long userId) {
            return false;
        }
    }

    @Test
    void shouldDelegateUpdateToRepo() {
        FakeRepo repo = new FakeRepo();
        UpdateMeterReadingService service = new UpdateMeterReadingService(repo);

        service.update(105, "curr_hotWater", 12.5);

        assertEquals(105, repo.calledApartment);
        assertEquals("curr_hotWater", repo.calledField);
        assertEquals(12.5, repo.calledValue);
    }
}