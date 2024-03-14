package com.example.backend.Repository;

import com.example.backend.Entity.Collaborateur;
import com.example.backend.Entity.ManagerService;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CollaborateurRepository  extends JpaRepository<Collaborateur,Long> {
    @Query("SELECT c FROM Collaborateur c LEFT JOIN FETCH c.managerService LEFT JOIN FETCH c.collaborateur LEFT JOIN FETCH c.competences WHERE c.id = :id")
    Optional<Collaborateur> findByIdWithAssociations(Long id);
    @Query("SELECT c FROM Collaborateur c WHERE c.collaborateur.id = :userId")
    Optional<Collaborateur> findByCollaborateurUserId(@Param("userId") Long userId);
}
