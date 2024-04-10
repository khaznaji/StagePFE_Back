package com.example.backend.Service;

import com.example.backend.Entity.*;
import com.example.backend.Repository.*;
import com.example.backend.Security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Service
public class PosteService implements IPosteService{

    @Autowired
    private PosteRepository posteRepository;
    @Autowired
    private CollaborateurRepository collaborateurRepository;
    @Autowired
    private CandidatureRepository candidatureRepository;
    @Override
    public Poste createPoste(Poste poste) {

        return posteRepository.save(poste);
    }
    public Poste getPosteById(Long posteId) {
        Poste poste = posteRepository.findById(posteId).orElse(null);
        if (poste != null) {
            System.out.println("Poste récupéré : " + poste.toString());
        } else {
            System.out.println("Aucun poste trouvé avec l'ID : " + posteId);
        }
        return poste;
    }

    public Long getUserIdForCollaborateur(Long collaborateurId) {
        Collaborateur collaborateur = collaborateurRepository.findById(collaborateurId)
                .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouvé avec l'ID : " + collaborateurId));

        // Retourne l'ID de l'utilisateur associé au collaborateur
        return collaborateur.getCollaborateur().getId();
    }

    public Poste getPosteByCandidatureId(Long candidatureId) {
        Candidature candidature = candidatureRepository.findById(candidatureId).orElse(null);
        if (candidature != null) {
            return candidature.getPoste();
        } else {
            return null;
        }
    }


}
