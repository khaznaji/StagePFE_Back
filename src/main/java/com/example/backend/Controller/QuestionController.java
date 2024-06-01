package com.example.backend.Controller;


import com.example.backend.Entity.Candidature;
import com.example.backend.Entity.Question;
import com.example.backend.Entity.Quiz;
import com.example.backend.Service.CandidatureService;
import com.example.backend.Service.QuestionService;
import com.example.backend.Service.QuizService;
import com.example.backend.exception.QuizNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


@Controller
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/question")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuizService quizService;
    @Autowired
    private CandidatureService candidatureService;

    @PostMapping("/add")
    public ResponseEntity<?> addQuestion(@RequestBody Question question) {
        Question question1 = questionService.addQuestion(question);
        return ResponseEntity.ok(question1);
    }

        @PutMapping("/update")
    public ResponseEntity<?> updateQuestion(@RequestBody Question question) {
        Question question1 = questionService.updateQuestion(question);
        return ResponseEntity.ok(question1);
    }
    @PostMapping("/add-to-quiz/{quizId}")
    public ResponseEntity<?> addQuestionToQuiz(@PathVariable Long quizId, @RequestBody Question question) {
        Quiz quiz = quizService.getQuizById(quizId);
        if (quiz != null) {
            question.setQuiz(quiz);
            Question savedQuestion = questionService.addQuestion(question);
            quiz.incrementNumberOfQuestions();
            quiz.setActive(quiz.isActive());
            quizService.addQuiz(quiz);  // Sauvegarder les changements dans le quiz
            return ResponseEntity.ok(savedQuestion);
        } else {
            throw new QuizNotFoundException("Quiz not found with ID: " + quizId);
        }
    }
    @GetMapping("/quiz/{qid}/candidature/{candidatureId}")
    public ResponseEntity<?> getQuestionsOfQuiz(@PathVariable("qid") Long qid , @PathVariable("candidatureId") Long candidatureId) {
        Quiz quiz = quizService.getQuiz(qid);
        Candidature candidature = candidatureService.getCandidature(candidatureId);

        Set<Question> questions = quiz.getQuestions();
        List<Question> list = new ArrayList<>(questions);
        if (list.size() > Integer.parseInt(quiz.getNumberOfQuestions())) {
            list = list.subList(0, Integer.parseInt(quiz.getNumberOfQuestions() + 1));
        }
        Collections.shuffle(list);
        return ResponseEntity.ok(list);
    }


    @GetMapping("/quiz/all/{qid}")
    public ResponseEntity<?> getQuestionsOfQuizAdmin(@PathVariable("qid") Long qid) {
        Quiz quiz=new Quiz();
        quiz.setQid(qid);
        Set<Question> questionsofQuiz=questionService.getQuestionsOfQuiz(quiz);
        return ResponseEntity.ok(questionsofQuiz);

    }

    @GetMapping("/getbyid/{quesid}")
    public ResponseEntity<Question> getQuestion(@PathVariable("quesid") Long quesid) {

        return ResponseEntity.ok(questionService.getQuestion(quesid));
    }

    @DeleteMapping("/delete/{quesid}")
    public void deleteQuestion(@PathVariable("quesid") Long quesid) {
        Question deletedQuestion = questionService.getQuestion(quesid);
        Quiz quiz = deletedQuestion.getQuiz();

        // Supprimer la question
        questionService.deleteQuestion(quesid);

        // Vérifier si le quiz n'a plus de questions
        if (quiz.getQuestions().isEmpty()) {
            // Si le quiz n'a plus de questions, définissez active sur false
            quiz.setActive(false);
            quizService.addQuiz(quiz); // Sauvegardez les modifications du quiz
        }
    }


}
