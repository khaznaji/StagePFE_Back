package com.example.backend.Repository;

import com.example.backend.Entity.Collaborateur;
import com.example.backend.Entity.Formation;
import com.example.backend.Entity.ParticipationFormation;
import com.example.backend.Entity.Poste;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipationFormationRepository extends JpaRepository<ParticipationFormation,Long> {
    List<ParticipationFormation> findByCollaborateur(Collaborateur collaborateur);
    ParticipationFormation findByCollaborateurAndFormation(Collaborateur collaborateur, Formation formation);


}
