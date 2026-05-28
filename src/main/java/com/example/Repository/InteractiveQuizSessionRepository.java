
package com.example.Repository;

import com.example.Entity.InteractiveQuizSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InteractiveQuizSessionRepository extends JpaRepository<InteractiveQuizSession, Integer> {

    Optional<InteractiveQuizSession> findByUserIdAndCategoryIdAndIsCompletedFalse(
            @Param("userId") Integer userId,
            @Param("categoryId") Integer categoryId);

    List<InteractiveQuizSession> findByUserIdAndIsCompletedTrueOrderByCompletedAtDesc(
            @Param("userId") Integer userId);

    @Query("SELECT COUNT(DISTINCT s.categoryId) FROM InteractiveQuizSession s " +
            "WHERE s.userId = :userId AND s.isCompleted = true")
    Integer countCompletedCategoriesByUserId(@Param("userId") Integer userId);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM InteractiveQuizSession s " +
            "WHERE s.userId = :userId AND s.categoryId = :categoryId AND s.isCompleted = true")
    boolean existsByUserIdAndCategoryIdAndIsCompletedTrue(@Param("userId") Integer userId,
                                                          @Param("categoryId") Integer categoryId);

    
}