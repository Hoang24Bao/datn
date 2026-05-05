package com.example.Controller;

import com.example.Entity.Roles;
import com.example.config.JwtUtils;
import com.example.Dto.Request.LoginRequest;
import com.example.Dto.Response.JwtResponse;
import com.example.Entity.Users;
import com.example.Service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest,
                                   HttpServletResponse httpResponse) {
        try {
            Users user = authService.login(loginRequest.getUserName(), loginRequest.getPassword());
            String jwt = jwtUtils.generateTokenFromUsername(user);

            // Gắn JWT vào cookie HttpOnly
            Cookie jwtCookie = new Cookie("JWT_TOKEN", jwt);
            jwtCookie.setHttpOnly(true);   // JS không đọc được → an toàn hơn
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(86400);    // 1 ngày
            // jwtCookie.setSecure(true);  // Bật khi dùng HTTPS
            httpResponse.addCookie(jwtCookie);

            String roleName = user.getRoles().stream()
                    .findFirst().map(Roles::getName).orElse("ROLE_USER");

            return ResponseEntity.ok(new JwtResponse(
                    jwt, user.getId(), user.getUserName(),
                    user.getFullname(), roleName, user.getAvatarUrl()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

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

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("JWT_TOKEN", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Xóa cookie
        response.addCookie(cookie);
        return ResponseEntity.ok("Logged out");
    }
}