package com.example.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/quiz")
public class QuizController {

    /**
     * Trang chủ quiz
     */
    @GetMapping
    public String quizHome() {
        return "quiz/index";
    }

    /**
     * Interactive Quiz - Học từ vựng với hình ảnh tương tác
     */
    @GetMapping("/interactive")
    public String interactiveQuiz() {
        return "quiz/interactive_quiz";
    }

    /**
     * Flashcard Quiz - Học từ vựng với flashcard
     */
    @GetMapping("/flashcard")
    public String flashcardQuiz() {
        return "quiz/flashcard_quiz";
    }

    /**
     * Multiple Choice Quiz - Trắc nghiệm từ vựng
     */
    @GetMapping("/multiple-choice")
    public String multipleChoiceQuiz() {
        return "quiz/multiple_choice_quiz";
    }

    /**
     * Listening Quiz - Nghe và chọn đáp án
     */
    @GetMapping("/listening")
    public String listeningQuiz() {
        return "quiz/listening_quiz";
    }

    /**
     * Writing Quiz - Viết từ vựng
     */
    @GetMapping("/writing")
    public String writingQuiz() {
        return "quiz/writing_quiz";
    }
}