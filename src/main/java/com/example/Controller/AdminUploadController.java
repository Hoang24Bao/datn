package com.example.Controller;

import com.example.Service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/upload")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUploadController {

    @Autowired
    private CloudinaryService cloudinaryService;

    //Upload ảnh từ vựng lên Cloudinary (folder vocabs)

    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("romaji") String romaji) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File không được để trống!");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("Chỉ chấp nhận file ảnh!");
            }

            if (romaji == null || romaji.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Vui lòng nhập Romaji trước khi upload ảnh!");
            }

            String imageUrl = cloudinaryService.uploadVocabImage(file, romaji);

            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("message", "Upload ảnh thành công!");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi upload: " + e.getMessage());
        }
    }
}