package com.example.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Interactive_Points")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InteractivePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "coord_x", nullable = false)
    private Double coordX; // Tọa độ X (%)

    @Column(name = "coord_y", nullable = false)
    private Double coordY; // Tọa độ Y (%)

    // Điểm này thuộc về cảnh nào
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scene_id", nullable = false)
    private InteractiveScene scene;

    // Điểm này ứng với từ vựng nào
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocab_id", nullable = false)
    private Vocabulary vocabulary;

    @Column(name = "width")
    private Double width;

    @Column(name = "height")
    private Double height;
}