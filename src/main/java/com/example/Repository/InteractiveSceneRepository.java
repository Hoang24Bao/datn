package com.example.Repository;

import com.example.Entity.InteractiveScene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InteractiveSceneRepository extends JpaRepository<InteractiveScene, Integer> {

    List<InteractiveScene> findByCategoryIdOrderByOrderIndexAsc(Integer categoryId);

    List<InteractiveScene> findByCategoryIdAndIsActiveTrueOrderByOrderIndexAsc(Integer categoryId);

    @Query("SELECT COALESCE(MAX(s.orderIndex), 0) FROM InteractiveScene s WHERE s.categoryId = :categoryId")
    int getMaxOrderIndex(@Param("categoryId") Integer categoryId);


    @Query("SELECT s FROM InteractiveScene s WHERE s.categoryId = :categoryId AND s.isActive = true ORDER BY s.orderIndex")
    List<InteractiveScene> findActiveScenesByCategory(@Param("categoryId") Integer categoryId);


}