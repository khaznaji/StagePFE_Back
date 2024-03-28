package com.example.backend.Service;


import com.example.backend.Entity.Poste;
import com.example.backend.Entity.Quiz;

import java.util.List;
import java.util.Set;

public interface QuizService {
    public Quiz addQuiz(Quiz quiz);
    public Quiz getQuizById(Long quizId);

    public Quiz updateQuiz(Quiz quiz);

    public Set<Quiz> getQuizzes();

    public Quiz getQuiz(Long qid);

    public void deleteQuiz(Long qid);
    List<Quiz> getQuizzesByPostId(Long postId) ;

    public List<Quiz> getQuizzesOfPoste(Poste poste);

    public List<Quiz> getActiveQuizzes();

    public List<Quiz> getActiveQuizzesOfPoste(Poste poste);
}
