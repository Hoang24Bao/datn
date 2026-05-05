package com.example.Service;

import com.example.Entity.Roles;
import com.example.Entity.Users;
import com.example.Repository.RoleRepository;
import com.example.Repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UsersRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;  // Dùng interface, không dùng class cụ thể

    @Autowired
    private RoleRepository roleRepository;

    public Users register(Users user) {
        // 1. Kiểm tra username đã tồn tại chưa
        if (userRepository.existsByUserName(user.getUserName())) {
            throw new RuntimeException("Username đã tồn tại!");
        }

        // 2. Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email này đã được sử dụng!");
        }

        // 3. Mã hóa mật khẩu
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        // 4. Thiết lập Role mặc định
        Roles userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy Role USER trong DB"));
        user.getRoles().add(userRole);

        // 5. Thiết lập Level mặc định
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        if (isAdmin) {
            user.setLevelId(null);
        } else {
            user.setLevelId(1);
        }

        user.setActive(true);

        return userRepository.save(user);
    }

    public Users login(String loginInput, String rawPassword) {
        // 1. Tìm user theo username hoặc email
        Users user = userRepository.findByUserNameOrEmail(loginInput, loginInput)
                .orElseThrow(() -> new RuntimeException("Tên đăng nhập hoặc Email không tồn tại!"));

        // 2. Kiểm tra mật khẩu
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu không chính xác!");
        }

        // 3. Kiểm tra trạng thái tài khoản
        if (user.getActive() == null || !user.getActive()) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Admin!");
        }

        return user;
    }
}