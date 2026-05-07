package com.example.Repository;

import com.example.Entity.Questions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionsRepository extends JpaRepository<Questions, Integer> {

    @Query("SELECT q FROM Questions q JOIN q.questionCategories qc WHERE qc.id.categoryId = :categoryId AND q.isActive = true")
    List<Questions> findByCategoryId(@Param("categoryId") Integer categoryId);

    @Query("SELECT q FROM Questions q WHERE q.type = :type AND q.isActive = true")
    List<Questions> findByType(@Param("type") String type);
}