package com.example.Repository;

import com.example.Entity.Lessons;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LessonsRepository extends JpaRepository<Lessons, Integer> {
    // Tìm danh sách bài học dựa trên ID của danh mục
    List<Lessons> findByCategoryId(Integer categoryId);
}