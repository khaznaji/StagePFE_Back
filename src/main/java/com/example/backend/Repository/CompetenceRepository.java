package com.example.backend.Repository;

import com.example.backend.Entity.Competence;
import com.example.backend.Entity.Domaine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompetenceRepository extends JpaRepository<Competence,Long> {
    List<Competence> findByDomaine(Domaine domaine);

}
