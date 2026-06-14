package com.example.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/quiz")
public class QuizController {


    @GetMapping
    public String quizHome() {
        return "quiz/index";
    }

    @GetMapping("/interactive")
    public String interactiveQuiz() {
        return "quiz/interactive_quiz";
    }

    @GetMapping("/flashcard")
    public String flashcardQuiz() {
        return "quiz/flashcard_quiz";
    }


}