package com.example.Controller;

import com.example.Dto.Request.AnswerRequestDTO;
import com.example.Dto.Response.QuizSessionResponseDTO;
import com.example.Entity.Categories;
import com.example.Entity.InteractiveQuizSession;
import com.example.Entity.InteractiveScene;
import com.example.Entity.InteractivePoint;
import com.example.Repository.InteractiveQuizRepository;
import com.example.Repository.InteractiveQuizSessionRepository;
import com.example.Repository.InteractiveSceneRepository;
import com.example.Repository.InteractivePointRepository;
import com.example.Service.InteractiveQuizService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quiz/interactive")
public class InteractiveQuizController {

    @Autowired
    private InteractiveQuizService quizService;

    @Autowired
    private InteractiveQuizSessionRepository sessionRepository;

    @Autowired
    private InteractiveQuizRepository quizRepository;

    @Autowired
    private InteractiveSceneRepository sceneRepository;

    @Autowired
    private InteractivePointRepository pointRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        return ResponseEntity.ok(quizService.getCategoriesWithScenes());
    }

    @PostMapping("/session")
    public ResponseEntity<?> createOrGetSession(
            @RequestParam Integer userId,
            @RequestParam Integer categoryId) {
        try {
            InteractiveQuizSession session = quizService.createOrGetSession(userId, categoryId);

            QuizSessionResponseDTO dto = new QuizSessionResponseDTO();
            dto.setId(session.getId());
            dto.setUserId(session.getUserId());
            dto.setCategoryId(session.getCategoryId());
            dto.setCurrentSceneId(session.getCurrentSceneId());
            dto.setCurrentPointId(session.getCurrentPointId());
            dto.setAnsweredPoints(session.getAnsweredPoints());
            dto.setStartedAt(session.getStartedAt());
            dto.setLastUpdated(session.getLastUpdated());
            dto.setIsCompleted(session.getIsCompleted());
            dto.setCompletedAt(session.getCompletedAt());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/question")
    public ResponseEntity<?> getQuestion(
            @RequestParam Integer pointId,
            @RequestParam Integer categoryId,
            @RequestParam Integer sessionId) {
        try {
            Map<String, Object> question = quizService.getQuestion(pointId, categoryId, sessionId);
            return ResponseEntity.ok(question);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/answer")
    public ResponseEntity<?> submitAnswer(@RequestBody AnswerRequestDTO request) {
        try {
            Map<String, Object> result = quizService.submitAnswer(
                    request.getSessionId(),
                    request.getPointId(),
                    request.getSelectedAnswer(),
                    request.getUserId(),
                    request.getOptions()
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/session/reset")
    public ResponseEntity<?> resetSession(@RequestParam Integer userId, @RequestParam Integer categoryId) {
        Optional<InteractiveQuizSession> session = sessionRepository
                .findByUserIdAndCategoryIdAndIsCompletedFalse(userId, categoryId);
        if (session.isPresent()) {
            sessionRepository.delete(session.get());
        }
        return ResponseEntity.ok(Map.of("message", "Session đã được reset"));
    }

    @GetMapping("/categories-with-status")
    public ResponseEntity<?> getCategoriesWithStatus(@RequestParam Integer userId) {
        List<Categories> categories = quizRepository.findCategoriesWithScenes();

        List<Map<String, Object>> result = categories.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("name", c.getCategoryName());
            map.put("iconUrl", c.getIconUrl());
            map.put("thumbnailUrl", c.getThumbnailUrl());
            map.put("jlptLevel", c.getJlptLevel());

            boolean isCompleted = sessionRepository.existsByUserIdAndCategoryIdAndIsCompletedTrue(userId, c.getId());
            map.put("isCompleted", isCompleted);

            Optional<InteractiveQuizSession> session = sessionRepository
                    .findByUserIdAndCategoryIdAndIsCompletedFalse(userId, c.getId());

            int progressPercent = 0;
            if (session.isPresent()) {
                int answeredCount = parseAnsweredPoints(session.get().getAnsweredPoints()).size();
                int totalPoints = getTotalPointsInCategory(c.getId());
                progressPercent = totalPoints > 0 ? (answeredCount * 100 / totalPoints) : 0;
            } else if (isCompleted) {
                progressPercent = 100;
            }
            map.put("progressPercent", progressPercent);

            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    private Set<Integer> parseAnsweredPoints(String answeredPointsJson) {
        Set<Integer> result = new HashSet<>();
        if (answeredPointsJson == null || answeredPointsJson.isEmpty()) return result;
        try {
            List<Integer> list = objectMapper.readValue(answeredPointsJson, new TypeReference<List<Integer>>() {
            });
            result.addAll(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private Integer getTotalPointsInCategory(Integer categoryId) {
        List<InteractiveScene> scenes = sceneRepository.findActiveScenesByCategory(categoryId);
        int total = 0;
        for (InteractiveScene scene : scenes) {
            total += pointRepository.findBySceneId(scene.getId()).size();
        }
        return total;
    }

    private Map<Integer, Map<String, Object>> parseAnsweredQuestions(String json) {
        if (json == null || json.isEmpty()) return new HashMap<>();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<Integer, Map<String, Object>>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    @GetMapping("/session/answers/{sessionId}")
    public ResponseEntity<?> getSessionAnswers(@PathVariable Integer sessionId) {
        Optional<InteractiveQuizSession> sessionOpt = sessionRepository.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("answeredPoints", parseAnsweredPoints(sessionOpt.get().getAnsweredPoints()));
        result.put("answeredQuestions", parseAnsweredQuestions(sessionOpt.get().getAnsweredQuestions()));

        return ResponseEntity.ok(result);
    }
}