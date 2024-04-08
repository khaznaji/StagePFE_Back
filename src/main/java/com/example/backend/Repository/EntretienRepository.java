package com.example.backend.Repository;

import com.example.backend.Entity.Entretien;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EntretienRepository extends JpaRepository<Entretien,Long> {
    List<Entretien> findByPosteId(Long posteId);
    List<Entretien> findByCandidature_Collaborateur_Id(Long collaborateurId);

}
