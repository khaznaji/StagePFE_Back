package com.example.backend.Controller;

import com.example.backend.Entity.Collaborateur;
import com.example.backend.Entity.Poste;
import com.example.backend.Entity.Quiz;
import com.example.backend.Repository.PosteRepository;
import com.example.backend.Repository.QuizRepository;
import com.example.backend.Service.QuizService;
import javafx.geometry.Pos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/quizz")
public class QuizController {
    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private QuizService quizService;
    @Autowired
    private PosteRepository posteRepository;

    @PostMapping("/add/{postId}")
    public ResponseEntity<?> addQuizToPoste(@PathVariable(value = "postId") Long postId, @RequestBody Quiz quiz) {
        return posteRepository.findById(postId).map(poste -> {
            quiz.setPoste(poste);
            quiz.setMaxMarks("10");
            quizRepository.save(quiz);
            Map<String, String> response = new HashMap<>();
            response.put("success", "Quiz added successfully to Poste with ID: " + postId);
            return ResponseEntity.ok(response);
        }).orElseGet(() -> {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to add Quiz: Poste with ID " + postId + " not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        });
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<Quiz> getQuizById(@PathVariable Long quizId) {
        return quizRepository.findById(quizId)
                .map(quiz -> new ResponseEntity<>(quiz, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/getall")
    public ResponseEntity<?> getQuizzes() {
        return ResponseEntity.ok(quizService.getQuizzes());
    }

    @PutMapping("/update/{quizId}")
    public ResponseEntity<?> updateQuiz(@PathVariable(value = "quizId") Long quizId, @RequestBody Quiz updatedQuiz) {
        return quizRepository.findById(quizId).map(quiz -> {
            quiz.setTitle(updatedQuiz.getTitle());
            quiz.setDescription(updatedQuiz.getDescription());
            quiz.setMaxMarks("10");
            quizRepository.save(quiz);
            return new ResponseEntity<>("Quiz updated successfully.", HttpStatus.OK);
        }).orElseGet(() -> new ResponseEntity<>("Quiz with ID: " + quizId + " not found.", HttpStatus.NOT_FOUND));
    }



    @DeleteMapping("/delete/{qid}")
    public void deleteQuiz(@PathVariable("qid") Long qid) {
        quizService.deleteQuiz(qid);
    }


    @GetMapping("/poste/{postId}")
    public ResponseEntity<List<Quiz>> getQuizzesByPostId(@PathVariable Long postId) {
        List<Quiz> quizzes = quizService.getQuizzesByPostId(postId);
        if (quizzes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(quizzes);
    }

    @GetMapping("/active")
    public List<Quiz> getActiveQuizzes() {
        return quizService.getActiveQuizzes();
    }

    @GetMapping("/poste/active/{cid}")
    public List<Quiz> getActiveQuizzesOfCategory(@PathVariable("cid") Long cid) {
        Poste poste = new Poste();
        poste.setId(cid);
        return quizService.getActiveQuizzesOfPoste(poste);
    }

}
