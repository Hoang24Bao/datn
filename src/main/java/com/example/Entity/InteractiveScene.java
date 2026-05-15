package com.example.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Interactive_Scenes")
@Data
public class InteractiveScene {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @Column(name = "description")
    private String description;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "order_index")
    private Integer orderIndex = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Categories category;

    @OneToMany(mappedBy = "scene", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<InteractivePoint> points;
}