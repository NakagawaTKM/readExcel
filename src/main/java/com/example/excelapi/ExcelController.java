package com.example.excelapi;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
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

            Sheet sheet = workbook.getSheetAt(0);

            // 指定されたセルと出力項目名のマッピング
            Map<String, String> cellMap = new HashMap<>();
            cellMap.put("ご記入年月日", "AE11");
            cellMap.put("取引を開始する弊社拠点", "G12");
            cellMap.put("会社名（ふりがな）", "G14");
            cellMap.put("会社名", "G15");
            cellMap.put("住所（ふりがな）", "G16");
            cellMap.put("住所", "G17");
            cellMap.put("HP", "G18");
            cellMap.put("役員1", "G20");
            cellMap.put("役員2", "Q20");
            cellMap.put("役員3", "AE20");
            cellMap.put("事業内容", "G21");
            cellMap.put("設立年(西暦)", "Z21");
            cellMap.put("法人番号", "AJ21");

            for (Map.Entry<String, String> entry : cellMap.entrySet()) {
                String label = entry.getKey();
                String cellRef = entry.getValue();
                String value = getCellValue(sheet, cellRef);
                response.put(label, value);
            }

            System.out.println("response normal:" + response);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "read Excel file fail: " + e.getMessage());
            System.out.println("response error:" + response);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

    }

    // セルの取得ヘルパー関数
    String getCellValue(Sheet sheet, String cellRef) {
        CellReference ref = new CellReference(cellRef);
        Row row = sheet.getRow(ref.getRow());
        if (row != null) {
            Cell cell = row.getCell(ref.getCol());
            if (cell != null) {
                switch (cell.getCellType()) {
                    case STRING:
                        return cell.getStringCellValue();
                    case NUMERIC:
                        return String.valueOf(cell.getNumericCellValue());
                    case BOOLEAN:
                        return String.valueOf(cell.getBooleanCellValue());
                    default:
                        return "";
                }
            }
        }
        return "";
    }
}