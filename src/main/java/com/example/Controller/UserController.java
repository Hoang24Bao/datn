package com.example.Controller;

import com.example.Entity.Users;
import com.example.Repository.UsersRepository;
import com.example.Service.CloudinaryService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class UserController {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CloudinaryService cloudinaryService;


    @PostMapping("/api/user/upload-avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            Users currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
            }

            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng chọn file ảnh"));
            }

            String avatarUrl = cloudinaryService.uploadFile(file, "avatar");
            currentUser.setAvatarUrl(avatarUrl);
            usersRepository.save(currentUser);

            return ResponseEntity.ok(Map.of("avatarUrl", avatarUrl, "message", "Upload ảnh thành công!"));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Upload ảnh thất bại: " + e.getMessage()));
        }
    }

    @PutMapping("/api/user/profile")
    @Transactional
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> payload) {
        try {
            Users currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
            }

            if (payload.containsKey("fullname")) {
                String fullname = (String) payload.get("fullname");
                if (fullname != null && !fullname.trim().isEmpty()) {
                    currentUser.setFullname(fullname.trim());
                }
            }

            if (payload.containsKey("email")) {
                String email = (String) payload.get("email");
                if (email != null && !email.trim().isEmpty()) {
                    currentUser.setEmail(email.trim());
                }
            }

            if (payload.containsKey("levelId")) {
                Integer levelId = (Integer) payload.get("levelId");
                if (levelId != null && levelId >= 1 && levelId <= 5) {
                    currentUser.setLevelId(levelId);
                }
            }

            if (payload.containsKey("avatarUrl")) {
                String avatarUrl = (String) payload.get("avatarUrl");
                if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                    currentUser.setAvatarUrl(avatarUrl);
                }
            }

            if (payload.containsKey("newPassword")) {
                String currentPassword = (String) payload.get("currentPassword");
                String newPassword = (String) payload.get("newPassword");

                if (currentPassword == null || currentPassword.trim().isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập mật khẩu hiện tại"));
                }

                if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu hiện tại không đúng"));
                }

                currentUser.setPassword(passwordEncoder.encode(newPassword));
            }

            usersRepository.save(currentUser);

            return ResponseEntity.ok(Map.of("message", "Cập nhật thông tin thành công!"));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Cập nhật thất bại: " + e.getMessage()));
        }
    }

    private Users getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        String username = auth.getName();
        Optional<Users> userOpt = usersRepository.findByUserName(username);
        return userOpt.orElse(null);
    }


}