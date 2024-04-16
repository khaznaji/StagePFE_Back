package com.example.backend.Controller;

import com.example.backend.Entity.Collaborateur;
import com.example.backend.Entity.EtatParticipation;
import com.example.backend.Entity.Formation;
import com.example.backend.Entity.ParticipationFormation;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.FormationRepository;
import com.example.backend.Repository.ParticipationFormationRepository;
import com.example.backend.Security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.persistence.EntityNotFoundException;

public class ParticipationFormationController {
    @Autowired
    CollaborateurRepository collaborateurRepository;
    @Autowired
    FormationRepository formationRepository;
    @Autowired
    ParticipationFormationRepository participationFormationRepository;

    @PostMapping("/inscription/{formationId}")
    public ResponseEntity<?> inscriptionFormation(@PathVariable Long formationId) {
        // Obtenez l'authentification actuelle pour récupérer les détails du collaborateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Obtenez le collaborateur connecté à partir des détails de l'utilisateur
        Collaborateur collaborateur = collaborateurRepository.findByCollaborateurUserId(userDetails.getId())
                .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouvé avec l'ID de l'utilisateur : " + userDetails.getId()));

        // Vérifiez si la formation existe
        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new EntityNotFoundException("Formation non trouvée avec l'ID : " + formationId));

        // Créez une nouvelle participation à la formation avec l'état spécifié
        ParticipationFormation participation = new ParticipationFormation();
        participation.setCollaborateur(collaborateur);
        participation.setFormation(formation);
        participation.setEtat(EtatParticipation.EN_ATTENTE);

        // Enregistrez la participation à la formation
        participationFormationRepository.save(participation);

        return ResponseEntity.ok("Inscription à la formation avec l'état "  + " réussie");
    }


}
