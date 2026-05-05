package com.example.Repository;

import com.example.Entity.Categories;
import com.example.Entity.InteractiveScene;
import com.example.Entity.Lessons;
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
public interface LessonsRepository extends JpaRepository<Lessons, Integer> {

    // ========== CÁC METHOD HIỆN CÓ ==========

    List<Lessons> findByCategoryIdAndIsActiveTrue(Integer categoryId);

    List<Lessons> findByCategoryIdAndIsActiveTrueOrderByOrderIndexAsc(Integer categoryId);

    @Query(value = "SELECT l.id, l.lesson_name, l.order_index, l.is_free, l.is_active " +
            "FROM lessons l " +
            "WHERE l.category_id = :categoryId AND l.is_active = 1 " +
            "ORDER BY l.order_index ASC", nativeQuery = true)
    List<Object[]> findActiveLessonsByCategoryIdNative(@Param("categoryId") Integer categoryId);

    @Query(value = "SELECT l.id, l.lesson_name, l.order_index, l.is_free, l.is_active " +
            "FROM lessons l " +
            "WHERE l.category_id = :categoryId AND l.is_active = 1 " +
            "ORDER BY l.order_index ASC", nativeQuery = true)
    List<Object[]> findActiveLessonsDTO(@Param("categoryId") Integer categoryId);

    @Query(value = "SELECT l.id, l.lesson_name, c.category_name, " +
            "(SELECT COUNT(*) FROM lesson_vocab lv WHERE lv.lesson_id = l.id) as total_vocab " +
            "FROM lessons l LEFT JOIN categories c ON l.category_id = c.id",
            nativeQuery = true)
    List<Object[]> findAllLessonsWithStats();

    @Query("SELECT l FROM Lessons l WHERE l.category.id = :categoryId")
    List<Lessons> findByCategoryId(@Param("categoryId") Integer categoryId);

    @Query("SELECT l FROM Lessons l WHERE l.category.jlptLevel = :level")
    List<Lessons> findByCategory_JlptLevel(@Param("level") String level);

    @Query(value = "SELECT l.id, l.lesson_name, l.category_id, c.category_name, " +
            "COALESCE((SELECT COUNT(*) FROM lesson_vocab lv WHERE lv.lesson_id = l.id), 0) as total_vocab, " +
            "l.is_active " +
            "FROM lessons l " +
            "LEFT JOIN categories c ON l.category_id = c.id " +
            "WHERE (:search IS NULL OR :search = '' OR " +
            "LOWER(l.lesson_name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.category_name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:categoryId IS NULL OR l.category_id = :categoryId) " +
            "AND (:level IS NULL OR :level = '' OR c.jlpt_level = :level) " +
            "ORDER BY l.id ASC",
            countQuery = "SELECT COUNT(*) FROM lessons l " +
                    "LEFT JOIN categories c ON l.category_id = c.id " +
                    "WHERE (:search IS NULL OR :search = '' OR " +
                    "LOWER(l.lesson_name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                    "LOWER(c.category_name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                    "AND (:categoryId IS NULL OR l.category_id = :categoryId) " +
                    "AND (:level IS NULL OR :level = '' OR c.jlpt_level = :level)",
            nativeQuery = true)
    Page<Object[]> findLessonsWithStatsByFilters(
            @Param("search") String search,
            @Param("categoryId") Integer categoryId,
            @Param("level") String level,
            Pageable pageable);

    @Query(value = "SELECT l.id, l.lesson_name, l.is_active, " +
            "COALESCE((SELECT COUNT(*) FROM lesson_vocab lv WHERE lv.lesson_id = l.id), 0) as total_vocab " +
            "FROM lessons l " +
            "WHERE l.category_id = :categoryId " +
            "ORDER BY l.id ASC", nativeQuery = true)
    List<Object[]> findLessonsByCategoryIdWithStats(@Param("categoryId") Integer categoryId);

    @Query("SELECT s FROM InteractiveScene s WHERE s.lesson.id = :lessonId ORDER BY s.orderIndex ASC")
    List<InteractiveScene> findScenesByLessonId(@Param("lessonId") Integer lessonId);

    @Query(value = "SELECT COUNT(*) FROM Lesson_Vocab WHERE lesson_id = :lessonId AND vocab_id = :vocabId", nativeQuery = true)
    int countVocabInLesson(@Param("lessonId") Integer lessonId, @Param("vocabId") Integer vocabId);

    default boolean isVocabInLesson(Integer lessonId, Integer vocabId) {
        return countVocabInLesson(lessonId, vocabId) > 0;
    }

    @Modifying
    @Query(value = "INSERT INTO Lesson_Vocab (lesson_id, vocab_id, display_order) VALUES (:lessonId, :vocabId, :displayOrder)", nativeQuery = true)
    void addVocabToLesson(@Param("lessonId") Integer lessonId, @Param("vocabId") Integer vocabId, @Param("displayOrder") Integer displayOrder);

    @Query(value = "SELECT v.* FROM Vocabulary v WHERE v.is_active = 1 " +
            "AND v.id NOT IN (SELECT lv.vocab_id FROM Lesson_Vocab lv WHERE lv.lesson_id = :lessonId)",
            nativeQuery = true)
    List<Vocabulary> findAvailableVocabForLesson(@Param("lessonId") Integer lessonId);

    @Modifying
    @Query(value = "DELETE FROM Lesson_Vocab WHERE lesson_id = :lessonId AND vocab_id = :vocabId", nativeQuery = true)
    int removeVocabFromLesson(@Param("lessonId") Integer lessonId, @Param("vocabId") Integer vocabId);

    @Query(value = "SELECT COUNT(*) FROM lessons WHERE category_id = :categoryId AND is_active = 1", nativeQuery = true)
    int countActiveByCategory(@Param("categoryId") Integer categoryId);

    @Query(value = "SELECT COUNT(*) FROM lessons WHERE category_id = :categoryId AND LOWER(lesson_name) = LOWER(:lessonName)", nativeQuery = true)
    int countByLessonNameAndCategoryId(@Param("lessonName") String lessonName, @Param("categoryId") Integer categoryId);

    @Query(value = "SELECT COUNT(*) FROM lessons WHERE category_id = :categoryId AND LOWER(lesson_name) = LOWER(:lessonName) AND id != :excludeId", nativeQuery = true)
    int countByLessonNameAndCategoryIdExcludeId(@Param("lessonName") String lessonName, @Param("categoryId") Integer categoryId, @Param("excludeId") Integer excludeId);

    @Query("SELECT COUNT(l) FROM Lessons l WHERE l.categoryId = :categoryId")
    int countByCategoryId(@Param("categoryId") Integer categoryId);

    @Query(value = "SELECT MAX(display_order) FROM Lesson_Vocab WHERE lesson_id = :lessonId", nativeQuery = true)
    Integer getMaxDisplayOrder(@Param("lessonId") Integer lessonId);


    //Đếm số lượng bài học đang active (is_active = true) theo category_id
    @Query("SELECT COUNT(l) FROM Lessons l WHERE l.categoryId = :categoryId AND l.isActive = true")
    int countByCategoryIdAndIsActiveTrue(@Param("categoryId") Integer categoryId);
}