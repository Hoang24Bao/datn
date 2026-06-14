package com.example.Service;

import com.example.Entity.InteractivePoint;
import com.example.Entity.InteractiveScene;
import com.example.Entity.Vocabulary;
import com.example.Repository.InteractivePointRepository;
import com.example.Repository.InteractiveSceneRepository;
import com.example.Repository.VocabularyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InteractiveSceneService {

    @Autowired
    private InteractiveSceneRepository sceneRepository;

    @Autowired
    private InteractivePointRepository pointRepository;

    @Autowired
    private VocabularyRepository vocabularyRepository;

    // Lấy danh sách scene theo Category (chỉ scene đang active)
    public List<InteractiveScene> getScenesByCategory(Integer categoryId) {
        return sceneRepository.findByCategoryIdAndIsActiveTrueOrderByOrderIndexAsc(categoryId);
    }

    // Lấy danh sách scene theo Category (kể cả scene inactive - cho admin)
    public List<InteractiveScene> getAllScenesByCategory(Integer categoryId) {
        return sceneRepository.findByCategoryIdOrderByOrderIndexAsc(categoryId);
    }

    // Lấy danh sách từ vựng trong Category
    public List<Vocabulary> getVocabByCategory(Integer categoryId) {
        return vocabularyRepository.findByCategoryId(categoryId);
    }

    // Lấy chi tiết scene kèm points
    public InteractiveScene getSceneDetail(Integer sceneId) {
        return sceneRepository.findById(sceneId).orElse(null);
    }

    // Lấy điểm tương tác của scene kèm thông tin từ vựng
    public List<Map<String, Object>> getScenePointsWithVocab(Integer sceneId) {
        List<InteractivePoint> points = pointRepository.findBySceneId(sceneId);

        return points.stream().map(point -> {
            Vocabulary vocab = vocabularyRepository.findById(point.getVocabId()).orElse(null);
            Map<String, Object> result = new HashMap<>();
            result.put("id", point.getId());
            result.put("sceneId", point.getSceneId());
            result.put("vocabId", point.getVocabId());
            result.put("coordX", point.getCoordX());
            result.put("coordY", point.getCoordY());
            result.put("width", point.getWidth());
            result.put("height", point.getHeight());

            if (vocab != null) {
                result.put("expression", vocab.getExpression());
                result.put("kana", vocab.getKana());
                result.put("romaji", vocab.getRomaji());
                result.put("meaning", vocab.getMeaning());
                result.put("imageUrl", vocab.getImageUrl());
                result.put("audioUrl", vocab.getAudioUrl());
            }
            return result;
        }).collect(Collectors.toList());
    }

    // Lấy danh sách tất cả points của scene
    public List<InteractivePoint> getPointsByScene(Integer sceneId) {
        return pointRepository.findBySceneId(sceneId);
    }

    // Tạo scene mới
    @Transactional
    public InteractiveScene createScene(InteractiveScene scene) {
        if (scene.getIsActive() == null) {
            scene.setIsActive(true);
        }
        return sceneRepository.save(scene);
    }

    // Cập nhật scene
    @Transactional
    public InteractiveScene updateScene(InteractiveScene scene) {
        return sceneRepository.save(scene);
    }

    // Thêm point vào scene
    @Transactional
    public InteractivePoint addPoint(InteractivePoint point) {
        return pointRepository.save(point);
    }

    // Cập nhật point
    @Transactional
    public InteractivePoint updatePoint(InteractivePoint point) {
        return pointRepository.save(point);
    }

    // Xóa point
    @Transactional
    public void deletePoint(Integer pointId) {
        pointRepository.deleteById(pointId);
    }

    // Xóa tất cả points của scene
    @Transactional
    public void deleteAllPointsByScene(Integer sceneId) {
        List<InteractivePoint> points = pointRepository.findBySceneId(sceneId);
        if (points != null && !points.isEmpty()) {
            pointRepository.deleteAll(points);
        }
    }

    // Xóa scene
    @Transactional
    public void deleteScene(Integer sceneId) {
        sceneRepository.deleteById(sceneId);
    }

    public int getMaxOrderIndex(Integer categoryId) {
        return sceneRepository.getMaxOrderIndex(categoryId);
    }
}