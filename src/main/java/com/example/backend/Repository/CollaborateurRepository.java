package com.example.backend.Repository;

import com.example.backend.Entity.Collaborateur;
import com.example.backend.Entity.ManagerService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollaborateurRepository  extends JpaRepository<Collaborateur,Long> {
}
