package com.example.Repository;

import com.example.Entity.UserQuizHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserQuizHistoryRepository extends JpaRepository<UserQuizHistory, Integer> {

    List<UserQuizHistory> findByUserIdAndQuizIdOrderByCompletedAtDesc(Integer userId, Integer quizId);

    @Query("SELECT COUNT(u) FROM UserQuizHistory u WHERE u.user.id = :userId AND u.quiz.id IN (SELECT qc.id.quizId FROM QuizCategories qc WHERE qc.id.categoryId = :categoryId)")
    long countByUserIdAndCategoryId(@Param("userId") Integer userId, @Param("categoryId") Integer categoryId);
}