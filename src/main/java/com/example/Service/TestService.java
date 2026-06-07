package com.example.Service;

import com.example.Dto.Request.CreateTestDTO;
import com.example.Dto.Response.TestResponseDTO;
import com.example.Dto.Response.TestResultDTO;
import com.example.Entity.*;
import com.example.Repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TestService {

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private TestQuestionsRepository testQuestionRepository;

    @Autowired
    private UserTestResultRepository userTestResultRepository;

    @Autowired
    private VocabularyRepository vocabularyRepository;

    @Autowired
    private CategoriesRepository categoriesRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private UserTestBestRepository userTestBestRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== ADMIN: TẠO TEST ====================
    public TestResponseDTO createTest(CreateTestDTO dto) throws Exception {
        // Kiểm tra category tồn tại
        Categories category = categoriesRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chủ đề"));

        // Tạo test mới
        Tests test = new Tests();
        test.setCategoryId(dto.getCategoryId());
        test.setTitle(dto.getTitle());
        test.setDurationMinutes(dto.getDurationMinutes());
        test.setMaxScore(dto.getMaxScore());
        test.setPassScore(dto.getPassScore());
        test.setQuestionCount(dto.getQuestionCount());
        test.setIsActive(true);
        test.setCreatedAt(LocalDateTime.now());

        test = testRepository.save(test);

        generateQuestionsForTest(test.getId(), dto.getCategoryId(), dto.getQuestionCount(), dto.getQuestionType());


        // Cập nhật số lượng câu hỏi thực tế
        Integer actualCount = testQuestionRepository.countByTestId(test.getId());
        test.setQuestionCount(actualCount);
        test = testRepository.save(test);

        return convertToResponseDTO(test, category.getCategoryName());
    }

    // Tự động sinh câu hỏi (có thể random 2 dạng)
    private void generateQuestionsForTest(Integer testId, Integer categoryId, Integer questionCount, String questionType) {
        List<Vocabulary> vocabs = vocabularyRepository.findByCategoryId(categoryId);

        if (vocabs.isEmpty()) {
            throw new RuntimeException("Chủ đề không có từ vựng nào");
        }

        int numberOfQuestions = Math.min(questionCount, vocabs.size());
        Collections.shuffle(vocabs);
        List<Vocabulary> selectedVocabs = vocabs.subList(0, numberOfQuestions);

        int order = 0;
        Random random = new Random();

        for (Vocabulary vocab : selectedVocabs) {
            TestQuestions question = new TestQuestions();
            question.setTestId(testId);
            question.setVocabId(vocab.getId());

            // Xác định dạng câu hỏi
            int questionTypeInt;
            if ("meaning".equals(questionType)) {
                questionTypeInt = 0; // Chỉ hỏi nghĩa
            } else if ("word".equals(questionType)) {
                questionTypeInt = 1; // Chỉ hỏi từ
            } else {
                questionTypeInt = random.nextInt(2); // Hỗn hợp
            }

            String displayText = vocab.getExpression();
            if (vocab.getKana() != null && !vocab.getKana().isEmpty()) {
                displayText = vocab.getExpression() + "（" + vocab.getKana() + "）";
            }

            if (questionTypeInt == 0) {
                // Dạng 1: Cho từ, hỏi nghĩa
                question.setQuestionText("Từ \"" + displayText + "\" có nghĩa là gì?");
                question.setCorrectAnswer(vocab.getMeaning());

                List<String> options = new ArrayList<>();
                options.add(vocab.getMeaning());

                List<Vocabulary> otherVocabs = vocabs.stream()
                        .filter(v -> !v.getId().equals(vocab.getId()))
                        .collect(Collectors.toList());
                Collections.shuffle(otherVocabs);

                for (int i = 0; i < Math.min(3, otherVocabs.size()); i++) {
                    options.add(otherVocabs.get(i).getMeaning());
                }

                while (options.size() < 4) {
                    options.add("???");
                }
                Collections.shuffle(options);

                try {
                    question.setOptions(objectMapper.writeValueAsString(options));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Lỗi khi tạo options: " + e.getMessage());
                }
            } else {
                // Dạng 2: Cho nghĩa, hỏi từ
                question.setQuestionText("Từ nào có nghĩa là \"" + vocab.getMeaning() + "\"?");
                question.setCorrectAnswer(displayText);

                List<String> options = new ArrayList<>();
                options.add(displayText);

                List<Vocabulary> otherVocabs = vocabs.stream()
                        .filter(v -> !v.getId().equals(vocab.getId()))
                        .collect(Collectors.toList());
                Collections.shuffle(otherVocabs);

                for (int i = 0; i < Math.min(3, otherVocabs.size()); i++) {
                    Vocabulary other = otherVocabs.get(i);
                    String otherText = other.getExpression();
                    if (other.getKana() != null && !other.getKana().isEmpty()) {
                        otherText = other.getExpression() + "（" + other.getKana() + "）";
                    }
                    options.add(otherText);
                }

                while (options.size() < 4) {
                    options.add("???");
                }
                Collections.shuffle(options);

                try {
                    question.setOptions(objectMapper.writeValueAsString(options));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Lỗi khi tạo options: " + e.getMessage());
                }
            }

            question.setOrderIndex(order++);
            testQuestionRepository.save(question);
        }
    }

    // ==================== USER: LẤY DANH SÁCH TEST ====================
    public List<TestResponseDTO> getTestsByCategory(Integer categoryId, Integer userId) {
        List<Tests> tests = testRepository.findByCategoryIdAndIsActiveTrue(categoryId);

        return tests.stream().map(test -> {
            String categoryName = categoriesRepository.findById(test.getCategoryId())
                    .map(Categories::getCategoryName)
                    .orElse("");
            TestResponseDTO dto = convertToResponseDTO(test, categoryName);

            // Kiểm tra user đã pass test này chưa
            boolean hasPassed = userTestResultRepository.hasUserPassedTest(userId, test.getId());
            dto.setHasPassed(hasPassed);
            int bestScore = userTestBestRepository.findBestScoreByUserAndTest(userId, test.getId()).orElse(0);
            dto.setBestScore(bestScore);

            return dto;
        }).collect(Collectors.toList());
    }

    // ==================== USER: BẮT ĐẦU LÀM TEST ====================
    public Map<String, Object> startTest(Integer testId, Integer userId) throws Exception {
        Tests test = testRepository.findByIdAndIsActiveTrue(testId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài test"));

        List<UserTestResults> incompleteAttempts = userTestResultRepository
                .findByUserIdAndTestIdAndCompletedAtIsNull(userId, testId);
        if (!incompleteAttempts.isEmpty()) {
            userTestResultRepository.deleteAll(incompleteAttempts);
            System.out.println("Đã xóa " + incompleteAttempts.size() + " attempt chưa hoàn thành");
        }

        // Lấy danh sách câu hỏi
        List<TestQuestions> questions = testQuestionRepository.findByTestIdOrderByOrderIndexAsc(testId);

        // Tạo attempt mới
        UserTestResults attempt = new UserTestResults();
        attempt.setUserId(userId);
        attempt.setTestId(testId);
        attempt.setStartedAt(LocalDateTime.now());
        attempt.setTotalCount(questions.size());
        attempt.setCorrectCount(0);
        attempt.setScore(0f);
        attempt.setIsPassed(false);

        attempt = userTestResultRepository.save(attempt);

        // Chuẩn bị câu hỏi cho frontend (ẩn đáp án đúng)
        List<Map<String, Object>> questionList = new ArrayList<>();
        for (TestQuestions q : questions) {
            Map<String, Object> qMap = new HashMap<>();
            qMap.put("id", q.getId());
            qMap.put("questionText", q.getQuestionText());

            // ✅ SỬA: Thêm try-catch cho readValue
            try {
                qMap.put("options", objectMapper.readValue(q.getOptions(), List.class));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Lỗi khi đọc options: " + e.getMessage());
            }

            qMap.put("orderIndex", q.getOrderIndex());
            questionList.add(qMap);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("attemptId", attempt.getId());
        result.put("testId", testId);
        result.put("testTitle", test.getTitle());
        result.put("durationMinutes", test.getDurationMinutes());
        result.put("maxScore", test.getMaxScore());
        result.put("questionCount", questions.size());
        result.put("questions", questionList);

        return result;
    }

    // ==================== USER: NỘP BÀI ====================
    public TestResultDTO submitTest(Integer attemptId, List<Integer> selectedAnswers) throws Exception {
        UserTestResults attempt = userTestResultRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy attempt"));

        if (attempt.getCompletedAt() != null) {
            throw new RuntimeException("Bài test đã được nộp trước đó");
        }

        Tests test = testRepository.findById(attempt.getTestId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài test"));

        List<TestQuestions> questions = testQuestionRepository.findByTestIdOrderByOrderIndexAsc(test.getId());

        if (selectedAnswers.size() != questions.size()) {
            throw new RuntimeException("Số lượng câu trả lời không khớp");
        }

        // Tính điểm
        int correctCount = 0;
        List<Map<String, Object>> answersData = new ArrayList<>();

        for (int i = 0; i < questions.size(); i++) {
            TestQuestions q = questions.get(i);
            int selected = selectedAnswers.get(i);

            List<String> options;
            try {
                options = objectMapper.readValue(q.getOptions(), List.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Lỗi khi đọc options: " + e.getMessage());
            }

            boolean isCorrect = false;
            String selectedAnswerText = "";

            if (selected >= 0 && selected < options.size()) {
                selectedAnswerText = options.get(selected);
                isCorrect = selectedAnswerText.equals(q.getCorrectAnswer());
            } else {
                selectedAnswerText = "(Chưa trả lời)";
                isCorrect = false;
            }

            if (isCorrect) {
                correctCount++;
            }

            Map<String, Object> answerDetail = new HashMap<>();
            answerDetail.put("questionId", q.getId());
            answerDetail.put("selectedIndex", selected);
            answerDetail.put("selectedAnswer", selectedAnswerText);
            answerDetail.put("isCorrect", isCorrect);
            answerDetail.put("correctAnswer", q.getCorrectAnswer());
            answersData.add(answerDetail);
        }

        int totalCount = questions.size();
        int newScore = (correctCount * test.getMaxScore()) / totalCount;
        boolean isPassed = newScore >= test.getPassScore();

        // ========== LẤY ĐIỂM CAO NHẤT ĐÃ ĐẠT (CHỈ TÍNH ĐIỂM PASS) ==========
        int currentBestPassed = userTestResultRepository.findBestPassedScoreByUserAndTest(attempt.getUserId(), test.getId()).orElse(0);

        int pointsEarned = 0;
        boolean isNewBest = newScore > currentBestPassed;

        // ✅ CHỈ CỘNG ĐIỂM NẾU ĐẠT (PASS)
        if (isPassed) {
            if (currentBestPassed == 0) {
                // Lần đầu tiên đạt → cộng TOÀN BỘ điểm
                pointsEarned = newScore;
                System.out.println("✅ Lần đầu đạt! Cộng " + pointsEarned + " điểm cho user");
            } else if (isNewBest) {
                // Đã đạt trước đó, lần này cao hơn → cộng chênh lệch
                pointsEarned = newScore - currentBestPassed;
                System.out.println("✅ Cộng chênh lệch " + pointsEarned + " điểm cho user (từ " + currentBestPassed + " lên " + newScore + ")");
            }

            // Cộng điểm vào total_points
            if (pointsEarned > 0) {
                Users user = usersRepository.findById(attempt.getUserId()).orElse(null);
                if (user != null) {
                    user.setTotalPoints(user.getTotalPoints() + pointsEarned);
                    usersRepository.save(user);
                }
            }

            // Cập nhật bảng User_Test_Best (chỉ lưu điểm cao nhất đã pass)
            if (isNewBest) {
                Optional<UserTestBest> existingBest = userTestBestRepository
                        .findByUserIdAndTestId(attempt.getUserId(), test.getId());

                if (existingBest.isPresent()) {
                    UserTestBest userTestBest = existingBest.get();
                    userTestBest.setBestScore(newScore);
                    userTestBest.setUpdatedAt(LocalDateTime.now());
                    userTestBestRepository.save(userTestBest);
                } else {
                    UserTestBestId bestId = new UserTestBestId(attempt.getUserId(), test.getId());
                    UserTestBest userTestBest = new UserTestBest();
                    userTestBest.setId(bestId);
                    userTestBest.setBestScore(newScore);
                    userTestBest.setUpdatedAt(LocalDateTime.now());
                    userTestBestRepository.save(userTestBest);
                }
            }
        } else {
            System.out.println("⚠️ Chưa đạt (" + newScore + "/" + test.getPassScore() + "), không cộng điểm");

            // ✅ VẪN CẬP NHẬT best_score nếu điểm mới cao hơn (dù chưa pass)
            // Để lần sau nếu pass sẽ tính chênh lệch đúng
            if (isNewBest) {
                Optional<UserTestBest> existingBest = userTestBestRepository
                        .findByUserIdAndTestId(attempt.getUserId(), test.getId());

                if (existingBest.isPresent()) {
                    UserTestBest userTestBest = existingBest.get();
                    userTestBest.setBestScore(newScore);
                    userTestBest.setUpdatedAt(LocalDateTime.now());
                    userTestBestRepository.save(userTestBest);
                } else {
                    UserTestBestId bestId = new UserTestBestId(attempt.getUserId(), test.getId());
                    UserTestBest userTestBest = new UserTestBest();
                    userTestBest.setId(bestId);
                    userTestBest.setBestScore(newScore);
                    userTestBest.setUpdatedAt(LocalDateTime.now());
                    userTestBestRepository.save(userTestBest);
                }
                System.out.println("📝 Cập nhật best_score (dù chưa pass): " + currentBestPassed + " -> " + newScore);
            }
        }

        // ========== CẬP NHẬT ATTEMPT ==========
        attempt.setCompletedAt(LocalDateTime.now());
        attempt.setScore((float) newScore);
        attempt.setCorrectCount(correctCount);
        attempt.setIsPassed(isPassed);
        attempt.setDurationSeconds((int) ChronoUnit.SECONDS.between(attempt.getStartedAt(), attempt.getCompletedAt()));

        try {
            attempt.setAnswersData(objectMapper.writeValueAsString(answersData));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Lỗi khi lưu answers data: " + e.getMessage());
        }

        userTestResultRepository.save(attempt);

        // Chuẩn bị kết quả trả về
        TestResultDTO result = new TestResultDTO();
        result.setTestId(test.getId());
        result.setTestTitle(test.getTitle());
        result.setScore(newScore);
        result.setMaxScore(test.getMaxScore());
        result.setPassScore(test.getPassScore().intValue());
        result.setCorrectCount(correctCount);
        result.setTotalCount(totalCount);
        result.setDurationSeconds(attempt.getDurationSeconds());
        result.setIsPassed(isPassed);
        result.setPointsEarned(pointsEarned);
        result.setIsNewBest(isNewBest);
        result.setCompletedAt(attempt.getCompletedAt());

        System.out.println("=== KẾT QUẢ ===");
        System.out.println("Điểm mới: " + newScore);
        System.out.println("Điểm cao nhất đã pass cũ: " + currentBestPassed);
        System.out.println("Điểm cộng thêm: " + pointsEarned);
        System.out.println("IsNewBest: " + isNewBest);
        System.out.println("IsPassed: " + isPassed);

        return result;
    }

    // ==================== HELPER ====================
    private TestResponseDTO convertToResponseDTO(Tests test, String categoryName) {
        TestResponseDTO dto = new TestResponseDTO();
        dto.setId(test.getId());
        dto.setCategoryId(test.getCategoryId());
        dto.setCategoryName(categoryName);
        dto.setTitle(test.getTitle());
        dto.setDurationMinutes(test.getDurationMinutes());
        dto.setMaxScore(test.getMaxScore());
        dto.setPassScore(test.getPassScore());
        dto.setQuestionCount(test.getQuestionCount());
        dto.setIsActive(test.getIsActive());
        dto.setCreatedAt(test.getCreatedAt());
        return dto;
    }
}