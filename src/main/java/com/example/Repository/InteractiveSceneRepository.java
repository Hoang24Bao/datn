package com.example.Repository;

import com.example.Entity.InteractiveScene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InteractiveSceneRepository extends JpaRepository<InteractiveScene, Integer> {

    // Sửa từ findByLessonId → findByCategoryId
    List<InteractiveScene> findByCategoryIdOrderByOrderIndexAsc(Integer categoryId);

    // Lấy scene đang active theo category
    List<InteractiveScene> findByCategoryIdAndIsActiveTrueOrderByOrderIndexAsc(Integer categoryId);

    // Lấy max order index trong category
    // Thêm method này nếu chưa có
    @Query("SELECT COALESCE(MAX(s.orderIndex), 0) FROM InteractiveScene s WHERE s.categoryId = :categoryId")
    int getMaxOrderIndex(@Param("categoryId") Integer categoryId);


    @Query("SELECT s FROM InteractiveScene s WHERE s.categoryId = :categoryId AND s.isActive = true ORDER BY s.orderIndex")
    List<InteractiveScene> findActiveScenesByCategory(@Param("categoryId") Integer categoryId);

    
}