package com.example.backend.Repository;

import com.example.backend.Entity.EtatPoste;
import com.example.backend.Entity.ManagerService;
import com.example.backend.Entity.Poste;
import com.example.backend.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PosteRepository extends JpaRepository<Poste,Long> {
    List<Poste> findAllByManagerService(ManagerService managerService);
    List<Poste> findByPoste(EtatPoste etat);
    @Query("SELECT p.managerService.manager FROM Poste p WHERE p.id = :postId")
    User findManagerByPostId(@Param("postId") Long postId);



}
