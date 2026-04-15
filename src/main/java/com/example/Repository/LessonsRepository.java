package com.example.Repository;

import com.example.Entity.Lessons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LessonsRepository extends JpaRepository<Lessons, Integer> {
    // Tìm danh sách bài học dựa trên ID của danh mục
    List<Lessons> findByCategoryId(Integer categoryId);

    // 2. Tìm theo Level của danh mục liên kết
    // Lưu ý: Tên phải khớp chính xác với biến trong Entity Categories (jlptLevel)
    List<Lessons> findByCategory_JlptLevel(String jlptLevel);

    // LessonsRepository.java
    @Query(value = """
            SELECT l.id, l.lesson_name as lessonName, 
                   c.category_name as categoryName,
                   COUNT(lv.vocab_id) as totalVocab
            FROM Lessons l
            LEFT JOIN Categories c ON l.category_id = c.id
            LEFT JOIN Lesson_Vocab lv ON l.id = lv.lesson_id
            GROUP BY l.id, l.lesson_name, c.category_name
            ORDER BY l.id
            """, nativeQuery = true)
    List<Object[]> findAllLessonsWithStats();


}