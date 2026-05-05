package com.example.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryService.class);
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Autowired
    private Cloudinary cloudinary;

    public String uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống!");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File quá lớn! Tối đa 5MB");
        }

        try {
            String uniqueId = UUID.randomUUID().toString();
            String publicId = folder + "_" + uniqueId;

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "public_id", publicId,
                            "resource_type", "auto",
                            "allowed_formats", new String[]{"jpg", "jpeg", "png", "gif", "webp"}
                    )
            );

            String url = uploadResult.get("secure_url").toString();
            logger.info("Upload file thành công: {} -> {}", file.getOriginalFilename(), url);
            return url;

        } catch (IOException e) {
            logger.error("Lỗi upload file lên Cloudinary: {}", e.getMessage());
            throw new RuntimeException("Lỗi upload file: " + e.getMessage());
        }
    }

    /**
     * Upload ảnh lên Cloudinary vào folder "vocabs" nhưng URL không chứa tên folder
     */
    public String uploadVocabImage(MultipartFile file, String romaji) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống!");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File quá lớn! Tối đa 5MB");
        }

        try {
            String baseName = romaji.trim().toLowerCase().replaceAll("[^a-z0-9]", "_");
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            String publicId = baseName + "_" + uniqueId;

            // Upload vào folder vocabs (để quản lý), nhưng URL sẽ tự động không hiển thị folder?
            // Thực tế Cloudinary URL vẫn hiển thị folder. Để ẩn folder, cần upload vào root.
            // Giải pháp: Upload vào root nhưng đặt public_id có dạng vocabs/filename
            // Hoặc chấp nhận URL có folder.

            // Cách 1: Upload vào root (không folder) - URL sẽ không có folder
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "vocabs",
                            "public_id", publicId,
                            "resource_type", "image",
                            "allowed_formats", new String[]{"jpg", "jpeg", "png", "gif", "webp"}
                    )
            );

            String url = uploadResult.get("secure_url").toString();
            logger.info("Upload ảnh thành công: {} -> {}", romaji, url);
            return url;

        } catch (IOException e) {
            logger.error("Lỗi upload ảnh lên Cloudinary: {}", e.getMessage());
            throw new RuntimeException("Lỗi upload ảnh: " + e.getMessage());
        }
    }

    /**
     * Upload ảnh lên Cloudinary (không cần romaji - dùng cho ảnh tương tác)
     *
     * @param file File ảnh cần upload
     * @return URL của ảnh đã upload
     */
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống!");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File quá lớn! Tối đa 5MB");
        }

        try {
            // Tạo tên file ngẫu nhiên
            String uniqueId = UUID.randomUUID().toString();
            String publicId = "scene_" + uniqueId;

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "interactive",  // Upload vào folder interactive
                            "public_id", publicId,
                            "resource_type", "image",
                            "allowed_formats", new String[]{"jpg", "jpeg", "png", "gif", "webp"}
                    )
            );

            String url = uploadResult.get("secure_url").toString();
            logger.info("Upload ảnh thành công: {} -> {}", file.getOriginalFilename(), url);
            return url;

        } catch (IOException e) {
            logger.error("Lỗi upload ảnh lên Cloudinary: {}", e.getMessage());
            throw new RuntimeException("Lỗi upload ảnh: " + e.getMessage());
        }
    }

    /**
     * Upload audio lên Cloudinary
     */
    public String uploadAudio(byte[] audioData, String romaji) {
        try {
            String baseName = romaji.trim().toLowerCase().replaceAll("[^a-z0-9]", "_");
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            String publicId = baseName + "_" + uniqueId;

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    audioData,
                    ObjectUtils.asMap(
                            "folder", "audios",
                            "public_id", publicId,
                            "resource_type", "video",
                            "format", "mp3"
                    )
            );

            String url = uploadResult.get("secure_url").toString();
            logger.info("Upload audio thành công: {} -> {}", romaji, url);
            return url;

        } catch (IOException e) {
            logger.error("Lỗi upload audio lên Cloudinary: {}", e.getMessage());
            throw new RuntimeException("Lỗi upload audio: " + e.getMessage());
        }
    }
}