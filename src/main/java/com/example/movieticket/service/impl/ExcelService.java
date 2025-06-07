package com.example.movieticket.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class ExcelService {
    private final String filePath = "src/main/resources/faqs.xlsx";;

    public Map<String, String> loadFAQs() {
        Map<String, String> faqMap = new HashMap<>();
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                Cell q = row.getCell(0);
                Cell a = row.getCell(1);
                faqMap.put(q.getStringCellValue().toLowerCase(), a.getStringCellValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return faqMap;
    }

}
