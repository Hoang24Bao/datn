package com.example.Repository;

import com.example.Entity.Vocabulary;
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

    // 1. Filter + phân trang (dùng cho bảng admin)
    @Query(value = "SELECT v.* FROM Vocabulary v " +
            "LEFT JOIN Lesson_Vocab lv ON v.id = lv.vocab_id " +
            "LEFT JOIN Lessons l ON lv.lesson_id = l.id " +
            "LEFT JOIN Categories c ON l.category_id = c.id " +
            "WHERE 1=1 " +
            "AND (:level IS NULL OR :level = '' OR c.jlpt_level = :level) " +
            "AND (:cateId IS NULL OR l.category_id = :cateId) " +
            "AND (:search IS NULL OR :search = '' OR " +
            "LOWER(v.expression) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(v.kana) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(v.romaji) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(v.meaning) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "GROUP BY v.id, v.expression, v.kana, v.romaji, v.meaning, " +
            "v.image_url, v.audio_url, v.word_type, v.example, v.example_vi, v.is_active ",
            countQuery = "SELECT COUNT(DISTINCT v.id) FROM Vocabulary v " +
                    "LEFT JOIN Lesson_Vocab lv ON v.id = lv.vocab_id " +
                    "LEFT JOIN Lessons l ON lv.lesson_id = l.id " +
                    "LEFT JOIN Categories c ON l.category_id = c.id " +
                    "WHERE 1=1 " +
                    "AND (:level IS NULL OR :level = '' OR c.jlpt_level = :level) " +
                    "AND (:cateId IS NULL OR l.category_id = :cateId) " +
                    "AND (:search IS NULL OR :search = '' OR " +
                    "LOWER(v.expression) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                    "LOWER(v.kana) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                    "LOWER(v.romaji) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                    "LOWER(v.meaning) LIKE LOWER(CONCAT('%', :search, '%')))",
            nativeQuery = true)
    Page<Vocabulary> findByFilters(
            @Param("level") String level,
            @Param("cateId") Integer cateId,
            @Param("search") String search,
            Pageable pageable);

    // 2. Lấy danh sách từ vựng theo bài học (dùng API /by-lesson/{lessonId})
    @Query(value = "SELECT v.* FROM Vocabulary v " +
            "INNER JOIN Lesson_Vocab lv ON v.id = lv.vocab_id " +
            "WHERE lv.lesson_id = :lessonId " +
            "ORDER BY lv.display_order ASC",
            nativeQuery = true)
    List<Vocabulary> findByLessonId(@Param("lessonId") Integer lessonId);

    // 3. Thêm từ vựng vào bài học (bảng trung gian)
    @Modifying
    @Query(value = "INSERT INTO Lesson_Vocab (lesson_id, vocab_id, display_order) " +
            "VALUES (:lessonId, :vocabId, (SELECT COALESCE(MAX(display_order), 0) + 1 FROM Lesson_Vocab WHERE lesson_id = :lessonId))",
            nativeQuery = true)
    void insertLessonVocab(@Param("lessonId") Integer lessonId, @Param("vocabId") Integer vocabId);

    // 4. Kiểm tra từ vựng đã tồn tại trong bài học chưa
    @Query(value = "SELECT COUNT(*) FROM Lesson_Vocab WHERE lesson_id = :lessonId AND vocab_id = :vocabId", nativeQuery = true)
    int countVocabInLesson(@Param("lessonId") Integer lessonId, @Param("vocabId") Integer vocabId);

    // 5. Tìm từ vựng theo expression (kiểm tra trùng)
    boolean existsByExpression(String expression);

    // 6. Lấy tất cả từ vựng đang active
    List<Vocabulary> findByIsActiveTrue();

    // =====================================================
    // 7. Lấy danh sách từ vựng theo Category (sửa lại - dùng native query)
    // =====================================================
    @Query(value = "SELECT DISTINCT v.* FROM Vocabulary v " +
            "INNER JOIN Lesson_Vocab lv ON v.id = lv.vocab_id " +
            "INNER JOIN Lessons l ON lv.lesson_id = l.id " +
            "WHERE l.category_id = :categoryId AND v.is_active = 1 " +
            "ORDER BY v.id ASC",
            nativeQuery = true)
    List<Vocabulary> findByCategoryId(@Param("categoryId") Integer categoryId);

    // 8. Đếm số từ vựng trong Category
    @Query(value = "SELECT COUNT(DISTINCT v.id) FROM Vocabulary v " +
            "INNER JOIN Lesson_Vocab lv ON v.id = lv.vocab_id " +
            "INNER JOIN Lessons l ON lv.lesson_id = l.id " +
            "WHERE l.category_id = :categoryId AND v.is_active = 1",
            nativeQuery = true)
    int countByCategoryId(@Param("categoryId") Integer categoryId);

    @Query(value = "SELECT DISTINCT " +
            "v.id, v.expression, v.kana, v.romaji, v.meaning, " +
            "v.word_type, v.example, v.example_vi, v.image_url, v.audio_url, v.is_active, " +
            "l.lesson_name, l.id as lesson_id " +
            "FROM vocabulary v " +
            "INNER JOIN lesson_vocab lv ON v.id = lv.vocab_id " +
            "INNER JOIN lessons l ON l.id = lv.lesson_id " +
            "WHERE l.category_id = :categoryId " +
            "AND v.is_active = 1 " +
            "ORDER BY l.id, lv.display_order",
            nativeQuery = true)
    List<Object[]> findVocabByCategoryIdNative(@Param("categoryId") Integer categoryId);
}