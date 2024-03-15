package com.example.backend.Repository;

import com.example.backend.Entity.Candidature;
import com.example.backend.Entity.Collaborateur;
import com.example.backend.Entity.Poste;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidatureRepository extends JpaRepository<Candidature,Long> {
    boolean existsByCollaborateurAndPoste(Collaborateur collaborateur, Poste poste);

}
