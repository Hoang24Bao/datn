package com.example.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
@Table(name = "Vocabulary")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Vocabulary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String expression;
    private String kana;
    private String romaji;
    private String meaning;
    private String wordType;
    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "audio_url")
    private String audioUrl;

    @Column(name = "example_vi")
    private String exampleVi;
    private String example;

}