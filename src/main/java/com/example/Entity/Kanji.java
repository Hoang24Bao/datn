package com.example.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "Kanji")
@Data
public class Kanji {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "character", nullable = false)
    private String character;

    @Column(name = "sino_vietnamese")
    private String sinoVietnamese;

    @Column(nullable = false)
    private String meaning;

    @Column(name = "on_reading")
    private String onReading;

    @Column(name = "kun_reading")
    private String kunReading;

    @Column(name = "stroke_count")
    private Integer strokeCount;

    @Column(name = "jlpt_level")
    private String jlptLevel;

    private String radical;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "kanji", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("kanji")
    private List<KanjiExample> examples;
}