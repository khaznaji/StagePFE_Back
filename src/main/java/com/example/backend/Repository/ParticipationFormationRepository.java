package com.example.backend.Repository;

import com.example.backend.Entity.ParticipationFormation;
import com.example.backend.Entity.Poste;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipationFormationRepository extends JpaRepository<ParticipationFormation,Long> {
}
