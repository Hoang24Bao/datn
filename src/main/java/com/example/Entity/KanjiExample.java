package com.example.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Kanji_Examples")
@Data
public class KanjiExample {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "kanji_id")
    @JsonIgnoreProperties("examples")
    private Kanji kanji;

    private String word;
    private String reading;
    private String meaning;
}