package com.example.backend.Service;

import com.example.backend.Entity.Competence;
import com.example.backend.Entity.Domaine;
import com.example.backend.Repository.CompetenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service

public class CompetenceService implements ICompetenceService {

    private final CompetenceRepository competenceRepository;

    @Autowired
    public CompetenceService(CompetenceRepository competenceRepository) {
        this.competenceRepository = competenceRepository;
    }

    @Override
    public Competence addCompetence(Competence competence) {
        return competenceRepository.save(competence);
    }

    @Override
    public Competence getCompetenceById(Long id) {
        return competenceRepository.findById(id).orElse(null);
    }

    @Override
    public List<Competence> getAllCompetences() {
        return competenceRepository.findAll();
    }

    @Override
    public Competence updateCompetence(Long id, Competence newCompetence) {
        Optional<Competence> existingCompetence = competenceRepository.findById(id);
        if (existingCompetence.isPresent()) {
            Competence competence = existingCompetence.get();
            competence.setNom(newCompetence.getNom());
            competence.setDomaine(newCompetence.getDomaine());

            return competenceRepository.save(competence);
        }
        return null;
    }

    @Override
    public void deleteCompetence(Long id) {
        competenceRepository.deleteById(id);
    }
    @Override

    public List<Competence> getCompetencesByDomain(Domaine domaine) {
        return competenceRepository.findByDomaine(domaine);
    }}