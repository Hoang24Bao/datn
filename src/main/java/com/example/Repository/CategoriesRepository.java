package com.example.Repository;

import com.example.Entity.Categories;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

@Repository
public interface CategoriesRepository extends JpaRepository<Categories, Integer> {
    boolean existsByCategoryName(String categoryName);

    List<Categories> findByIsActiveTrue();

    List<Categories> findAllByOrderByIdAsc();

    // Tìm category theo slug
    Optional<Categories> findBySlug(String slug);

    // Tìm category lớn nhất có id nhỏ hơn (category trước đó)
    @Query("SELECT c FROM Categories c WHERE c.id < :id ORDER BY c.id DESC")
    List<Categories> findPreviousCategories(@Param("id") Integer id);

    // Tìm category nhỏ nhất có id lớn hơn (category tiếp theo)
    @Query("SELECT c FROM Categories c WHERE c.id > :id ORDER BY c.id ASC")
    List<Categories> findNextCategories(@Param("id") Integer id);

    // Lấy category đầu tiên (id nhỏ nhất)
    Optional<Categories> findFirstByOrderByIdAsc();

    // Lấy category cuối cùng (id lớn nhất)
    Optional<Categories> findTopByOrderByIdDesc();

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

    // Lấy categories theo level
    @Query("SELECT c FROM Categories c WHERE c.jlptLevel = :level AND c.isActive = true")
    List<Categories> findByJlptLevel(@Param("level") String level);

}