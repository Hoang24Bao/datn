package com.example.Entity;

import jakarta.persistence.*;


@Entity
@Table(name = "Vocabulary")
public class Vocabulary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String expression;
    private String kana;
    private String romaji;
    private String meaning;
    private String wordType;
    private String imageUrl;
    private String audioUrl;
    private String example;
    private String exampleVi;

    // Getters and Setters
}