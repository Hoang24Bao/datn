package com.example.Repository;

import com.example.Entity.InteractivePoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface InteractivePointRepository extends JpaRepository<InteractivePoint, Integer> {

    List<InteractivePoint> findBySceneId(Integer sceneId);

    @Query("SELECT p FROM InteractivePoint p WHERE p.sceneId = :sceneId")
    List<InteractivePoint> findPointsBySceneId(@Param("sceneId") Integer sceneId);

    @Query("SELECT COUNT(p) FROM InteractivePoint p WHERE p.vocabId = :vocabId")
    int countByVocabId(@Param("vocabId") Integer vocabId);

    // Đếm số lượng interactive points theo vocab_id và category_id (qua scene)
    @Query(value = "SELECT COUNT(*) FROM Interactive_Points ip " +
            "INNER JOIN Interactive_Scenes ist ON ip.scene_id = ist.id " +
            "WHERE ip.vocab_id = :vocabId AND ist.category_id = :categoryId",
            nativeQuery = true)
    int countByVocabIdAndCategoryId(@Param("vocabId") Integer vocabId,
                                    @Param("categoryId") Integer categoryId);

    // Lấy thông tin các scene có chứa vocab này
    @Query(value = "SELECT ist.id as sceneId, ist.description as sceneName, " +
            "ist.category_id as categoryId, c.category_name as categoryName, " +
            "l.id as lessonId " +
            "FROM Interactive_Points ip " +
            "INNER JOIN Interactive_Scenes ist ON ip.scene_id = ist.id " +
            "INNER JOIN Categories c ON ist.category_id = c.id " +
            "LEFT JOIN Lessons l ON l.category_id = c.id " +
            "WHERE ip.vocab_id = :vocabId " +
            "GROUP BY ist.id, ist.description, ist.category_id, c.category_name, l.id",
            nativeQuery = true)
    List<Map<String, Object>> findSceneUsageByVocabId(@Param("vocabId") Integer vocabId);
}