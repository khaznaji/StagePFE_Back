package com.example.backend.Repository;

import com.example.backend.Entity.Quiz;
import com.example.backend.Entity.SessionFormation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionFormationRepository  extends JpaRepository<SessionFormation, Long> {
}
