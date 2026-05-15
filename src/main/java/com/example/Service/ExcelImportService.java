package com.example.Service;

import com.example.Dto.Response.ExcelImportDTO;
import com.example.Entity.Vocabulary;
import com.example.Repository.VocabularyRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExcelImportService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelImportService.class);

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private VocabularyRepository vocabularyRepository;

    /**
     * Đọc file Excel và trả về danh sách DTO
     */
    public List<ExcelImportDTO> readExcelFile(MultipartFile file) throws Exception {
        List<ExcelImportDTO> vocabList = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            boolean isFirstRow = true;

            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                // Kiểm tra dòng trống
                Cell firstCell = row.getCell(0);
                if (firstCell == null || getCellValueAsString(firstCell).trim().isEmpty()) {
                    continue;
                }

                ExcelImportDTO dto = new ExcelImportDTO();
                dto.setExpression(getCellValueAsString(row.getCell(0)));
                dto.setKana(getCellValueAsString(row.getCell(1)));
                dto.setRomaji(getCellValueAsString(row.getCell(2)));
                dto.setMeaning(getCellValueAsString(row.getCell(3)));
                dto.setImageFileName(getCellValueAsString(row.getCell(4)));
                dto.setAudioFileName(getCellValueAsString(row.getCell(5)));
                dto.setWordType(getCellValueAsString(row.getCell(6)));
                dto.setExample(getCellValueAsString(row.getCell(7)));
                dto.setExampleVi(getCellValueAsString(row.getCell(8)));

                if (!dto.getExpression().isEmpty()) {
                    vocabList.add(dto);
                }
            }
        }

        return vocabList;
    }

    /**
     * Import từ Excel, upload ảnh & audio lên Cloudinary
     */
    @Transactional
    public ImportResult importFromExcel(MultipartFile excelFile,
                                        MultipartFile[] imageFiles,
                                        MultipartFile[] audioFiles) {
        ImportResult result = new ImportResult();
        List<String> errors = new ArrayList<>();

        try {
            // 1. Đọc file Excel
            List<ExcelImportDTO> vocabList = readExcelFile(excelFile);
            result.setTotal(vocabList.size());

            // 2. Tạo map để tra nhanh file theo tên
            Map<String, MultipartFile> imageMap = new HashMap<>();
            Map<String, MultipartFile> audioMap = new HashMap<>();

            if (imageFiles != null) {
                for (MultipartFile img : imageFiles) {
                    String fileName = img.getOriginalFilename();
                    if (fileName != null) {
                        // Loại bỏ đường dẫn nếu có (chỉ lấy tên file)
                        String simpleName = fileName.substring(fileName.lastIndexOf("/") + 1);
                        imageMap.put(simpleName, img);
                        imageMap.put(fileName, img); // cũng lưu cả đường dẫn đầy đủ
                    }
                }
            }

            if (audioFiles != null) {
                for (MultipartFile aud : audioFiles) {
                    String fileName = aud.getOriginalFilename();
                    if (fileName != null) {
                        String simpleName = fileName.substring(fileName.lastIndexOf("/") + 1);
                        audioMap.put(simpleName, aud);
                        audioMap.put(fileName, aud);
                    }
                }
            }

            // 3. Xử lý từng từ vựng
            int index = 0;
            for (ExcelImportDTO dto : vocabList) {
                index++;
                try {
                    // Kiểm tra trùng lặp (nếu đã tồn tại thì bỏ qua hoặc update tùy ý)
                    if (vocabularyRepository.existsByExpression(dto.getExpression())) {
                        logger.warn("Từ vựng đã tồn tại: {}", dto.getExpression());
                        errors.add("Dòng " + index + ": Từ vựng '" + dto.getExpression() + "' đã tồn tại trong database");
                        result.incrementFail();
                        continue;
                    }

                    String imageUrl = null;
                    String audioUrl = null;

                    // Upload ảnh lên Cloudinary nếu có file
                    if (dto.getImageFileName() != null && !dto.getImageFileName().isEmpty()) {
                        // Lấy tên file đơn giản (không đường dẫn)
                        String imageSimpleName = dto.getImageFileName();
                        if (imageSimpleName.contains("/")) {
                            imageSimpleName = imageSimpleName.substring(imageSimpleName.lastIndexOf("/") + 1);
                        }

                        MultipartFile imageFile = imageMap.get(imageSimpleName);
                        if (imageFile == null) {
                            // Thử tìm với tên gốc
                            imageFile = imageMap.get(dto.getImageFileName());
                        }

                        if (imageFile != null) {
                            try {
                                imageUrl = cloudinaryService.uploadVocabImage(imageFile, dto.getRomaji());
                                logger.info("Upload ảnh thành công: {} -> {}", dto.getImageFileName(), imageUrl);
                            } catch (Exception e) {
                                logger.error("Upload ảnh thất bại: {}", dto.getImageFileName(), e);
                                errors.add("Dòng " + index + " - ảnh '" + dto.getImageFileName() + "': " + e.getMessage());
                            }
                        } else {
                            logger.warn("Không tìm thấy file ảnh: {}", dto.getImageFileName());
                            errors.add("Dòng " + index + " - Không tìm thấy file ảnh: " + dto.getImageFileName());
                        }
                    }

                    // Upload audio lên Cloudinary nếu có file
                    if (dto.getAudioFileName() != null && !dto.getAudioFileName().isEmpty()) {
                        String audioSimpleName = dto.getAudioFileName();
                        if (audioSimpleName.contains("/")) {
                            audioSimpleName = audioSimpleName.substring(audioSimpleName.lastIndexOf("/") + 1);
                        }

                        MultipartFile audioFile = audioMap.get(audioSimpleName);
                        if (audioFile == null) {
                            audioFile = audioMap.get(dto.getAudioFileName());
                        }

                        if (audioFile != null) {
                            try {
                                audioUrl = cloudinaryService.uploadAudio(audioFile.getBytes(), dto.getRomaji());
                                logger.info("Upload audio thành công: {} -> {}", dto.getAudioFileName(), audioUrl);
                            } catch (Exception e) {
                                logger.error("Upload audio thất bại: {}", dto.getAudioFileName(), e);
                                errors.add("Dòng " + index + " - audio '" + dto.getAudioFileName() + "': " + e.getMessage());
                            }
                        } else {
                            logger.warn("Không tìm thấy file audio: {}", dto.getAudioFileName());
                            errors.add("Dòng " + index + " - Không tìm thấy file audio: " + dto.getAudioFileName());
                        }
                    }

                    // Lưu vào database
                    saveVocabulary(dto, imageUrl, audioUrl);
                    result.incrementSuccess();
                    logger.info("Đã lưu từ vựng: {}", dto.getExpression());

                } catch (Exception e) {
                    logger.error("Lỗi import từ vựng: {}", dto.getExpression(), e);
                    errors.add("Dòng " + index + " - " + dto.getExpression() + ": " + e.getMessage());
                    result.incrementFail();
                }
            }

            result.setErrors(errors);
            logger.info("Import hoàn tất: success={}, fail={}, total={}",
                    result.getSuccess(), result.getFail(), result.getTotal());

        } catch (Exception e) {
            logger.error("Lỗi đọc file Excel", e);
            throw new RuntimeException("Lỗi đọc file Excel: " + e.getMessage());
        }

        return result;
    }

    /**
     * Lưu từ vựng vào database
     */
    private void saveVocabulary(ExcelImportDTO dto, String imageUrl, String audioUrl) {
        Vocabulary vocab = new Vocabulary();
        vocab.setExpression(dto.getExpression());
        vocab.setKana(dto.getKana());
        vocab.setRomaji(dto.getRomaji());
        vocab.setMeaning(dto.getMeaning());
        vocab.setImageUrl(imageUrl);
        vocab.setAudioUrl(audioUrl);
        vocab.setWordType(dto.getWordType());
        vocab.setExample(dto.getExample());
        vocab.setExampleVi(dto.getExampleVi());
        vocab.setIsActive(true);  // Mặc định active

        vocabularyRepository.save(vocab);
    }

    /**
     * Lấy giá trị Cell dưới dạng String
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                // Kiểm tra nếu là số nguyên thì không hiển thị .0
                double numValue = cell.getNumericCellValue();
                if (numValue == (long) numValue) {
                    return String.valueOf((long) numValue);
                }
                return String.valueOf(numValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (IllegalStateException e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    // ========== Inner Class ImportResult ==========
    public static class ImportResult {
        private int total;
        private int success;
        private int fail;
        private List<String> errors;

        public ImportResult() {
            this.total = 0;
            this.success = 0;
            this.fail = 0;
            this.errors = new ArrayList<>();
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getSuccess() {
            return success;
        }

        public void setSuccess(int success) {
            this.success = success;
        }

        public int getFail() {
            return fail;
        }

        public void setFail(int fail) {
            this.fail = fail;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }

        public void incrementSuccess() {
            this.success++;
        }

        public void incrementFail() {
            this.fail++;
        }
    }
}