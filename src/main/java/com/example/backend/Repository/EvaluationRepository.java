package com.example.backend.Repository;

import com.example.backend.Entity.Collaborateur;
import com.example.backend.Entity.Competence;
import com.example.backend.Entity.Evaluation;
import com.example.backend.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation,Long> {
    Optional<Evaluation> findByCollaborateurAndCompetence(Collaborateur collaborateur, Competence competence);
    Evaluation findByCompetence_IdAndCollaborateur_Id(Long competenceId, Long collaborateurId);
    List<Evaluation> findByCollaborateur(Collaborateur collaborateur);

}
