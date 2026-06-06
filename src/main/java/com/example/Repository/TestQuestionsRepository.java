package com.example.Repository;

import com.example.Entity.TestQuestions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestQuestionsRepository extends JpaRepository<TestQuestions, Integer> {

    List<TestQuestions> findByTestIdOrderByOrderIndexAsc(Integer testId);

    @Query("SELECT COUNT(tq) FROM TestQuestions tq WHERE tq.testId = :testId")
    Integer countByTestId(@Param("testId") Integer testId);
}