package com.example.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Categories")
@Data // Nếu bạn dùng Thư viện Lombok, nếu không hãy tạo Getter/Setter thủ công
public class Categories {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String categoryName;
    private String slug;
    private String iconUrl;

    // Các cột bạn mới thêm vào SSMS
    private Integer totalLessons;
    private Integer progress;
    private String jlptLevel;
}