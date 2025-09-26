package com.example.excelapi;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ExcelController {

    @CrossOrigin(origins = "*")
    @PostMapping(value = "/read-cell", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> readExcelCells(@RequestParam("file") MultipartFile file) {
        Map<String, String> response = new HashMap<>();

        try (InputStream inputStream = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(inputStream)) {

            //　部署１
            



            //　部署２


            Sheet sheet = workbook.getSheetAt(0);
            // B2~B8 -> row 1~7（0-based），row B is cell 1（0-based）
            for (int i = 1; i <= 7; i++) {
                Row row = sheet.getRow(i);
                String cellValue = "";
                if (row != null) {
                    Cell cell = row.getCell(1);
                    if (cell != null) {
                        switch (cell.getCellType()) {
                            case STRING:
                                cellValue = cell.getStringCellValue();
                                break;
                            case NUMERIC:
                                cellValue = String.valueOf(cell.getNumericCellValue());
                                break;
                            case BOOLEAN:
                                cellValue = String.valueOf(cell.getBooleanCellValue());
                                break;
                            default:
                                cellValue = "";
                        }
                    }
                }
                response.put("B" + (i + 1), cellValue);
            }
            System.out.println("response normal:"+response);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "read Excel file fail: " + e.getMessage());
            System.out.println("response error:"+response);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

    }

}
