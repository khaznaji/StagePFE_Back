package com.example.backend.Repository;

import com.example.backend.Entity.Formateur;
import com.example.backend.Entity.ManagerService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FormateurRepository extends JpaRepository<Formateur,Long> {
    @Query("SELECT c FROM Formateur c WHERE c.fomarteur.id = :formateur_id")
    Optional<Formateur> findByFormateurFormateurId(@Param("formateur_id") Long formateur_id);
}
