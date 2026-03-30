package com.example.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "Lessons")
@Data
public class Lessons {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String lessonName;
    // Thay đổi ở đây: Mapping thực tế với bảng Categories
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Categories category;

    // Giữ lại cái này nếu bạn vẫn muốn dùng ID thuần túy trong một số trường hợp
    @Column(name = "category_id")
    private Integer categoryId;
    private String thumbnailUrl;
    private Integer orderIndex;

    @Column(name = "is_free")
    private Boolean free;
}