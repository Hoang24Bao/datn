package com.example.Repository;

import com.example.Entity.Tests;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestRepository extends JpaRepository<Tests, Integer> {

    List<Tests> findByCategoryIdAndIsActiveTrue(Integer categoryId);

    Optional<Tests> findByIdAndIsActiveTrue(Integer id);

    @Query("SELECT t FROM Tests t WHERE t.categoryId = :categoryId AND t.isActive = true")
    List<Tests> findActiveTestsByCategory(@Param("categoryId") Integer categoryId);

    // Tìm kiếm và lọc
    @Query("SELECT t FROM Tests t WHERE " +
            "(:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:categoryId IS NULL OR t.categoryId = :categoryId) AND " +
            "(:isActive IS NULL OR t.isActive = :isActive)")
    Page<Tests> findByFilters(@Param("categoryId") Integer categoryId,
                              @Param("isActive") Boolean isActive,
                              @Param("search") String search,
                              Pageable pageable);

    // Helper method cho trường hợp không có search
    default Page<Tests> findByFilters(Integer categoryId, Boolean isActive, Pageable pageable) {
        return findByFilters(categoryId, isActive, null, pageable);
    }

    // Helper method cho trường hợp có search
    default Page<Tests> findByTitleContainingIgnoreCaseAndFilters(String search, Integer categoryId, Boolean isActive, Pageable pageable) {
        return findByFilters(categoryId, isActive, search, pageable);
    }

    @Query("SELECT COUNT(t) FROM Tests t WHERE t.categoryId = :categoryId AND t.isActive = true")
    Integer countByCategoryId(@Param("categoryId") Integer categoryId);
}