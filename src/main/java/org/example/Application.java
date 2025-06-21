package org.example;

import org.example.application.repository.JdbcMeterReadingRepository;
import org.example.application.service.MonthlyReportEmailService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);

        //ctx.getBean(JdbcMeterReadingRepository.class).updatePrevReadings();

        //ctx.getBean(MonthlyReportEmailService.class).sendMonthlyReport();
    }
}