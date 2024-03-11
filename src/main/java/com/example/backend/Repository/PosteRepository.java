package com.example.backend.Repository;

import com.example.backend.Entity.Poste;
import com.example.backend.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PosteRepository extends JpaRepository<Poste,Long> {
}
