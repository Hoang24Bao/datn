package com.example.Controller;

import com.example.Dto.Response.AdminStatsDTO;
import com.example.Entity.Users;
import com.example.Entity.Vocabulary;
import com.example.Repository.CategoriesRepository;
import com.example.Repository.LessonsRepository;
import com.example.Repository.UsersRepository;
import com.example.Repository.VocabularyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UsersRepository userRepository;

    @Autowired
    private LessonsRepository lessonRepository;

    @Autowired
    private VocabularyRepository vocabularyRepository;

    @Autowired
    private CategoriesRepository categoriesRepository;

    // 1. Lấy thống kê tổng quan
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getStats() {
        AdminStatsDTO stats = new AdminStatsDTO();

        // Đếm user có role_id = 2 (Dùng Native Query hoặc JPQL trong Repository)
        stats.setTotalUsers(userRepository.countUsersByRoleId(2));

        // Đếm tổng bài học
        stats.setTotalLessons(lessonRepository.count());

        // Đếm tổng từ vựng
        stats.setTotalVocab(vocabularyRepository.count());

        stats.setTotalCategories(categoriesRepository.count());

        return ResponseEntity.ok(stats);
    }

    // 2. Lấy danh sách người dùng mới nhất
    @GetMapping("/users/latest")
    public ResponseEntity<List<Users>> getLatestUsers() {
        java.time.LocalDateTime sevenDaysAgo = java.time.LocalDateTime.now().minusDays(7);

        List<Users> users = userRepository.findRecentUsers(sevenDaysAgo);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/all-students")
    public ResponseEntity<List<Users>> getAllStudents() {
        return ResponseEntity.ok(userRepository.findAllStudents());
    }

    @GetMapping("/vocab/all")
    public ResponseEntity<?> getVocabPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Vocabulary> vocabPage;

        if (search != null && !search.isEmpty()) {
            vocabPage = vocabularyRepository.findByExpressionContainingOrMeaningContaining(search, search, pageable);
        } else {
            vocabPage = vocabularyRepository.findAll(pageable);
        }

        return ResponseEntity.ok(vocabPage);
    }

}