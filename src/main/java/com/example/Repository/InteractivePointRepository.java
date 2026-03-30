package com.example.Repository;

import com.example.Entity.InteractivePoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InteractivePointRepository extends JpaRepository<InteractivePoint, Integer> {
    // Lấy tất cả các điểm chạm thuộc về một cảnh cụ thể
    List<InteractivePoint> findBySceneId(Integer sceneId);
}
