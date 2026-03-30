package com.example.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "Interactive_Scenes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InteractiveScene {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "order_index")
    private Integer orderIndex = 0;

    // Liên kết với bài học
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lessons lesson;

    // Liên kết với các điểm chạm thuộc cảnh này
    @OneToMany(mappedBy = "scene", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InteractivePoint> points;
}