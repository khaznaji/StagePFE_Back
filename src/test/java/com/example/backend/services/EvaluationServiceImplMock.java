package com.example.backend.services;


import com.example.backend.Entity.Evaluation;
import com.example.backend.Repository.EvaluationRepository;
import com.example.backend.Service.EvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class EvaluationServiceImplMock {

    @Mock
    private EvaluationRepository evaluationRepository;

    @InjectMocks
    private EvaluationService evaluationService;

    private Evaluation evaluation;

    @BeforeEach
    public void setUp() {
        evaluation = new Evaluation();
        // Initialize evaluation with necessary fields
    }

    @Test
    public void testSaveEvaluation() {
        // Given
        when(evaluationRepository.save(any(Evaluation.class))).thenReturn(evaluation);

        // When
        Evaluation savedEvaluation = evaluationService.save(evaluation);

        // Then
        assertNotNull(savedEvaluation);
        verify(evaluationRepository, times(1)).save(evaluation);
    }
}

