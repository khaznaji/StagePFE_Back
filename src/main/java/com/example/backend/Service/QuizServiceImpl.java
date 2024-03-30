package com.example.backend.Service;

import com.example.backend.Entity.Candidature;
import com.example.backend.Entity.Collaborateur;
import com.example.backend.Entity.Poste;
import com.example.backend.Entity.Quiz;
import com.example.backend.Repository.CandidatureRepository;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuizServiceImpl implements QuizService {

    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private CandidatureRepository candidatureRepository;

    @Override
    public Quiz addQuiz(Quiz quiz) {
        return this.quizRepository.save(quiz);
    }

    @Override
    public Quiz updateQuiz(Quiz quiz) {
        return quizRepository.save(quiz);
    }

    public Quiz getQuizById(Long quizId) {
        return quizRepository.findById(quizId).orElse(null);
    }

    @Override
    public Set<Quiz> getQuizzes() {
        return new HashSet<>(quizRepository.findAll());
    }

    @Override
    public Quiz getQuiz(Long qid) {
        return quizRepository.findById(qid).get();
    }

    @Override
    public void deleteQuiz(Long qid) {
        quizRepository.deleteById(qid);
    }

    @Override
    public List<Quiz> getQuizzesOfPoste(Poste poste) {
        return this.quizRepository.findByPoste(poste);
    }

    @Override
    public List<Quiz> getActiveQuizzes() {
        return quizRepository.findByActive(true);
    }

    public List<Quiz> getQuizzesByPostId(Long postId) {
        // Récupérer tous les quizzes
        List<Quiz> allQuizzes = quizRepository.findAll();

        // Filtrer les quizzes pour ne garder que ceux associés au poste avec l'ID spécifié
        return allQuizzes.stream()
                .filter(quiz -> quiz.getPoste() != null && quiz.getPoste().getId().equals(postId))
                .collect(Collectors.toList());
    }

    public List<Quiz> getActiveQuizzesOfPoste(Poste poste) {
        return quizRepository.findByPosteAndActive(poste, true);
    }
    public void soumettreQuiz(Candidature candidature, int score) {
        candidature.setScore(score);
        candidatureRepository.save(candidature);
    }
}