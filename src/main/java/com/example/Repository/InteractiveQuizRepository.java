package com.example.Repository;

import com.example.Entity.Categories;
import com.example.Entity.InteractivePoint;
import com.example.Entity.InteractiveScene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InteractiveQuizRepository extends JpaRepository<Categories, Integer> {  // EXTENDS JPA REPOSITORY

    // Lấy danh sách category có scene
    @Query("SELECT DISTINCT c FROM Categories c " +
            "WHERE c.isActive = true " +
            "AND EXISTS (SELECT 1 FROM InteractiveScene s WHERE s.categoryId = c.id AND s.isActive = true)")
    List<Categories> findCategoriesWithScenes();

    // Lấy danh sách scenes theo category
    @Query("SELECT s FROM InteractiveScene s " +
            "WHERE s.categoryId = :categoryId AND s.isActive = true " +
            "ORDER BY s.orderIndex")
    List<InteractiveScene> findScenesByCategory(@Param("categoryId") Integer categoryId);

    // Lấy danh sách points theo scene
    @Query("SELECT p FROM InteractivePoint p " +
            "WHERE p.sceneId = :sceneId")
    List<InteractivePoint> findPointsByScene(@Param("sceneId") Integer sceneId);

    // Lấy random vocab khác trong cùng category (cho đáp án nhiễu)
    @Query(value = "SELECT TOP 3 v.id, v.expression " +
            "FROM Vocabulary v " +
            "INNER JOIN Lesson_Vocab lv ON v.id = lv.vocab_id " +
            "INNER JOIN Lessons l ON lv.lesson_id = l.id " +
            "WHERE l.category_id = :categoryId AND v.id != :vocabId AND v.is_active = 1 " +
            "ORDER BY NEWID()", nativeQuery = true)
    List<Object[]> findRandomVocabByCategory(@Param("categoryId") Integer categoryId,
                                             @Param("vocabId") Integer vocabId);
}