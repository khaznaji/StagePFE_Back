package com.example.backend.Repository;

import com.example.backend.Entity.BilanAnnuel;
import com.example.backend.Entity.Collaborateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface BilanAnnuelRepository extends JpaRepository<BilanAnnuel,Long> {
    List<BilanAnnuel> findByCollaborateur(Collaborateur collaborateur);
    List<BilanAnnuel> findByCollaborateurId(Long collaborateurId);

}
