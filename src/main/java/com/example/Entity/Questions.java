package com.example.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Questions")
@Data
public class Questions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "type", nullable = false, length = 30)
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocab_id", nullable = false)
    private Vocabulary vocab;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "options", columnDefinition = "NVARCHAR(MAX)")
    private String options;

    @Column(name = "difficulty")
    private Integer difficulty = 1;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<QuestionCategories> questionCategories;
}