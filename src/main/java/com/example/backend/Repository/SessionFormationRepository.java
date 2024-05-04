package com.example.backend.Repository;

import com.example.backend.Entity.Quiz;
import com.example.backend.Entity.SessionFormation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionFormationRepository  extends JpaRepository<SessionFormation, Long> {
    List<SessionFormation> findByFormationId(Long formationId);
    List<SessionFormation> findByGroup_Id(Long groupId);

    List<SessionFormation> findByGroup_Formateur_Id(Long formateurId);

}
