package com.example.backend.Service;

import com.example.backend.Entity.Collaborateur;
import com.example.backend.Entity.Competence;
import com.example.backend.Entity.Evaluation;
import com.example.backend.Repository.EvaluationRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class EvaluationService  {
    @Autowired
    private EvaluationRepository evaluationRepository;


    public Evaluation save(Evaluation evaluation) {
        return evaluationRepository.save(evaluation);
    }
}
