package com.example.backend.Repository;

import com.example.backend.Entity.Entretien;
import com.example.backend.Entity.ManagerService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EntretienRepository extends JpaRepository<Entretien,Long> {
    List<Entretien> findByPosteId(Long posteId);
    List<Entretien> findByCandidature_Collaborateur_Id(Long collaborateurId);
    Optional<Entretien> findByCandidatureId(Long candidatureId);
    List<Entretien> findByManagerServiceId(Long managerServiceId);
    List<Entretien> findByCollaborateurs_Id(Long collaborateurId);

}
