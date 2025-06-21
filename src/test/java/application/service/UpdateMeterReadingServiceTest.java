package application.service;

import org.example.application.service.UpdateMeterReadingService;
import org.example.domain.application.MeterReadingRepository;
import org.example.domain.model.MeterReading;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

class UpdateMeterReadingServiceTest {

    static class FakeRepo implements MeterReadingRepository {
        int calledApartment = -1;
        String calledField  = null;
        double calledValue  = -1;
        Optional<MeterReading> prev = Optional.empty();

        @Override
        public void updateField(int apartmentNumber, String field, double value) {
            this.calledApartment = apartmentNumber;
            this.calledField     = field;
            this.calledValue     = value;
        }

        @Override public void save(MeterReading r) {}
        @Override public Optional<MeterReading> findPrevious(int apartmentNumber) {
            return prev;
        }
        @Override public void resetMonthly() {}
        @Override public boolean hasUnsubmittedReadings(long userId, int apartmentNumber) {
            return false;
        }
    }

    @Test
    void shouldDelegateUpdateToRepo() {
        FakeRepo repo = new FakeRepo();
        repo.prev = Optional.of(
                MeterReading.of(LocalDate.now(), 105, 0, 0, 0, 0, 0)
        );

        UpdateMeterReadingService service = new UpdateMeterReadingService(repo);

        service.update(105, "curr_hotWater", 12.5);

        assertEquals(105, repo.calledApartment);
        assertEquals("curr_hotWater", repo.calledField);
        assertEquals(12.5, repo.calledValue, 0.0001);
    }
}