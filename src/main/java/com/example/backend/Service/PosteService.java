package com.example.backend.Service;

import com.example.backend.Entity.Collaborateur;
import com.example.backend.Entity.Competence;
import com.example.backend.Entity.ManagerService;
import com.example.backend.Entity.Poste;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.CompetenceRepository;
import com.example.backend.Repository.ManagerServiceRepository;
import com.example.backend.Repository.PosteRepository;
import com.example.backend.Security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PosteService implements IPosteService{

    @Autowired
    private PosteRepository posteRepository;
    @Autowired
    private CollaborateurRepository collaborateurRepository;
    @Autowired
    private CompetenceRepository competenceRepository;
    @Override
    public Poste createPoste(Poste poste) {

        return posteRepository.save(poste);
    }
    public Long getUserIdForCollaborateur(Long collaborateurId) {
        Collaborateur collaborateur = collaborateurRepository.findById(collaborateurId)
                .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouvé avec l'ID : " + collaborateurId));

        // Retourne l'ID de l'utilisateur associé au collaborateur
        return collaborateur.getCollaborateur().getId();
    }

}
