package com.example.config;

import com.example.Entity.Roles;
import com.example.Entity.Users; // Đảm bảo import đúng Entity Users của bạn
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    private final String jwtSecret = "ChuoiBiMatNayPhaiThatDaiVaKhoDoanDeBaoMatGanbatteProject";
    private final int jwtExpirationMs = 86400000;

    private Key key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

   
    public String generateTokenFromUsername(Users user) {
        String roleName = user.getRoles().stream()
                .findFirst()
                .map(Roles::getName)
                .orElse("ROLE_USER");

        return Jwts.builder()
                .setSubject(user.getUserName())
                .claim("role", roleName)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    // THÊM HÀM NÀY: Để Filter có thể lấy Role ra kiểm tra quyền admin
    public String getRoleFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    // ✅ Sửa lại
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken); // Đổi parse → parseClaimsJws
            return true;
        } catch (Exception e) {
            System.out.println("Token không hợp lệ: " + e.getMessage());
        }
        return false;
    }
}