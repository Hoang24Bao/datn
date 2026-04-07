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

    // ĐỔI THAM SỐ: Truyền vào cả đối tượng Users thay vì mỗi username
    public String generateTokenFromUsername(Users user) {
        // Lấy tên role đầu tiên (hoặc nối chuỗi nếu user có nhiều role)
        String roleName = user.getRoles().stream()
                .findFirst()
                .map(Roles::getName) // Nhớ là Role (số ít) nhé, khớp với Entity của bạn
                .orElse("ROLE_USER");

        return Jwts.builder()
                .setSubject(user.getUserName())
                .claim("role", roleName) // Lưu dưới dạng String "ROLE_ADMIN"
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

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (Exception e) {
            System.out.println("Token không hợp lệ: " + e.getMessage());
        }
        return false;
    }
}