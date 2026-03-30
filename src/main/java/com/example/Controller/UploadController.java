package com.example.Controller;

import com.example.Entity.Vocabulary;
import com.example.Repository.VocabularyRepository;
import com.example.Service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class UploadController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private VocabularyRepository vocabularyRepository; // 1. Thêm dòng này

    @PostMapping("/admin/vocabulary/add")
    public String saveVocabulary(@ModelAttribute Vocabulary vocab,
                                 @RequestParam("imageFile") MultipartFile imageFile,
                                 @RequestParam("audioFile") MultipartFile audioFile) {

        // 2. Upload và lấy URL từ Cloudinary
        if (!imageFile.isEmpty()) {
            String imageUrl = cloudinaryService.uploadFile(imageFile);
            vocab.setImageUrl(imageUrl); // Lưu link vào đối tượng vocab
        }

        if (!audioFile.isEmpty()) {
            String audioUrl = cloudinaryService.uploadFile(audioFile);
            vocab.setAudioUrl(audioUrl); // Lưu link vào đối tượng vocab
        }

        // 3. Lưu vào SQL Server
        vocabularyRepository.save(vocab); // Sửa lỗi gọi static ở đây

        return "redirect:/admin/vocabulary";
    }
}
