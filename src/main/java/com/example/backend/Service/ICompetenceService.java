package com.example.backend.Service;

import com.example.backend.Entity.Competence;

import java.util.List;

public interface ICompetenceService {
    Competence addCompetence(Competence competence);

    Competence getCompetenceById(Long id);

    List<Competence> getAllCompetences();

    Competence updateCompetence(Long id, Competence newCompetence);

    void deleteCompetence(Long id);
}
