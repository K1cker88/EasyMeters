package org.example.application.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataAccessException;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ExcelExportService {

    private final JdbcTemplate jdbc;

    @Autowired
    public ExcelExportService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void exportExcelReport(String filePath) {
        String sql = """
            SELECT u.apartmentNumber,
                   u.accountNumber,
                   m.curr_hotWater,
                   m.curr_coldWater,
                   m.curr_heating,
                   m.curr_electricityDay,
                   m.curr_electricityNight
            FROM easymeter.users u
            JOIN easymeter.meters m
              ON u.apartmentNumber = m.apartmentNumber
            """;

        List<Map<String,Object>> rows;
        try {
            rows = jdbc.queryForList(sql);
        } catch (DataAccessException ex) {
            System.err.println("Ошибка выборки: " + ex.getMessage());
            return;
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Отчет");
            CellStyle style = workbook.createCellStyle();
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);

            String[] headers = {
                    "№ кв.","№ сч.","Хвода","Гвода","Тепло","Эдень","Эночь"
            };
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(style);
            }

            int r = 1;
            for (var rowData : rows) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue((Integer) rowData.get("apartmentNumber"));
                row.createCell(1).setCellValue((Integer) rowData.get("accountNumber"));
                row.createCell(2).setCellValue((Double)  rowData.get("curr_coldWater"));
                row.createCell(3).setCellValue((Double)  rowData.get("curr_hotWater"));
                row.createCell(4).setCellValue((Double)  rowData.get("curr_heating"));
                row.createCell(5).setCellValue((Double)  rowData.get("curr_electricityDay"));
                row.createCell(6).setCellValue((Double)  rowData.get("curr_electricityNight"));
                for (int i = 0; i < 7; i++) {
                    row.getCell(i).setCellStyle(style);
                }
            }
            for (int i = 0; i < 7; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
                System.out.println("✅ Excel сформирован: " + filePath);
            }
        } catch (IOException ex) {
            System.err.println("Ошибка Excel: " + ex.getMessage());
        }
    }
}