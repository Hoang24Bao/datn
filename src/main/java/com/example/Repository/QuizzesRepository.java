package com.example.Repository;

import com.example.Entity.Quizzes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizzesRepository extends JpaRepository<Quizzes, Integer> {

    @Query("SELECT q FROM Quizzes q JOIN q.quizCategories qc WHERE qc.id.categoryId = :categoryId AND q.isActive = true")
    List<Quizzes> findByCategoryId(@Param("categoryId") Integer categoryId);

    List<Quizzes> findByTypeAndIsActiveTrue(String type);
}