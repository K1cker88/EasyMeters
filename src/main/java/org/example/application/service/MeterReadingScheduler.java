package org.example.application.service;

import org.example.domain.application.MeterReadingRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MeterReadingScheduler {

    private final MonthlyReportEmailService emailService;
    private final MeterReadingRepositoryPort meterRepo;

    @Autowired
    public MeterReadingScheduler(MonthlyReportEmailService emailService,
                                 MeterReadingRepositoryPort meterRepo) {
        this.emailService = emailService;
        this.meterRepo    = meterRepo;
    }

    /** Ежемесячная рассылка отчёта 10:00 23-го числа */
    @Scheduled(cron = "0 0 10 23 * ?")
    public void scheduleMonthlyReport() {
        emailService.sendMonthlyReport();
    }

    /** Сброс и перенос prev_* , curr_* в начале каждого месяца */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void scheduleMonthlyReset() {
        meterRepo.resetMonthly();
    }
}