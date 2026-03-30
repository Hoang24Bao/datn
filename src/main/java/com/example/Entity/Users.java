package com.example.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "Users")
@Data // Tự động tạo Getter/Setter nếu dùng Lombok
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_name", unique = true, nullable = false)
    private String userName;

    @Column(nullable = false)
    private String password;

    private String fullname;
    private String email;

    @Column(nullable = false)
    private String role; // 'admin' hoặc 'user'

    @Column(name = "level_id")
    private Integer levelId;

    @Column(name = "total_points")
    private Integer totalPoints = 0;

    @Column(name = "avatar_url")
    private String avatarUrl;

    private Boolean active = true;

    @Column(updatable = false)
    private LocalDateTime created = LocalDateTime.now();
}