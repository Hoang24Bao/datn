package com.example.Repository;

import com.example.Entity.TestQuestions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TestQuestionsRepository extends JpaRepository<TestQuestions, TestQuestions.TestQuestionsId> {

    @Query("SELECT tq.question FROM TestQuestions tq WHERE tq.id.testId = :testId ORDER BY tq.orderIndex ASC")
    List<com.example.Entity.Questions> findQuestionsByTestId(@Param("testId") Integer testId);
}