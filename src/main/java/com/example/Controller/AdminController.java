package com.example.Controller;

import com.example.Entity.Users;
import com.example.Repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UsersRepository userRepository;

    // 1. Lấy thống kê tổng quan
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        // Sau này bạn có thể thêm:
        // stats.put("totalVocab", vocabRepository.count());
        return ResponseEntity.ok(stats);
    }

    // 2. Lấy danh sách người dùng mới nhất
    @GetMapping("/users/latest")
    public ResponseEntity<List<Users>> getLatestUsers() {
        // Lấy toàn bộ hoặc top 10 user mới nhất
        List<Users> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }
}