package com.example.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "Categories")
@Data
public class Categories {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String categoryName;
    private String slug;
    private String iconUrl;
    private Integer totalLessons;
    private Integer progress;
    private String jlptLevel;
    @Column(name = "is_active")
    private Boolean isActive = true;
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
    @Column(name = "is_locked")
    private Boolean isLocked = true;

    @Column(name = "next_category_id")
    private Integer nextCategoryId;

    @Column(name = "unlock_avg_score")
    private Double unlockAvgScore = 70.0;

    @Column(name = "total_tests")
    private Integer totalTests = 0;

    @OneToMany(mappedBy = "category")
    private List<Lessons> lessons;
}