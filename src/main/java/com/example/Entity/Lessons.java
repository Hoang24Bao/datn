package com.example.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "Lessons")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "vocabularies"})
@Data
public class Lessons {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lesson_name")
    private String lessonName;
    // Thay đổi ở đây: Mapping thực tế với bảng Categories
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "lessons"})
    private Categories category;

    // Giữ lại cái này nếu bạn vẫn muốn dùng ID thuần túy trong một số trường hợp
    @Column(name = "category_id")
    private Integer categoryId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Vocabulary> vocabularies;

    private String thumbnailUrl;
    private Integer orderIndex;

    @Column(name = "is_free")
    private Boolean free;
    @Column(name = "is_active")
    private Boolean isActive = true;
}