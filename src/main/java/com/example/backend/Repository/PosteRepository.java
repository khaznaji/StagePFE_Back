package com.example.backend.Repository;

import com.example.backend.Entity.Poste;
import com.example.backend.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PosteRepository extends JpaRepository<Poste,Long> {
    List<Poste> findByApprouveParManagerRHTrue();

}
