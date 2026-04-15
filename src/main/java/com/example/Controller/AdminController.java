package com.example.Controller;

import com.example.Dto.Response.AdminStatsDTO;
import com.example.Entity.Users;
import com.example.Entity.Vocabulary;
import com.example.Repository.CategoriesRepository;
import com.example.Repository.LessonsRepository;
import com.example.Repository.UsersRepository;
import com.example.Repository.VocabularyRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @DeleteMapping("/users/{id}")
    @Transactional
    public ResponseEntity<?> toggleUserStatus(@PathVariable Integer id) {
        return userRepository.findById(id)
                .map(user -> {
                    // Đảo ngược trạng thái active
                    boolean newStatus = !user.getActive();
                    user.setActive(newStatus);
                    userRepository.save(user);

                    String statusLabel = newStatus ? "mở khóa" : "khóa";
                    return ResponseEntity.ok().body("Đã " + statusLabel + " tài khoản học viên");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Lấy chi tiết học viên (Chế độ chỉ xem)
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserDetail(@PathVariable Integer id) {
        return userRepository.findById(id)
                .map(user -> {
                    // Tạo một Map để lọc dữ liệu trả về, tránh gửi field password
                    Map<String, Object> details = new HashMap<>();
                    details.put("id", user.getId());
                    details.put("userName", user.getUserName());
                    details.put("fullname", user.getFullname());
                    details.put("email", user.getEmail());
                    details.put("levelId", user.getLevelId());
                    details.put("active", user.getActive());
                    details.put("avatarUrl", user.getAvatarUrl());
                    details.put("created", user.getCreated());
                    details.put("totalPoints", user.getTotalPoints());

                    return ResponseEntity.ok(details);
                })
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/users/all-paging")
    public ResponseEntity<?> getAllStudentsPaging(
            @RequestParam(required = false) Integer levelId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String timeFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        java.time.LocalDateTime dateLimit = null;
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        if (timeFilter != null) {
            switch (timeFilter) {
                case "24h":
                    dateLimit = now.minusDays(1);
                    break;
                case "week":
                    dateLimit = now.minusWeeks(1);
                    break;
                case "month":
                    dateLimit = now.minusMonths(1);
                    break;
                case "3months":
                    dateLimit = now.minusMonths(3);
                    break;
                case "6months":
                    dateLimit = now.minusMonths(6);
                    break;
            }
        }

        // Sort
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Integer activeInt = (active == null) ? null : (active ? 1 : 0);
        Page<Users> usersPage = userRepository.findStudentsWithFilters(levelId, activeInt, search, dateLimit, pageable);

        return ResponseEntity.ok(usersPage);
    }

}