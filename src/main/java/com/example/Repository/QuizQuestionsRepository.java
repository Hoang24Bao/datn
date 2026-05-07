package com.example.Repository;

import com.example.Entity.QuizQuestions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizQuestionsRepository extends JpaRepository<QuizQuestions, QuizQuestions.QuizQuestionsId> {

    @Query("SELECT qq.question FROM QuizQuestions qq WHERE qq.id.quizId = :quizId")
    List<com.example.Entity.Questions> findQuestionsByQuizId(@Param("quizId") Integer quizId);
}