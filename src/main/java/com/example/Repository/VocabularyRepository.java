package com.example.Repository;

import com.example.Entity.Vocabulary;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VocabularyRepository extends JpaRepository<Vocabulary, Integer> {

    Page<Vocabulary> findByExpressionContainingOrMeaningContaining(String expression, String meaning, Pageable pageable);

    // Lấy danh sách từ vựng theo ID của bài học (Lesson)
    @Query("SELECT v FROM Vocabulary v " +
            "JOIN LessonVocab lv ON v.id = lv.vocabId " +
            "WHERE lv.lessonId = :lessonId AND v.isActive = true " +
            "ORDER BY lv.displayOrder ASC")
    List<Vocabulary> findByLessonId(@Param("lessonId") Integer lessonId);

    // Lọc từ vựng theo Level (Đi xuyên qua bảng Lessons và Categories)
    @Query(value = "SELECT DISTINCT v.* FROM Vocabulary v " +
            "INNER JOIN Lesson_Vocab lv ON v.id = lv.vocab_id " +
            "INNER JOIN Lessons l ON lv.lesson_id = l.id " +
            "INNER JOIN Categories c ON l.category_id = c.id " +
            "WHERE (:level IS NULL OR :level = '' OR c.jlpt_level = :level) " +
            "AND (:cateId IS NULL OR c.id = :cateId) " + // Lọc chính xác ID chủ đề
            "AND (:search IS NULL OR :search = '' OR (" +
            "v.expression LIKE '%' + :search + '%' " +
            "OR v.meaning LIKE '%' + :search + '%' " +
            "OR v.kana LIKE '%' + :search + '%' " +
            "OR v.romaji LIKE '%' + :search + '%'))",
            countQuery = "SELECT COUNT(DISTINCT v.id) FROM Vocabulary v " +
                    "INNER JOIN Lesson_Vocab lv ON v.id = lv.vocab_id " +
                    "INNER JOIN Lessons l ON lv.lesson_id = l.id " +
                    "INNER JOIN Categories c ON l.category_id = c.id " +
                    "WHERE (:level IS NULL OR :level = '' OR c.jlpt_level = :level) " +
                    "AND (:cateId IS NULL OR c.id = :cateId) " +
                    "AND (:search IS NULL OR :search = '' OR (" +
                    "v.expression LIKE '%' + :search + '%' " +
                    "OR v.meaning LIKE '%' + :search + '%' " +
                    "OR v.kana LIKE '%' + :search + '%' " +
                    "OR v.romaji LIKE '%' + :search + '%'))",
            nativeQuery = true)
    Page<Vocabulary> findByFilters(@Param("level") String level,
                                   @Param("cateId") Integer cateId,
                                   @Param("search") String search,
                                   Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO Lesson_Vocab (lesson_id, vocab_id) VALUES (:lessonId, :vocabId)", nativeQuery = true)
    void insertLessonVocab(@Param("lessonId") Integer lessonId, @Param("vocabId") Integer vocabId);
}