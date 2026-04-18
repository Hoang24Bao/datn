package com.example.Repository;

import com.example.Entity.Categories;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.domain.Pageable;

@Repository
public interface CategoriesRepository extends JpaRepository<Categories, Integer> {
    // JpaRepository đã có sẵn các hàm findAll(), findById()
    boolean existsByCategoryName(String categoryName);

    List<Categories> findByIsActiveTrue();

    // Tìm kiếm và phân trang categories
    @Query("SELECT c FROM Categories c WHERE " +
            "(:search IS NULL OR :search = '' OR LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:level IS NULL OR :level = '' OR c.jlptLevel = :level) " +
            "AND (:isActive IS NULL OR c.isActive = :isActive)")
    Page<Categories> findByFilters(
            @Param("search") String search,
            @Param("level") String level,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    // Lấy categories theo level (dùng cho filter)
    @Query("SELECT c FROM Categories c WHERE c.jlptLevel = :level AND c.isActive = true")
    List<Categories> findByJlptLevel(@Param("level") String level);
}