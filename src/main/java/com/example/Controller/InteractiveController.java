package com.example.Controller;

import com.example.Entity.InteractivePoint;
import com.example.Entity.InteractiveScene;
import com.example.Entity.Categories;
import com.example.Entity.Vocabulary;
import com.example.Repository.InteractivePointRepository;
import com.example.Repository.InteractiveSceneRepository;
import com.example.Repository.CategoriesRepository;
import com.example.Repository.VocabularyRepository;
import com.example.Service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/interactive")
public class InteractiveController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private InteractiveSceneRepository sceneRepository;

    @Autowired
    private InteractivePointRepository pointRepository;

    @Autowired
    private CategoriesRepository categoriesRepository;

    @Autowired
    private VocabularyRepository vocabularyRepository;

    @PostMapping("/scene")
    public ResponseEntity<?> createScene(
            @RequestParam("file") MultipartFile file,
            @RequestParam("categoryId") Integer categoryId,
            @RequestParam(value = "description", required = false) String description) {
        try {
            String imageUrl = cloudinaryService.uploadImage(file);

            Categories category = categoriesRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chủ đề"));

            InteractiveScene scene = new InteractiveScene();
            scene.setCategoryId(categoryId);
            scene.setImageUrl(imageUrl);
            scene.setDescription(description);

            int maxOrder = sceneRepository.getMaxOrderIndex(categoryId);
            scene.setOrderIndex(maxOrder + 1);

            sceneRepository.save(scene);

            return ResponseEntity.ok(Map.of(
                    "sceneId", scene.getId(),
                    "imageUrl", imageUrl,
                    "message", "Tạo cảnh thành công!"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Thêm điểm tương tác
    @PostMapping("/point")
    public ResponseEntity<?> createPoint(@RequestBody Map<String, Object> payload) {
        try {
            Integer sceneId = Integer.parseInt(payload.get("sceneId").toString());
            Integer vocabId = Integer.parseInt(payload.get("vocabId").toString());

            InteractiveScene scene = sceneRepository.findById(sceneId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy cảnh"));

            Vocabulary vocabulary = vocabularyRepository.findById(vocabId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy từ vựng"));

            InteractivePoint point = new InteractivePoint();
            point.setSceneId(sceneId);
            point.setVocabId(vocabId);
            point.setCoordX(Double.parseDouble(payload.get("coordX").toString()));
            point.setCoordY(Double.parseDouble(payload.get("coordY").toString()));
            point.setWidth(Double.parseDouble(payload.get("width").toString()));
            point.setHeight(Double.parseDouble(payload.get("height").toString()));

            pointRepository.save(point);
            return ResponseEntity.ok(Map.of("message", "Thêm điểm tương tác thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Lấy danh sách scenes theo category
    @GetMapping("/scenes/{categoryId}")
    public ResponseEntity<?> getScenesByCategory(@PathVariable Integer categoryId) {
        List<InteractiveScene> scenes = sceneRepository.findByCategoryIdOrderByOrderIndexAsc(categoryId);

        List<Map<String, Object>> result = scenes.stream().map(scene -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", scene.getId());
            item.put("imageUrl", scene.getImageUrl());
            item.put("description", scene.getDescription());
            item.put("orderIndex", scene.getOrderIndex());

            List<InteractivePoint> points = pointRepository.findBySceneId(scene.getId());
            List<Map<String, Object>> pointList = points.stream().map(point -> {
                Map<String, Object> pointItem = new HashMap<>();
                pointItem.put("id", point.getId());
                pointItem.put("coordX", point.getCoordX());
                pointItem.put("coordY", point.getCoordY());
                pointItem.put("width", point.getWidth());
                pointItem.put("height", point.getHeight());

                Vocabulary vocab = point.getVocabulary();
                if (vocab != null) {
                    pointItem.put("vocab", Map.of(
                            "id", vocab.getId(),
                            "expression", vocab.getExpression(),
                            "kana", vocab.getKana(),
                            "romaji", vocab.getRomaji(),
                            "meaning", vocab.getMeaning(),
                            "imageUrl", vocab.getImageUrl()
                    ));
                }
                return pointItem;
            }).collect(Collectors.toList());
            item.put("points", pointList);
            return item;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // Xóa scene
    @DeleteMapping("/scene/{sceneId}")
    public ResponseEntity<?> deleteScene(@PathVariable Integer sceneId) {
        try {
            sceneRepository.deleteById(sceneId);
            return ResponseEntity.ok(Map.of("message", "Xóa thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Tạo scene từ URL
    @PostMapping("/scene-from-url")
    public ResponseEntity<?> createSceneFromUrl(@RequestBody Map<String, Object> payload) {
        try {
            String imageUrl = (String) payload.get("imageUrl");
            Object categoryIdObj = payload.get("categoryId");

            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "imageUrl không được để trống"));
            }

            if (categoryIdObj == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "categoryId không được để trống"));
            }

            Integer categoryId = Integer.parseInt(categoryIdObj.toString());
            String description = (String) payload.get("description");

            Categories category = categoriesRepository.findById(categoryId)
                    .orElse(null);

            if (category == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy chủ đề với ID: " + categoryId));
            }

            InteractiveScene scene = new InteractiveScene();
            scene.setCategoryId(categoryId);
            scene.setImageUrl(imageUrl);
            scene.setDescription(description != null ? description : "");

            int maxOrder = sceneRepository.getMaxOrderIndex(categoryId);
            scene.setOrderIndex(maxOrder + 1);

            InteractiveScene savedScene = sceneRepository.save(scene);

            Map<String, Object> response = new HashMap<>();
            response.put("sceneId", savedScene.getId());
            response.put("message", "Tạo cảnh thành công!");

            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "categoryId phải là số"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Upload ảnh scene
    @PostMapping("/upload-scene-image")
    public ResponseEntity<?> uploadSceneImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = cloudinaryService.uploadImage(file);
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Lấy chi tiết scene theo ID
    @GetMapping("/scene/{sceneId}")
    public ResponseEntity<?> getSceneById(@PathVariable Integer sceneId) {
        try {
            InteractiveScene scene = sceneRepository.findById(sceneId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy cảnh"));

            List<InteractivePoint> points = pointRepository.findBySceneId(sceneId);

            Map<String, Object> result = new HashMap<>();
            result.put("id", scene.getId());
            result.put("imageUrl", scene.getImageUrl());
            result.put("description", scene.getDescription());
            result.put("orderIndex", scene.getOrderIndex());

            List<Map<String, Object>> pointList = points.stream().map(point -> {
                Map<String, Object> pointItem = new HashMap<>();
                pointItem.put("id", point.getId());
                pointItem.put("coordX", point.getCoordX());
                pointItem.put("coordY", point.getCoordY());
                pointItem.put("width", point.getWidth());
                pointItem.put("height", point.getHeight());
                pointItem.put("vocabId", point.getVocabId());

                Vocabulary vocab = point.getVocabulary();
                if (vocab != null) {
                    pointItem.put("vocab", Map.of(
                            "id", vocab.getId(),
                            "expression", vocab.getExpression(),
                            "meaning", vocab.getMeaning()
                    ));
                }
                return pointItem;
            }).collect(Collectors.toList());

            result.put("points", pointList);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Cập nhật mô tả scene
    @PutMapping("/scene/update")
    public ResponseEntity<?> updateScene(@RequestBody Map<String, Object> payload) {
        try {
            Integer sceneId = Integer.parseInt(payload.get("sceneId").toString());
            String description = (String) payload.get("description");

            InteractiveScene scene = sceneRepository.findById(sceneId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy cảnh"));

            if (description != null) {
                scene.setDescription(description);
            }
            sceneRepository.save(scene);

            return ResponseEntity.ok(Map.of("message", "Cập nhật thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Xóa tất cả points của scene
    @DeleteMapping("/scene/{sceneId}/points")
    public ResponseEntity<?> deleteAllPoints(@PathVariable Integer sceneId) {
        try {
            List<InteractivePoint> points = pointRepository.findBySceneId(sceneId);
            pointRepository.deleteAll(points);
            return ResponseEntity.ok(Map.of("message", "Xóa thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}