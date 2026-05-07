package com.example.Repository;

import com.example.Entity.QuizCategories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizCategoriesRepository extends JpaRepository<QuizCategories, QuizCategories.QuizCategoriesId> {

    @Query("SELECT qc.category.id FROM QuizCategories qc WHERE qc.id.quizId = :quizId")
    List<Integer> findCategoryIdsByQuizId(@Param("quizId") Integer quizId);
}