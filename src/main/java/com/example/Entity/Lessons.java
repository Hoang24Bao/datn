package com.example.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "Lessons")
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
    private Categories category;

    // Giữ lại cái này nếu bạn vẫn muốn dùng ID thuần túy trong một số trường hợp
    @Column(name = "category_id")
    private Integer categoryId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "Lesson_Vocab",
            joinColumns = @JoinColumn(name = "lesson_id"),
            inverseJoinColumns = @JoinColumn(name = "vocab_id")
    )
    private List<Vocabulary> vocabularies;

    private String thumbnailUrl;
    private Integer orderIndex;

    @Column(name = "is_free")
    private Boolean free;
}