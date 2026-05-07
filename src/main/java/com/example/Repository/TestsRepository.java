package com.example.Repository;

import com.example.Entity.Tests;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TestsRepository extends JpaRepository<Tests, Integer> {

    List<Tests> findByCategoryIdAndIsActiveTrue(Integer categoryId);

    @Query("SELECT t FROM Tests t WHERE t.category.id = :categoryId AND t.isActive = true ORDER BY t.orderIndex ASC")
    List<Tests> findByCategoryIdOrderByOrderIndex(@Param("categoryId") Integer categoryId);
}