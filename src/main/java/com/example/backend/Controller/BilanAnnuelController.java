package com.example.backend.Controller;

import com.example.backend.Entity.*;
import com.example.backend.Repository.BilanAnnuelRepository;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.ManagerServiceRepository;
import com.example.backend.Security.services.UserDetailsImpl;
import com.example.backend.Service.BilanAnnuelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/BilanAnnuel")
public class BilanAnnuelController {
    @Autowired
    private ManagerServiceRepository managerServiceRepository;
    @Autowired
    private CollaborateurRepository collaborateurRepository;
    @Autowired
    private BilanAnnuelService bilanAnnuelService;
    @PostMapping("/envoyer")
    public ResponseEntity<Map<String, Object>>envoyerBilanAnnuel(

    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long managerServiceId = userDetails.getId();
        ManagerService managerService = managerServiceRepository.findByManagerManagerId(managerServiceId)
                .orElseThrow(() -> new EntityNotFoundException("ManagerService non trouvé avec l'ID : " + managerServiceId));
        Map<String, Object> response = new HashMap<>();

        if (managerService == null) {
            response.put("success", false);
            response.put("message", "ManagerService non trouvé");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        bilanAnnuelService.envoyerBilanAnnuel(managerService);
        response.put("success", true);
        response.put("message", "Bilan annuel envoyé avec succès");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PutMapping("/mettreAJour/{bilanAnnuelId}")
    public ResponseEntity<BilanAnnuel> mettreAJourBilanAnnuel(
            @PathVariable Long bilanAnnuelId,
            @RequestBody BilanAnnuel bilanAnnuelMiseAJour) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();
        // ID de l'utilisateur
        // Utilisez la nouvelle méthode pour trouver le Collaborateur par l'ID de l'utilisateur

        Collaborateur collaborateur = collaborateurRepository.findByCollaborateurUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouvé avec l'ID : " + userId));

        // Vérifier que le Collaborateur connecté est le propriétaire du BilanAnnuel
        BilanAnnuel bilanAnnuel = bilanAnnuelService.mettreAJourBilanAnnuel(bilanAnnuelId, bilanAnnuelMiseAJour);

            return new ResponseEntity<>(bilanAnnuel, HttpStatus.OK);

    }
    @PutMapping("/mettreAJouretEnvoyer/{bilanAnnuelId}")
    public ResponseEntity<BilanAnnuel> mettreAJouretEnvoyerBilanAnnuel(
            @PathVariable Long bilanAnnuelId,
            @RequestBody BilanAnnuel bilanAnnuelMiseAJour) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();
        // ID de l'utilisateur
        // Utilisez la nouvelle méthode pour trouver le Collaborateur par l'ID de l'utilisateur

        Collaborateur collaborateur = collaborateurRepository.findByCollaborateurUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouvé avec l'ID : " + userId));

        // Vérifier que le Collaborateur connecté est le propriétaire du BilanAnnuel
        BilanAnnuel bilanAnnuel = bilanAnnuelService.mettreAJourBilanAnnuelEtEnvoye(bilanAnnuelId, bilanAnnuelMiseAJour);

        return new ResponseEntity<>(bilanAnnuel, HttpStatus.OK);

    }
        @GetMapping("/mesBilansAnnuel")
    public ResponseEntity<List<BilanAnnuel>> getMesBilanAnnuel() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId(); // ID de l'utilisateur

        // Utilisez la nouvelle méthode pour trouver le Collaborateur par l'ID de l'utilisateur
        Collaborateur collaborateur = collaborateurRepository.findByCollaborateurUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouvé avec l'ID : " + userId));

        List<BilanAnnuel> bilansAnnuelCollaborateur = bilanAnnuelService.getBilansAnnuelByCollaborateur(collaborateur);

        if (bilansAnnuelCollaborateur.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(bilansAnnuelCollaborateur, HttpStatus.OK);
        }
    }

    @GetMapping("/{bilanAnnuelId}")
    public ResponseEntity<Map<String, Object>> getBilanById(@PathVariable Long bilanAnnuelId) {
        BilanAnnuel bilanAnnuel = bilanAnnuelService.getBilanById(bilanAnnuelId);
        if (bilanAnnuel != null) {
            User collaborateur = bilanAnnuel.getCollaborateur().getCollaborateur();
            User managerService = bilanAnnuel.getManagerService().getManager();
            List<Evaluation> evaluations = bilanAnnuel.getCollaborateur().getEvaluations();
            evaluations.forEach(evaluation -> {
                Competence competence = evaluation.getCompetence();
                if (competence != null) {
                    evaluation.setCompetenceName(competence.getNom()); // Set the competenceName property
                }
            });
            Map<String, Object> response = new HashMap<>();
            response.put("bilanAnnuel", bilanAnnuel);
            response.put("collaborateur", collaborateur);
            response.put("manager", managerService);
            response.put("evaluations", evaluations);


            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/collaborateur/{collaborateurId}")
    public ResponseEntity<List<BilanAnnuel>> getEnvoyedBilansByCollaborateurId(@PathVariable Long collaborateurId) {
        // Récupérer tous les bilans du collaborateur
        List<BilanAnnuel> allBilans = bilanAnnuelService.getBilansByCollaborateurId(collaborateurId);

        // Filtrer les bilans envoyés
        List<BilanAnnuel> envoyedBilans = allBilans.stream()
                .filter(bilan -> bilan.getEtatBilan() == Etat.Envoye)
                .collect(Collectors.toList());

        // Retourner les bilans envoyés
        return new ResponseEntity<>(envoyedBilans, HttpStatus.OK);
    }


}
