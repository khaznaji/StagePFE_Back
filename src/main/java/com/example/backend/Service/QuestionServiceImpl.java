package com.example.backend.Service;


import com.example.backend.Entity.Question;
import com.example.backend.Entity.Quiz;
import com.example.backend.Repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class QuestionServiceImpl implements QuestionService {
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private QuizService quizService;

    @Override
    public Question addQuestion(Question question) {
        return questionRepository.save(question);
    }

    @Override
    public Question updateQuestion(Question question) {
        return questionRepository.save(question);
    }

    @Override
    public Set<Question> getQuestions() {
        return new HashSet<>(questionRepository.findAll());
    }

    @Override
    public Question getQuestion(Long quesid) {
        return questionRepository.findById(quesid).get();
    }

    @Override
    public Set<Question> getQuestionsOfQuiz(Quiz quiz) {
        return questionRepository.findByQuiz(quiz);
    }

    @Override
    public void deleteQuestion(Long quesid) {
        Question question = getQuestion(quesid);
        if (question != null) {
            Quiz quiz = question.getQuiz();
            if (quiz != null) {
                quiz.decrementNumberOfQuestions();
                quizService.addQuiz(quiz);
            }
            questionRepository.delete(question);
        }
    }


}
