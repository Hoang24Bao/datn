package com.example.Controller;

import com.example.Entity.Roles;
import com.example.config.JwtUtils;
import com.example.Dto.Request.LoginRequest;
import com.example.Dto.Response.JwtResponse;
import com.example.Entity.Users;
import com.example.Service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private JwtUtils jwtUtils;

    // API Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Users user = authService.login(loginRequest.getUserName(), loginRequest.getPassword());

            String jwt = jwtUtils.generateTokenFromUsername(user);

            // TRUYỀN user VÀO ĐÂY
            String roleName = user.getRoles().stream()
                    .findFirst()
                    .map(Roles::getName)
                    .orElse("ROLE_USER");

            return ResponseEntity.ok(new JwtResponse(
                    jwt,
                    user.getId(),
                    user.getUserName(),
                    user.getFullname(),
                    roleName // Trả về String cho Frontend nhận diện
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    // API Đăng ký
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Users user) {
        try {
            Users savedUser = authService.register(user);
            savedUser.setPassword(null);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
