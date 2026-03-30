package com.example.Service;

import com.example.Entity.Users;
import com.example.Repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UsersRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public Users register(Users user) {
        // 1. Kiểm tra username đã tồn tại chưa
        if (userRepository.existsByUserName(user.getUserName())) {
            throw new RuntimeException("Username đã tồn tại!");
        }

        // 2. Mã hóa mật khẩu (Quan trọng nhất)
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        // 3. Thiết lập mặc định nếu chưa có
        if (user.getRole() == null) {
            user.setRole("user"); // Mặc định là người học
        }

        return userRepository.save(user);
    }
}
