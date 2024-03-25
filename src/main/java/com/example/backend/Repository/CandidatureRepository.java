package com.example.backend.Repository;

import com.example.backend.Entity.Candidature;
import com.example.backend.Entity.Collaborateur;
import com.example.backend.Entity.EtatPostulation;
import com.example.backend.Entity.Poste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CandidatureRepository extends JpaRepository<Candidature,Long> {
    boolean existsByCollaborateurAndPoste(Collaborateur collaborateur, Poste poste);
    List<Candidature> findByPoste(Poste poste);
    Long countByPosteAndEtat(Poste poste, EtatPostulation etat);
    List<Candidature> findByPoste_Id(Long posteId);

    Long countByPosteAndEtatIsNot(Poste poste, EtatPostulation etat);

    Long countByPoste(Poste poste);
    Candidature findByCollaborateurIdAndPosteId(Long collaborateurId, Long posteId);

}
