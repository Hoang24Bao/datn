package com.example.Repository;

import com.example.Entity.UserTestResults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTestResultRepository extends JpaRepository<UserTestResults, Integer> {

    List<UserTestResults> findByUserIdOrderByCompletedAtDesc(Integer userId);

    List<UserTestResults> findByUserIdAndTestIdOrderByCompletedAtDesc(Integer userId, Integer testId);

    @Query("SELECT MAX(utr.score) FROM UserTestResults utr WHERE utr.userId = :userId AND utr.testId = :testId AND utr.isPassed = true")
    Float findBestScoreByUserAndTest(@Param("userId") Integer userId, @Param("testId") Integer testId);

    @Query("SELECT COUNT(utr) > 0 FROM UserTestResults utr WHERE utr.userId = :userId AND utr.testId = :testId AND utr.isPassed = true")
    boolean hasUserPassedTest(@Param("userId") Integer userId, @Param("testId") Integer testId);

    Optional<UserTestResults> findTopByUserIdAndTestIdOrderByIdDesc(Integer userId, Integer testId);

    @Query("SELECT COUNT(DISTINCT utr.testId) FROM UserTestResults utr " +
            "WHERE utr.userId = :userId AND utr.isPassed = true AND utr.testId IN " +
            "(SELECT t.id FROM Tests t WHERE t.categoryId = :categoryId)")
    Integer countCompletedTestsByUserAndCategory(@Param("userId") Integer userId,
                                                 @Param("categoryId") Integer categoryId);

    @Query("SELECT COUNT(t.id) > 0 FROM UserTestResults utr " +
            "JOIN Tests t ON t.id = utr.testId " +
            "WHERE utr.userId = :userId AND t.categoryId = :categoryId AND utr.isPassed = true")
    boolean hasUserCompletedAnyTestInCategory(@Param("userId") Integer userId,
                                              @Param("categoryId") Integer categoryId);

    List<UserTestResults> findByUserIdAndTestIdAndCompletedAtIsNull(Integer userId, Integer testId);

    @Query("SELECT MAX(utr.score) FROM UserTestResults utr " +
            "WHERE utr.userId = :userId AND utr.testId = :testId AND utr.isPassed = true")
    Optional<Integer> findBestPassedScoreByUserAndTest(@Param("userId") Integer userId,
                                                       @Param("testId") Integer testId);

    @Query("SELECT COUNT(DISTINCT t.testId) FROM UserTestResults t WHERE t.userId = :userId AND t.isPassed = true")
    int countPassedTestsByUser(@Param("userId") Integer userId);

    List<UserTestResults> findByUserIdAndCompletedAtIsNotNullOrderByCompletedAtDesc(Integer userId);

    @Query("SELECT utr.id, utr.testId, " +
            "t.title, c.categoryName, " +
            "utr.totalCount, utr.durationSeconds, " +
            "utr.score, t.maxScore, " +
            "utr.completedAt, utr.isPassed " +
            "FROM UserTestResults utr " +
            "JOIN Tests t ON utr.testId = t.id " +
            "JOIN Categories c ON t.categoryId = c.id " +
            "WHERE utr.userId = :userId AND utr.completedAt IS NOT NULL " +
            "ORDER BY utr.completedAt DESC")
    List<Object[]> findHistoryWithDetails(@Param("userId") Integer userId);
}