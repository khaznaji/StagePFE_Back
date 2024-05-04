package com.example.backend.Controller;

import com.example.backend.Entity.*;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.FormationRepository;
import com.example.backend.Repository.ManagerServiceRepository;
import com.example.backend.Repository.ParticipationFormationRepository;
import com.example.backend.Security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public ResponseEntity<Map<String, Object>> inscriptionFormation(@PathVariable Long formationId) {
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

        // Créer une réponse JSON
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Inscription à la formation avec l'état réussie");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/mesdemandesdeFormations")
    public ResponseEntity<List<Map<String, Object>>> getMesDemandesDeFormations() {
        // Récupérez l'authentification actuelle pour obtenir les détails du collaborateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId(); // ID de l'utilisateur

        // Recherche du collaborateur correspondant à l'utilisateur
        Collaborateur collaborateur = collaborateurRepository.findByCollaborateurUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouvé avec l'ID de l'utilisateur : " + userId));

        // Récupérez les participations à la formation pour le collaborateur connecté
        List<ParticipationFormation> participations = participationFormationRepository.findByCollaborateur(collaborateur);

        // Créez une liste pour stocker les informations requises
        List<Map<String, Object>> mesFormations = new ArrayList<>();

        // Parcourez les participations pour récupérer les informations nécessaires
        for (ParticipationFormation participation : participations) {
            Map<String, Object> formationMap = new HashMap<>();
            Formation formation = participation.getFormation();
            formationMap.put("id", formation.getId());
            formationMap.put("titre", formation.getTitle());
            formationMap.put("etatParticipation", participation.getEtat().toString());
            mesFormations.add(formationMap);
        }

        return ResponseEntity.ok(mesFormations);
    }

    @Autowired
    ManagerServiceRepository managerServiceRepository;
    @GetMapping("/mesFormationsPourManager")
    public ResponseEntity<List<Map<String, Object>>> getMesFormationsPourManager() {
        // Récupérez l'authentification actuelle pour obtenir les détails du manager service connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId(); // ID de l'utilisateur

        // Recherche du manager service correspondant à l'utilisateur
        ManagerService managerService = managerServiceRepository.findByManagerManagerId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Manager service non trouvé avec l'ID de l'utilisateur : " + userId));

        // Récupérez les collaborateurs sous la responsabilité du manager service
        List<Collaborateur> collaborateurs = collaborateurRepository.findByManagerService(managerService);

        // Créez une liste pour stocker les informations requises
        List<Map<String, Object>> managerServiceFormationList = new ArrayList<>();

        // Parcourez les collaborateurs et leurs participations pour récupérer les informations nécessaires
        for (Collaborateur collaborateur : collaborateurs) {
            for (ParticipationFormation participation : collaborateur.getParticipations()) {
                if (participation.getEtat() == EtatParticipation.EN_ATTENTE) {
                    Map<String, Object> formationMap = new HashMap<>();
                    formationMap.put("nom", collaborateur.getCollaborateur().getNom());
                    formationMap.put("prenom", collaborateur.getCollaborateur().getPrenom());
                    formationMap.put("image", collaborateur.getCollaborateur().getImage());
                    formationMap.put("poste", collaborateur.getPoste());
                    formationMap.put("id", participation.getFormation().getId());
                    formationMap.put("idParticipation", participation.getId()); // Ajoutez l'ID de la formation
// Ajoutez l'ID de la formation
                    formationMap.put("nomFormation", participation.getFormation().getTitle());
                    managerServiceFormationList.add(formationMap);
                }
            }
        }

        return ResponseEntity.ok(managerServiceFormationList);
    }
    @GetMapping("/demandeConfirme/{idFormation}")
    public ResponseEntity<List<Map<String, Object>>> getDemandeConfirme(@PathVariable Long idFormation) {
        // Récupérez la formation correspondant à l'ID spécifié
        Formation formation = formationRepository.findById(idFormation)
                .orElseThrow(() -> new EntityNotFoundException("Formation non trouvée avec l'ID : " + idFormation));

        // Créez une liste pour stocker les informations requises
        List<Map<String, Object>> managerServiceFormationList = new ArrayList<>();

        // Parcourez les participations à la formation pour récupérer les informations nécessaires
        for (ParticipationFormation participation : formation.getParticipations()) {
            if (participation.getEtat() == EtatParticipation.CONFIRME) {
                Map<String, Object> formationMap = new HashMap<>();
                Collaborateur collaborateur = participation.getCollaborateur();
                formationMap.put("nom", collaborateur.getCollaborateur().getNom());
                formationMap.put("idC", collaborateur.getId());
                formationMap.put("prenom", collaborateur.getCollaborateur().getPrenom());
                formationMap.put("image", collaborateur.getCollaborateur().getImage());
                formationMap.put("poste", collaborateur.getPoste());
                formationMap.put("idParticipation", participation.getId()); // Ajoutez l'ID de la participation
                formationMap.put("nomFormation", formation.getTitle());
                formationMap.put("id", formation.getId());
                managerServiceFormationList.add(formationMap);
            }
        }

        return ResponseEntity.ok(managerServiceFormationList);
    }
    @GetMapping("/demandeAccepte/{idFormation}")
    public ResponseEntity<List<Map<String, Object>>> getDemandeAccepte(@PathVariable Long idFormation) {
        // Récupérez la formation correspondant à l'ID spécifié
        Formation formation = formationRepository.findById(idFormation)
                .orElseThrow(() -> new EntityNotFoundException("Formation non trouvée avec l'ID : " + idFormation));

        // Créez une liste pour stocker les informations requises
        List<Map<String, Object>> managerServiceFormationList = new ArrayList<>();

        // Parcourez les participations à la formation pour récupérer les informations nécessaires
        for (ParticipationFormation participation : formation.getParticipations()) {
            if (participation.getEtat() == EtatParticipation.ACCEPTE) {
                Map<String, Object> formationMap = new HashMap<>();
                Collaborateur collaborateur = participation.getCollaborateur();
                formationMap.put("nom", collaborateur.getCollaborateur().getNom());
                formationMap.put("prenom", collaborateur.getCollaborateur().getPrenom());
                formationMap.put("image", collaborateur.getCollaborateur().getImage());
                formationMap.put("poste", collaborateur.getPoste());
                formationMap.put("idParticipation", participation.getId()); // Ajoutez l'ID de la participation
                formationMap.put("nomFormation", formation.getTitle());
                formationMap.put("id", formation.getId());

                managerServiceFormationList.add(formationMap);
            }
        }

        return ResponseEntity.ok(managerServiceFormationList);
    }

    @GetMapping("/FormationAccepte")
    public ResponseEntity<List<Map<String, Object>>> getFormationAccepte() {
        // Récupérez l'authentification actuelle pour obtenir les détails du manager service connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId(); // ID de l'utilisateur

        // Recherche du manager service correspondant à l'utilisateur
        ManagerService managerService = managerServiceRepository.findByManagerManagerId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Manager service non trouvé avec l'ID de l'utilisateur : " + userId));

        // Récupérez les collaborateurs sous la responsabilité du manager service
        List<Collaborateur> collaborateurs = collaborateurRepository.findByManagerService(managerService);

        // Créez une liste pour stocker les informations requises
        List<Map<String, Object>> managerServiceFormationList = new ArrayList<>();

        // Parcourez les collaborateurs et leurs participations pour récupérer les informations nécessaires
        for (Collaborateur collaborateur : collaborateurs) {
            for (ParticipationFormation participation : collaborateur.getParticipations()) {
                if (participation.getEtat() == EtatParticipation.ACCEPTE) {
                    Map<String, Object> formationMap = new HashMap<>();
                    formationMap.put("nom", collaborateur.getCollaborateur().getNom());
                    formationMap.put("prenom", collaborateur.getCollaborateur().getPrenom());
                    formationMap.put("image", collaborateur.getCollaborateur().getImage());
                    formationMap.put("poste", collaborateur.getPoste());
                    formationMap.put("id", participation.getFormation().getId());
                    formationMap.put("idParticipation", participation.getId()); // Ajoutez l'ID de la formation
// Ajoutez l'ID de la formation
                    formationMap.put("nomFormation", participation.getFormation().getTitle());
                    managerServiceFormationList.add(formationMap);
                }
            }
        }

        return ResponseEntity.ok(managerServiceFormationList);
    }

    @PutMapping("/updateEtatAccepte/{participationId}")
    public ResponseEntity<Map<String, String>> updateEtatParticipationAccepte(@PathVariable Long participationId) {
        // Vérifier si la participation existe
        ParticipationFormation participation = participationFormationRepository.findById(participationId)
                .orElseThrow(() -> new EntityNotFoundException("Participation non trouvée avec l'ID : " + participationId));

        // Mettre à jour l'état de la participation
        participation.setEtat(EtatParticipation.ACCEPTE);
        participationFormationRepository.save(participation);

        // Retourner une réponse appropriée
        Map<String, String> response = new HashMap<>();
        response.put("message", "État de la participation mis à jour avec succès");
        return ResponseEntity.ok(response);

    }
    @PutMapping("/updateEtatConfirme/{participationId}")
    public ResponseEntity<Map<String, String>> updateEtatParticipationConfirme(@PathVariable Long participationId) {
        // Vérifier si la participation existe
        ParticipationFormation participation = participationFormationRepository.findById(participationId)
                .orElseThrow(() -> new EntityNotFoundException("Participation non trouvée avec l'ID : " + participationId));

        // Mettre à jour l'état de la participation
        participation.setEtat(EtatParticipation.CONFIRME);
        participationFormationRepository.save(participation);

        // Retourner une réponse appropriée
        Map<String, String> response = new HashMap<>();
        response.put("message", "État de la participation mis à jour avec succès");
        return ResponseEntity.ok(response);

    }

    @PutMapping("/updateEtatRefusee/{participationId}")
    public ResponseEntity<Map<String, String>> updateEtatParticipationRefusee(@PathVariable Long participationId) {

        ParticipationFormation participation = participationFormationRepository.findById(participationId)
                .orElseThrow(() -> new EntityNotFoundException("Participation non trouvée avec l'ID : " + participationId));

        participation.setEtat(EtatParticipation.REFUSE);
        participationFormationRepository.save(participation);

        Map<String, String> response = new HashMap<>();
        response.put("message", "État de la participation mis à jour avec succès");
        return ResponseEntity.ok(response);
    }


}
