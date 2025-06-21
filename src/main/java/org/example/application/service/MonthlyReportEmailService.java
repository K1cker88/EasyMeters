package org.example.application.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.mail.javamail.MimeMessageHelper;

@Service
public class MonthlyReportEmailService {

    private final ExcelExportService excelExportService;
    private final JavaMailSender mailSender;
    private final String recipientEmail;
    private final String address = "Ленина 66";

    @Autowired
    public MonthlyReportEmailService(ExcelExportService excelExportService,JavaMailSender mailSender,@Value("${report.email.recipient}") String recipientEmail) {
        this.excelExportService = excelExportService;
        this.mailSender = mailSender;
        this.recipientEmail = recipientEmail;
    }

    public void sendMonthlyReport() {
        String filePath = generateReportFileName();
        try {
            excelExportService.exportExcelReport(filePath);
        } catch (Exception e) {
            System.err.println("Ошибка генерации отчёта: " + e.getMessage());
            return;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("Файл не найден: " + filePath);
            return;
        }

        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true);

            helper.setTo(recipientEmail);
            helper.setSubject("Ежемесячный отчет по счетчикам " + address);
            helper.setText("Добрый день. \nВо вложении ежемесячный отчет с показаниями приборов учета для дома по адресу " + address, false);

            String attachmentName = file.getName(); // e.g. "meter_report_2025-06.xlsx"
            helper.addAttachment(attachmentName, new FileSystemResource(file));

            mailSender.send(msg);
            System.out.println("Отчет отправлен: " + attachmentName);
        } catch (Exception ex) {
            System.err.println("Ошибка отправки email: " + ex.getMessage());
        }
    }

    private String generateReportFileName() {
        String base = System.getProperty("user.dir") + "/reports";
        new File(base).mkdirs();
        String date = new SimpleDateFormat("yyyy-MM").format(new Date());
        return base + "/meter_report_" + date + ".xlsx";
    }
}