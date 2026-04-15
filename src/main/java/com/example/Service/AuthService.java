package com.example.Service;

import com.example.Entity.Roles;
import com.example.Entity.Users;
import com.example.Repository.RoleRepository;
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

        // 2. Mã hóa mật khẩu (Quan trọng nhất)
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        // 3. Thiết lập mặc định nếu chưa có
        Roles userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy Role USER trong DB"));

        user.getRoles().add(userRole);

        // 4. Thiết lập Level mặc định dựa trên Role
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        if (isAdmin) {
            user.setLevelId(null); // Admin thì để null
        } else {
            user.setLevelId(1);    // User bình thường mặc định là 1 (N5)
        }

        user.setActive(true);

        return userRepository.save(user);
    }

    public Users login(String loginInput, String rawPassword) {
        // 1. Tìm user theo username
        Users user = userRepository.findByUserNameOrEmail(loginInput, loginInput)
                .orElseThrow(() -> new RuntimeException("Tên đăng nhập hoặc Email không tồn tại!"));

        // 2. Kiểm tra mật khẩu (Sử dụng passwordEncoder.matches)
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu không chính xác!");
        }

        // 3. KIỂM TRA TRẠNG THÁI TÀI KHOẢN
        // Giả sử user.getActive() trả về Integer (0 hoặc 1) hoặc Boolean
        if (user.getActive() != null && !user.getActive()) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Admin!");
        }

        // 4. Nếu đúng, trả về thông tin user (sau này sẽ trả về JWT Token)
        return user;
    }
}
