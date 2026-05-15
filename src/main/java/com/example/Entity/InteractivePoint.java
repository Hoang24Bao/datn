package com.example.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "Interactive_Points")
@Data
public class InteractivePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "scene_id", nullable = false)
    private Integer sceneId;

    @Column(name = "vocab_id", nullable = false)
    private Integer vocabId;

    @Column(name = "coord_x", nullable = false)
    private Double coordX;

    @Column(name = "coord_y", nullable = false)
    private Double coordY;

    @Column(name = "width")
    private Double width = 8.0;

    @Column(name = "height")
    private Double height = 8.0;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scene_id", insertable = false, updatable = false)
    private InteractiveScene scene;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocab_id", insertable = false, updatable = false)
    private Vocabulary vocabulary;
}