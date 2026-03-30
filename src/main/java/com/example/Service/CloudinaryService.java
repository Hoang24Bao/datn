package com.example.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) {
        try {
            // ObjectUtils.asMap("resource_type", "auto") giúp Cloudinary tự nhận diện
            // xem đó là ảnh hay file âm thanh để lưu vào đúng folder.
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("resource_type", "auto"));

            return uploadResult.get("secure_url").toString(); // Trả về link https an toàn
        } catch (IOException e) {
            throw new RuntimeException("Lỗi upload tài nguyên lên Cloudinary: " + e.getMessage());
        }
    }
}
