package com.example.backend.services;

import com.example.backend.Entity.Question;
import com.example.backend.Entity.Quiz;
import com.example.backend.Repository.QuestionRepository;
import com.example.backend.Service.QuestionServiceImpl;
import com.example.backend.Service.QuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when; // Assurez-vous d'importer cette classe statique

@ExtendWith(MockitoExtension.class)
public class QuestionServiceImplMock {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuizService quizService;

    @InjectMocks
    private QuestionServiceImpl questionService;
    @BeforeEach
    void setUp() {
        // Reset mock invocations before each test
        reset(questionRepository, quizService);
    }
    @Test
    void testAddQuestion() {
        // Créer une question de test
        Question question = new Question();

        // Simuler le comportement du repository
        when(questionRepository.save(question)).thenReturn(question);

        // Appeler la méthode à tester
        Question addedQuestion = questionService.addQuestion(question);

        // Vérifier si la question ajoutée est égale à la question de test
        assertEquals(question, addedQuestion);
    }

    @Test
    void testUpdateQuestion() {
        // Créer une question de test
        Question question = new Question();

        // Simuler le comportement du repository
        when(questionRepository.save(question)).thenReturn(question);

        // Appeler la méthode à tester
        Question updatedQuestion = questionService.updateQuestion(question);

        // Vérifier si la question mise à jour est égale à la question de test
        assertEquals(question, updatedQuestion);
    }
    @Test
    void testGetQuestions() {
        // Créer des questions de test
        Question question1 = new Question();
        Question question2 = new Question();
        Set<Question> questions = new HashSet<>(Arrays.asList(question1, question2));

        // Simuler le comportement du repository
// Simuler le comportement du repository
// Simuler le comportement du repository
        when(questionRepository.findAll()).thenReturn(new ArrayList<>(questions));

// Appeler la méthode à tester
        Set<Question> result = new HashSet<>(questionService.getQuestions());

        // Appeler la méthode à tester

        // Vérifier si le résultat est égal à l'ensemble de questions de test
        assertEquals(questions, result);
    }

    @Test
    void testGetQuestion() {
        // Créer une question de test
        Question question = new Question();
        Long questionId = 1L;

        // Simuler le comportement du repository
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));

        // Appeler la méthode à tester
        Question result = questionService.getQuestion(questionId);

        // Vérifier si le résultat est égal à la question de test
        assertEquals(question, result);
    }

    @Test
    void testGetQuestionsOfQuiz() {
        // Créer un quiz de test
        Quiz quiz = new Quiz();
        quiz.setQid(1L);

        // Créer des questions de test
        Question question1 = new Question();
        question1.setQuiz(quiz);
        Question question2 = new Question();
        question2.setQuiz(quiz);
        Set<Question> questions = new HashSet<>();
        questions.add(question1);
        questions.add(question2);

        // Simuler le comportement du repository
        when(questionRepository.findByQuiz(quiz)).thenReturn(questions);

        // Appeler la méthode à tester
        Set<Question> result = questionService.getQuestionsOfQuiz(quiz);

        // Vérifier si le résultat est égal à l'ensemble de questions de test
        assertEquals(questions, result);
    }

    @Test
    void testDeleteQuestion() {
        // Créer une question de test
        Question question = new Question();
        question.setQuesId(1L);
        Quiz quiz = new Quiz();
        question.setQuiz(quiz);

        // Simuler le comportement de la méthode getQuestion
        when(questionRepository.findById(question.getQuesId())).thenReturn(java.util.Optional.of(question));

        // Appeler la méthode à tester
        questionService.deleteQuestion(question.getQuesId());

        // Vérifier si la méthode delete a été appelée avec la question de test
        verify(questionRepository, times(1)).delete(question);

        // Vérifier si la méthode addQuiz a été appelée avec le quiz
        verify(quizService, times(1)).addQuiz(quiz);
    }

}
