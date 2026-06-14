package com.example.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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

    //Upload ảnh lên Cloudinary vào folder "vocabs"
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

    // Upload ảnh lên Cloudinary

    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống!");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File quá lớn! Tối đa 5MB");
        }

        try {
            String uniqueId = UUID.randomUUID().toString();
            String publicId = "scene_" + uniqueId;

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "interactive",
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

    //Upload audio lên Cloudinary
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

    public String uploadKanjiGif(MultipartFile file, Integer kanjiId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống!");
        }

        try {
            String publicId = String.format("kanji_%04d", kanjiId);

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "kanji-gif",
                            "public_id", publicId,
                            "resource_type", "image",
                            "allowed_formats", new String[]{"gif"}
                    )
            );

            String url = uploadResult.get("secure_url").toString();
            logger.info("Upload Kanji GIF thành công: ID {} -> {}", kanjiId, url);
            return url;

        } catch (IOException e) {
            logger.error("Lỗi upload Kanji GIF: {}", e.getMessage());
            throw new RuntimeException("Lỗi upload Kanji GIF: " + e.getMessage());
        }
    }

    // Batch upload từ URL (từ GitHub)
    public String uploadKanjiGifFromUrl(String gitUrl, Integer kanjiId) {
        try {
            byte[] gifBytes = new RestTemplate().getForObject(gitUrl, byte[].class);

            if (gifBytes == null || gifBytes.length == 0) {
                throw new RuntimeException("Không thể tải GIF từ URL: " + gitUrl);
            }

            String publicId = String.format("kanji_%04d", kanjiId);

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    gifBytes,
                    ObjectUtils.asMap(
                            "folder", "kanji-gif",
                            "public_id", publicId,
                            "resource_type", "image"
                    )
            );

            return uploadResult.get("secure_url").toString();

        } catch (Exception e) {
            logger.error("Lỗi upload từ URL cho kanji {}: {}", kanjiId, e.getMessage());
            return null;
        }
    }
}