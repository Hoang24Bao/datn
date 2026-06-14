package com.example.Service;

import com.example.Entity.*;
import com.example.Repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class InteractiveQuizService {

    @Autowired
    private InteractiveQuizRepository quizRepository;

    @Autowired
    private InteractiveSceneRepository sceneRepository;

    @Autowired
    private InteractivePointRepository pointRepository;

    @Autowired
    private VocabularyRepository vocabularyRepository;

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private InteractiveQuizSessionRepository sessionRepository;

    @Autowired
    private UsersRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Map<String, Object>> getCategoriesWithScenes() {
        List<Categories> categories = quizRepository.findCategoriesWithScenes();
        return categories.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("name", c.getCategoryName());
            map.put("iconUrl", c.getIconUrl());
            map.put("thumbnailUrl", c.getThumbnailUrl());
            map.put("jlptLevel", c.getJlptLevel());
            return map;
        }).collect(Collectors.toList());
    }

    public InteractiveQuizSession createOrGetSession(Integer userId, Integer categoryId) {
        Optional<InteractiveQuizSession> existing = sessionRepository
                .findByUserIdAndCategoryIdAndIsCompletedFalse(userId, categoryId);

        if (existing.isPresent()) {
            return existing.get();
        }

        InteractiveQuizSession session = new InteractiveQuizSession();
        session.setUserId(userId);
        session.setCategoryId(categoryId);
        session.setStartedAt(LocalDateTime.now());
        session.setLastUpdated(LocalDateTime.now());
        session.setIsCompleted(false);
        session.setAnsweredPoints("[]");
        session.setAnsweredQuestions("{}");

        List<InteractiveScene> scenes = sceneRepository.findActiveScenesByCategory(categoryId);
        if (!scenes.isEmpty()) {
            session.setCurrentSceneId(scenes.get(0).getId());
            List<InteractivePoint> points = pointRepository.findBySceneId(scenes.get(0).getId());
            if (!points.isEmpty()) {
                session.setCurrentPointId(points.get(0).getId());
            }
        }

        return sessionRepository.save(session);
    }

    public Map<String, Object> getQuestion(Integer pointId, Integer categoryId, Integer sessionId) {
        Optional<InteractiveQuizSession> sessionOpt = sessionRepository.findById(sessionId);
        if (sessionOpt.isPresent()) {
            Map<Integer, Map<String, Object>> answeredQuestions = parseAnsweredQuestions(sessionOpt.get().getAnsweredQuestions());
            if (answeredQuestions.containsKey(pointId)) {
                Map<String, Object> saved = answeredQuestions.get(pointId);
                saved.put("isAnswered", true);
                saved.put("pointId", pointId);
                saved.put("isCorrect", saved.get("isCorrect"));

                Optional<InteractivePoint> pointOpt = pointRepository.findById(pointId);
                if (pointOpt.isPresent()) {
                    Optional<InteractiveScene> sceneOpt = sceneRepository.findById(pointOpt.get().getSceneId());
                    sceneOpt.ifPresent(scene -> {
                        saved.put("imageUrl", scene.getImageUrl());
                        saved.put("coordX", pointOpt.get().getCoordX());
                        saved.put("coordY", pointOpt.get().getCoordY());
                        saved.put("width", pointOpt.get().getWidth());
                        saved.put("height", pointOpt.get().getHeight());
                    });
                }
                return saved;
            }
        }

        Optional<InteractivePoint> pointOpt = pointRepository.findById(pointId);
        if (pointOpt.isEmpty()) return null;

        InteractivePoint point = pointOpt.get();
        Optional<Vocabulary> correctVocabOpt = vocabularyRepository.findById(point.getVocabId());
        if (correctVocabOpt.isEmpty()) return null;

        Vocabulary correctVocab = correctVocabOpt.get();

        List<Object[]> randomVocabs = quizRepository.findRandomVocabByCategory(categoryId, point.getVocabId());

        List<Map<String, String>> optionsWithKana = new ArrayList<>();

        Map<String, String> correctOption = new HashMap<>();
        correctOption.put("expression", correctVocab.getExpression());
        correctOption.put("kana", correctVocab.getKana() != null ? correctVocab.getKana() : "");
        optionsWithKana.add(correctOption);

        int count = 0;
        for (Object[] v : randomVocabs) {
            if (count >= 3) break;
            Integer vocabId = (Integer) v[0];
            String expression = (String) v[1];

            Optional<Vocabulary> vocabOpt = vocabularyRepository.findById(vocabId);
            String kana = vocabOpt.map(Vocabulary::getKana).orElse("");

            Map<String, String> option = new HashMap<>();
            option.put("expression", expression);
            option.put("kana", kana);
            optionsWithKana.add(option);
            count++;
        }

        if (optionsWithKana.size() < 4) {
            List<String> fallbackExpressions = Arrays.asList("???", "???", "???");
            for (int i = optionsWithKana.size(); i < 4; i++) {
                Map<String, String> fallback = new HashMap<>();
                fallback.put("expression", fallbackExpressions.get(i - 1));
                fallback.put("kana", "");
                optionsWithKana.add(fallback);
            }
        }

        Collections.shuffle(optionsWithKana);

        Optional<InteractiveScene> sceneOpt = sceneRepository.findById(point.getSceneId());
        InteractiveScene scene = sceneOpt.orElse(null);

        Map<String, Object> result = new HashMap<>();
        result.put("pointId", pointId);
        result.put("correctExpression", correctVocab.getExpression());
        result.put("options", optionsWithKana);
        result.put("imageUrl", scene != null ? scene.getImageUrl() : null);
        result.put("coordX", point.getCoordX());
        result.put("coordY", point.getCoordY());
        result.put("width", point.getWidth());
        result.put("height", point.getHeight());
        result.put("isAnswered", false);

        return result;
    }

    public Map<String, Object> submitAnswer(Integer sessionId, Integer pointId,
                                            String selectedAnswer, Integer userId,
                                            List<Map<String, String>> options) {
        System.out.println("sessionId: " + sessionId);
        System.out.println("pointId: " + pointId);
        System.out.println("selectedAnswer: " + selectedAnswer);
        System.out.println("userId: " + userId);
        System.out.println("options: " + options);

        Optional<InteractiveQuizSession> sessionOpt = sessionRepository.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            throw new RuntimeException("Session không tồn tại");
        }
        InteractiveQuizSession session = sessionOpt.get();

        Optional<InteractivePoint> pointOpt = pointRepository.findById(pointId);
        if (pointOpt.isEmpty()) {
            throw new RuntimeException("Point không tồn tại");
        }
        InteractivePoint point = pointOpt.get();

        Optional<Vocabulary> vocabOpt = vocabularyRepository.findById(point.getVocabId());
        boolean isCorrect = vocabOpt.isPresent() && vocabOpt.get().getExpression().equals(selectedAnswer);
        Vocabulary vocab = vocabOpt.orElse(null);

        System.out.println("vocabId: " + point.getVocabId());
        System.out.println("vocab.expression: " + (vocab != null ? vocab.getExpression() : "NULL"));
        System.out.println("selectedAnswer: " + selectedAnswer);
        System.out.println("isCorrect: " + isCorrect);
        System.out.println("equals check: " + (vocab != null ? vocab.getExpression().equals(selectedAnswer) : "N/A"));
        System.out.println("vocab bytes: " + (vocab != null ? java.util.Arrays.toString(vocab.getExpression().getBytes(java.nio.charset.StandardCharsets.UTF_8)) : "NULL"));
        System.out.println("selected bytes: " + java.util.Arrays.toString(selectedAnswer.getBytes(java.nio.charset.StandardCharsets.UTF_8)));

        Map<Integer, Map<String, Object>> answeredQuestions = parseAnsweredQuestions(session.getAnsweredQuestions());
        Map<String, Object> questionDetail = new HashMap<>();
        questionDetail.put("selectedAnswer", selectedAnswer);
        questionDetail.put("options", options);
        questionDetail.put("isCorrect", isCorrect);
        questionDetail.put("correctAnswer", vocab != null ? vocab.getExpression() : "");
        questionDetail.put("pointId", pointId);
        answeredQuestions.put(pointId, questionDetail);

        try {
            session.setAnsweredQuestions(objectMapper.writeValueAsString(answeredQuestions));
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateMemoryLevel(userId, point.getVocabId(), isCorrect);

        updateAnsweredPoints(session, pointId);

        Map<String, Object> nextPoint = findNextPoint(session);

        Map<String, Object> result = new HashMap<>();
        result.put("isCorrect", isCorrect);
        result.put("correctAnswer", vocab != null ? vocab.getExpression() : "");
        result.put("message", isCorrect ? "Chính xác!" : "Sai rồi!");
        result.put("hasNext", nextPoint != null);
        result.put("nextPoint", nextPoint);

        if (nextPoint != null) {
            session.setCurrentSceneId((Integer) nextPoint.get("sceneId"));
            session.setCurrentPointId((Integer) nextPoint.get("pointId"));
        }

        if (nextPoint == null) {
            session.setIsCompleted(true);
            session.setCompletedAt(LocalDateTime.now());
            result.put("completed", true);
            result.put("totalPoints", getTotalPointsInCategory(session.getCategoryId()));
            result.put("answeredCount", getAnsweredCount(session));
        }

        session.setLastUpdated(LocalDateTime.now());
        sessionRepository.save(session);

        return result;
    }

    private List<String> getOptionsForPoint(Integer pointId, Integer categoryId) {
        Optional<InteractivePoint> pointOpt = pointRepository.findById(pointId);
        if (pointOpt.isEmpty()) return new ArrayList<>();

        Optional<Vocabulary> correctVocabOpt = vocabularyRepository.findById(pointOpt.get().getVocabId());
        if (correctVocabOpt.isEmpty()) return new ArrayList<>();

        Vocabulary correctVocab = correctVocabOpt.get();
        List<Object[]> randomVocabs = quizRepository.findRandomVocabByCategory(categoryId, pointOpt.get().getVocabId());

        List<String> options = new ArrayList<>();
        options.add(correctVocab.getExpression());

        int count = 0;
        for (Object[] v : randomVocabs) {
            if (count >= 3) break;
            options.add((String) v[1]);
            count++;
        }

        if (options.size() < 4) {
            List<String> fallbackOptions = Arrays.asList("???", "???", "???");
            for (int i = options.size(); i < 4; i++) {
                options.add(fallbackOptions.get(i - 1));
            }
        }

        Collections.shuffle(options);
        return options;
    }

    private void updateMemoryLevel(Integer userId, Integer vocabId, boolean isCorrect) {
        Optional<Progress> progressOpt = progressRepository.findByUserIdAndVocabId(userId, vocabId);
        Progress progress;

        if (progressOpt.isEmpty()) {
            progress = new Progress();
            Progress.ProgressId id = new Progress.ProgressId(userId, vocabId);
            progress.setId(id);
            progress.setMemoryLevel(isCorrect ? 2 : 1);
            progress.setIsLearned(false);
            progress.setLastReviewed(LocalDateTime.now());
        } else {
            progress = progressOpt.get();
            int currentLevel = progress.getMemoryLevel();
            if (isCorrect) {
                progress.setMemoryLevel(Math.min(currentLevel + 1, 5));
            } else {
                progress.setMemoryLevel(Math.max(currentLevel - 1, 1));
            }
            progress.setLastReviewed(LocalDateTime.now());
        }

        progressRepository.save(progress);
    }

    private Map<String, Object> findNextPoint(InteractiveQuizSession session) {
        List<InteractiveScene> scenes = sceneRepository.findActiveScenesByCategory(session.getCategoryId());
        Set<Integer> answeredPoints = parseAnsweredPoints(session.getAnsweredPoints());

        for (InteractiveScene scene : scenes) {
            List<InteractivePoint> points = pointRepository.findBySceneId(scene.getId());
            for (InteractivePoint point : points) {
                if (!answeredPoints.contains(point.getId())) {
                    Map<String, Object> next = new HashMap<>();
                    next.put("sceneId", scene.getId());
                    next.put("pointId", point.getId());
                    next.put("imageUrl", scene.getImageUrl());
                    next.put("coordX", point.getCoordX());
                    next.put("coordY", point.getCoordY());
                    next.put("width", point.getWidth());
                    next.put("height", point.getHeight());
                    return next;
                }
            }
        }
        return null;
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

    private Map<Integer, Map<String, Object>> parseAnsweredQuestions(String json) {
        if (json == null || json.isEmpty()) return new HashMap<>();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<Integer, Map<String, Object>>>() {
            });
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private void updateAnsweredPoints(InteractiveQuizSession session, Integer pointId) {
        Set<Integer> answered = parseAnsweredPoints(session.getAnsweredPoints());
        answered.add(pointId);
        try {
            session.setAnsweredPoints(objectMapper.writeValueAsString(new ArrayList<>(answered)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Integer getTotalPointsInCategory(Integer categoryId) {
        List<InteractiveScene> scenes = sceneRepository.findActiveScenesByCategory(categoryId);
        int total = 0;
        for (InteractiveScene scene : scenes) {
            total += pointRepository.findBySceneId(scene.getId()).size();
        }
        return total;
    }

    private Integer getAnsweredCount(InteractiveQuizSession session) {
        return parseAnsweredPoints(session.getAnsweredPoints()).size();
    }
}