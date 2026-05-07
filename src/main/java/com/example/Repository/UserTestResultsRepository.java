package com.example.Repository;

import com.example.Entity.UserTestResults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserTestResultsRepository extends JpaRepository<UserTestResults, Integer> {

    List<UserTestResults> findByUserIdAndTestId(Integer userId, Integer testId);

    @Query("SELECT MAX(u.score) FROM UserTestResults u WHERE u.user.id = :userId AND u.test.id = :testId")
    Optional<Float> findMaxScoreByUserAndTest(@Param("userId") Integer userId, @Param("testId") Integer testId);

    @Query("SELECT u FROM UserTestResults u WHERE u.user.id = :userId AND u.test.category.id = :categoryId")
    List<UserTestResults> findByUserIdAndCategoryId(@Param("userId") Integer userId, @Param("categoryId") Integer categoryId);
}