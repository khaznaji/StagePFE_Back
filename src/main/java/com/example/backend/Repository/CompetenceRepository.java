package com.example.backend.Repository;

import com.example.backend.Entity.Competence;
import com.example.backend.Entity.Domaine;
import com.example.backend.Entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CompetenceRepository extends JpaRepository<Competence,Long> {
    List<Competence> findByDomaine(Domaine domaine);
    @Query("SELECT e FROM Evaluation e WHERE e.competence = :competence")
    List<Evaluation> findEvaluationsByCompetence(Competence competence);
}
