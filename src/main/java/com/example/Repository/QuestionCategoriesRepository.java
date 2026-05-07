package com.example.Repository;

import com.example.Entity.QuestionCategories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionCategoriesRepository extends JpaRepository<QuestionCategories, QuestionCategories.QuestionCategoriesId> {

    @Query("SELECT qc.category.id FROM QuestionCategories qc WHERE qc.id.questionId = :questionId")
    List<Integer> findCategoryIdsByQuestionId(@Param("questionId") Integer questionId);
}