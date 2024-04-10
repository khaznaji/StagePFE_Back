package com.example.backend.Repository;

import com.example.backend.Entity.Entretien;
import com.example.backend.Entity.EntretienRh;
import com.example.backend.Entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EntretienRhRepository extends JpaRepository<EntretienRh,Long> {
    List<EntretienRh> findByCandidature_Collaborateur_Id(Long collaborateurId);
    List<EntretienRh> findByPosteId(Long posteId);
    Optional<EntretienRh> findByCandidatureId(Long candidatureId);
    List<EntretienRh> findByUser_Id(Long managerRhId);


}
