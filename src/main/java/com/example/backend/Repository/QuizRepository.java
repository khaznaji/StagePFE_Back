package com.example.backend.Repository;

import com.example.backend.Entity.Poste;
import com.example.backend.Entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    public List<Quiz> findByPoste(Poste poste);

    public List<Quiz> findByActive(Boolean b);

    public List<Quiz> findByPosteAndActive(Poste poste, Boolean b);
}
