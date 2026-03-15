package com.example.Repository;

import com.example.Entity.Lessons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LessonsRepository extends JpaRepository<Lessons, Integer> {

    // Spring Boot sẽ tự hiểu đây là: SELECT * FROM Lessons WHERE category_id = ?
    List<Lessons> findByCategoryId(Integer categoryId);
}