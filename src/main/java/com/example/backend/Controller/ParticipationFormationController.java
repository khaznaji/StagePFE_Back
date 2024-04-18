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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/ParticipationFormation")
public class ParticipationFormationController {
    @Autowired
    CollaborateurRepository collaborateurRepository;
    @Autowired
    FormationRepository formationRepository;
    @Autowired
    ParticipationFormationRepository participationFormationRepository;

    @PostMapping("/inscription/{formationId}")
    public ResponseEntity<?> inscriptionFormation(@PathVariable Long formationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId(); // ID de l'utilisateur

        // Recherche du collaborateur correspondant à l'utilisateur
        Collaborateur collaborateur = collaborateurRepository.findByCollaborateurUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouvé avec l'ID de l'utilisateur : " + userId));

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
    @GetMapping("/mesFormations")
    public ResponseEntity<List<Formation>> getMesFormations() {
        // Récupérez l'authentification actuelle pour obtenir les détails du collaborateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId(); // ID de l'utilisateur

        // Recherche du collaborateur correspondant à l'utilisateur
        Collaborateur collaborateur = collaborateurRepository.findByCollaborateurUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouvé avec l'ID de l'utilisateur : " + userId));

        // Récupérez les participations à la formation pour le collaborateur connecté
        List<ParticipationFormation> participations = participationFormationRepository.findByCollaborateur(collaborateur);

        // Récupérez les formations correspondantes aux participations
        List<Formation> mesFormations = participations.stream()
                .map(ParticipationFormation::getFormation)
                .collect(Collectors.toList());

        return ResponseEntity.ok(mesFormations);
    }


}
