package com.example.backend.Repository;

import com.example.backend.Entity.BilanAnnuel;
import com.example.backend.Entity.Collaborateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BilanAnnuelRepository extends JpaRepository<BilanAnnuel,Long> {
    List<BilanAnnuel> findByCollaborateur(Collaborateur collaborateur);
    List<BilanAnnuel> findByCollaborateurId(Long collaborateurId);

}
