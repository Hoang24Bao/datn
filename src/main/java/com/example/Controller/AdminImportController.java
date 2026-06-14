package com.example.Controller;

import com.example.Service.ExcelImportService;
import com.example.Service.ExcelImportService.ImportResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/import")
public class AdminImportController {

    @Autowired
    private ExcelImportService excelImportService;

    @PostMapping("/vocab")
    public ResponseEntity<?> importVocabFromExcel(
            @RequestParam("excelFile") MultipartFile excelFile,
            @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles,
            @RequestParam(value = "audioFiles", required = false) MultipartFile[] audioFiles) {

        try {
            if (excelFile == null || excelFile.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File Excel không được để trống!"));
            }

            String fileName = excelFile.getOriginalFilename();
            if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
                return ResponseEntity.badRequest().body(Map.of("error", "Chỉ hỗ trợ file Excel (.xlsx, .xls)"));
            }

            ImportResult result = excelImportService.importFromExcel(excelFile, imageFiles, audioFiles);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.getSuccess());
            response.put("fail", result.getFail());
            response.put("total", result.getTotal());
            response.put("errors", result.getErrors());

            if (result.getSuccess() > 0) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi import: " + e.getMessage()));
        }
    }
}