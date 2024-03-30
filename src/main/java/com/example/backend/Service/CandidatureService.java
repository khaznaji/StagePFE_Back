package com.example.backend.Service;

import com.example.backend.Entity.*;
import com.example.backend.Repository.CandidatureRepository;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.exception.CandidatureNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class CandidatureService {

    private final CandidatureRepository candidatureRepository;
    @Autowired
    private  CollaborateurRepository collaborateurRepository;


    @Autowired
    public CandidatureService(CandidatureRepository candidatureRepository) {
        this.candidatureRepository = candidatureRepository;
    }
    public Candidature getCandidature(Long candidatureId) {
        return candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new CandidatureNotFoundException("Candidature not found with id: " + candidatureId));
    }
    public Candidature modifierEtat(Long collaborateurId, Long posteId, EtatPostulation nouvelEtat) {
        Candidature candidature = candidatureRepository.findByCollaborateurIdAndPosteId(collaborateurId, posteId);
        if (candidature != null) {
            candidature.setEtat(nouvelEtat);
            return candidatureRepository.save(candidature);
        }
        // Gérer le cas où la candidature n'est pas trouvée
        return null;
    }
    public List<Candidature> getAllCandidatures() {
        return candidatureRepository.findAll();
    }
    public List<Candidature> getCandidaturesByPost(Long postId) {
        // Implémentez la logique pour récupérer les candidatures associées à un poste spécifique
        // Vous pouvez accéder à votre repository de candidature pour exécuter une requête appropriée
        // par exemple, candidatureRepository.findByPosteId(postId)
        return candidatureRepository.findByPoste_Id(postId);
    }
}
