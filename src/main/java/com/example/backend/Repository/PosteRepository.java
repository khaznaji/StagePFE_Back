package com.example.backend.Repository;

import com.example.backend.Entity.EtatPoste;
import com.example.backend.Entity.ManagerService;
import com.example.backend.Entity.Poste;
import com.example.backend.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PosteRepository extends JpaRepository<Poste,Long> {
    List<Poste> findAllByManagerService(ManagerService managerService);
    List<Poste> findByPoste(EtatPoste etat);



}
