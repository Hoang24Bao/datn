package com.example.Repository;

import com.example.Entity.InteractiveScene;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InteractiveSceneRepository extends JpaRepository<InteractiveScene, Integer> {
    // Lấy tất cả các cảnh thuộc về một bài học, sắp xếp theo thứ tự orderIndex
    List<InteractiveScene> findByLessonIdOrderByOrderIndexAsc(Integer lessonId);
}
