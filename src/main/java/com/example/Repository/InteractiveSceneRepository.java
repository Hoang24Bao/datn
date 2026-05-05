package com.example.Repository;

import com.example.Entity.InteractiveScene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InteractiveSceneRepository extends JpaRepository<InteractiveScene, Integer> {
    List<InteractiveScene> findByLessonIdOrderByOrderIndexAsc(Integer lessonId);

    @Query("SELECT COALESCE(MAX(s.orderIndex), 0) FROM InteractiveScene s WHERE s.lesson.id = :lessonId")
    int getMaxOrderIndex(@Param("lessonId") Integer lessonId);
}