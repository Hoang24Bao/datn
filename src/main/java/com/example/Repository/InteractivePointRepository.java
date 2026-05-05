package com.example.Repository;

import com.example.Entity.InteractivePoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface InteractivePointRepository extends JpaRepository<InteractivePoint, Integer> {
    List<InteractivePoint> findBySceneId(Integer sceneId);

    @Query("SELECT COUNT(ip) FROM InteractivePoint ip WHERE ip.vocabulary.id = :vocabId")
    int countByVocabId(@Param("vocabId") Integer vocabId);

    @Query("SELECT COUNT(ip) FROM InteractivePoint ip WHERE ip.vocabulary.id = :vocabId AND ip.scene.lesson.id = :lessonId")
    int countByVocabIdAndLessonId(@Param("vocabId") Integer vocabId, @Param("lessonId") Integer lessonId);

    @Query("SELECT new map(ip.scene.id as sceneId, ip.scene.description as sceneName, ip.scene.lesson.id as lessonId, ip.scene.lesson.lessonName as lessonName) FROM InteractivePoint ip WHERE ip.vocabulary.id = :vocabId GROUP BY ip.scene.id, ip.scene.description, ip.scene.lesson.id, ip.scene.lesson.lessonName")
    List<Map<String, Object>> findSceneUsageByVocabId(@Param("vocabId") Integer vocabId);
}