package com.example.Repository;

import com.example.Entity.Lessons;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonsRepository extends JpaRepository<Lessons, Integer> {

    // Query cũ - giữ nguyên
    @Query(value = "SELECT l.id, l.lesson_name, c.category_name, " +
            "(SELECT COUNT(*) FROM lesson_vocab lv WHERE lv.lesson_id = l.id) as total_vocab " +
            "FROM lessons l LEFT JOIN categories c ON l.category_id = c.id",
            nativeQuery = true)
    List<Object[]> findAllLessonsWithStats();

    // Query cũ - giữ nguyên
    @Query("SELECT l FROM Lessons l WHERE l.category.id = :categoryId")
    List<Lessons> findByCategoryId(@Param("categoryId") Integer categoryId);

    // Query cũ - giữ nguyên
    @Query("SELECT l FROM Lessons l WHERE l.category.jlptLevel = :level")
    List<Lessons> findByCategory_JlptLevel(@Param("level") String level);

    // API phân trang KÈM SỐ LƯỢNG TỪ VỰNG - DÙNG NATIVE QUERY
    @Query(value = "SELECT l.id, l.lesson_name, l.category_id, c.category_name, " +
            "COALESCE((SELECT COUNT(*) FROM lesson_vocab lv WHERE lv.lesson_id = l.id), 0) as total_vocab " +
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
}