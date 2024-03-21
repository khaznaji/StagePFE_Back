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
    public Optional<Evaluation> getEvaluationByCollaborateurAndCompetence(Collaborateur collaborateur, Competence competence) {
        return evaluationRepository.findByCollaborateurAndCompetence(collaborateur, competence);
    }
    public void saveEvaluation(Evaluation evaluation) {
        evaluationRepository.save(evaluation);
    }
    public Evaluation findByCompetenceIdAndCollaborateurId(Long competenceId, Long collaborateurId) {
        return evaluationRepository.findByCompetence_IdAndCollaborateur_Id(competenceId, collaborateurId);
    }

    public Evaluation save(Evaluation evaluation) {
        return evaluationRepository.save(evaluation);
    }
}
