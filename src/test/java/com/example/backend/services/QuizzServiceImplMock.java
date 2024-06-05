package com.example.backend.services;

import com.example.backend.Entity.Candidature;
import com.example.backend.Entity.Quiz;
import com.example.backend.Repository.CandidatureRepository;
import com.example.backend.Repository.QuizRepository;
import com.example.backend.Service.QuizServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public  class QuizzServiceImplMock {


    @Mock
    private QuizRepository quizRepository;

    @Mock
    private CandidatureRepository candidatureRepository;

    @InjectMocks
    private QuizServiceImpl quizService;

    private Quiz quiz;
    private Candidature candidature;

    @BeforeEach
    void setUp() {
        // Créer un objet Quiz pour les tests
        quiz = new Quiz();
        quiz.setQid(1L);
        quiz.setTitle("Quiz de test");
        quiz.setActive(true);

        // Créer un objet Candidature pour les tests
        candidature = new Candidature();
        candidature.setId(1L);
        candidature.setScore(0);
    }

    @Test
    void testAddQuiz() {
        // Arrange
        when(quizRepository.save(quiz)).thenReturn(quiz);

        // Act
        Quiz result = quizService.addQuiz(quiz);

        // Assert
        assertEquals(quiz, result);
    }

    @Test
    void testGetQuizById() {
        // Arrange
        Long quizId = 1L;
        when(quizRepository.findById(quizId)).thenReturn(java.util.Optional.ofNullable(quiz));

        // Act
        Quiz result = quizService.getQuizById(quizId);

        // Assert
        assertEquals(quiz, result);
    }

    @Test
    void testSoumettreQuiz() {
        // Arrange
        int score = 80;

        // Act
        quizService.soumettreQuiz(candidature, score);

        // Assert
        assertEquals(score, candidature.getScore());
        verify(candidatureRepository).save(candidature);
    }
}
