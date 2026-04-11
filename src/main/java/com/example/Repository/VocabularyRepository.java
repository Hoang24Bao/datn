package com.example.Repository;

import com.example.Entity.Vocabulary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VocabularyRepository extends JpaRepository<Vocabulary, Long> {

    Page<Vocabulary> findByExpressionContainingOrMeaningContaining(String expr, String mean, Pageable pageable);

    // Lấy danh sách từ vựng theo ID của bài học (Lesson)
    @Query(value = "SELECT v.* FROM Vocabulary v " +
            "JOIN Lesson_Vocab lv ON v.id = lv.vocab_id " +
            "WHERE lv.lesson_id = :lessonId", nativeQuery = true)
    List<Vocabulary> findByLessonId(@Param("lessonId") Integer lessonId);
}