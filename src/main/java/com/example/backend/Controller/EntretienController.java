package com.example.backend.Controller;
import com.example.backend.Entity.*;
import com.example.backend.Repository.CandidatureRepository;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.ManagerServiceRepository;
import com.example.backend.Security.services.UserDetailsImpl;
import com.example.backend.Service.EntretienService;
import com.example.backend.Service.PosteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.*;

@Controller
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/entretien")
public class EntretienController {
    @Autowired
    private EntretienService entretienService;
    @Autowired

    private CandidatureRepository candidatureRepository;
    @Autowired
    private CollaborateurRepository collaborateurRepository;
    @Autowired
    private ManagerServiceRepository managerServiceRepository;


    @GetMapping
    public ResponseEntity<List<Entretien>> getAllEntretiens() {
        List<Entretien> entretiens = entretienService.getAllEntretiens();
        return new ResponseEntity<>(entretiens, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getEntretienById(@PathVariable Long id) {
        Optional<Entretien> entretienOptional = entretienService.getEntretienById(id);

        if (entretienOptional.isPresent()) {
            Entretien entretien = entretienOptional.get();

            Map<String, Object> entretienAvecCollaborateur = new HashMap<>();
            entretienAvecCollaborateur.put("entretien", entretien);

            // Obtenez la candidature associée à l'entretien
            Candidature candidature = entretien.getCandidature();
            if (candidature != null) {
                Collaborateur collaborateur = candidature.getCollaborateur();
                if (collaborateur != null) {
                    // Ajoutez les détails du collaborateur à la réponse
                    entretienAvecCollaborateur.put("nomCollaborateur", collaborateur.getCollaborateur().getNom());
                    entretienAvecCollaborateur.put("prenomCollaborateur", collaborateur.getCollaborateur().getPrenom());
                }
            }
            return new ResponseEntity<>(entretienAvecCollaborateur, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @PostMapping("/create")
    public ResponseEntity<String> createEntretien(
            @RequestParam Long postId,
            @RequestParam Long candidatureId,
            @RequestParam String dateEntretien,
            @RequestParam String heureDebut,
            @RequestParam String  heureFin) {
        try {
            entretienService.createEntretienForPosteAndCandidature(postId, candidatureId, dateEntretien, heureDebut, heureFin);
            Candidature candidature = candidatureRepository.findById(candidatureId).orElseThrow(() -> new IllegalArgumentException("Candidature not found"));
            candidature.setEtat(EtatPostulation.Entretien);
            candidatureRepository.save(candidature);
            return ResponseEntity.ok("Entretien created successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/poste/{posteId}")
    public ResponseEntity<List<Map<String, Object>>> getEntretiensByPosteId(@PathVariable Long posteId) {
        List<Entretien> entretiens = entretienService.getEntretiensByPosteId(posteId);
        List<Map<String, Object>> entretiensAvecCollaborateurs = new ArrayList<>();
        for (Entretien entretien : entretiens) {
            Map<String, Object> entretienAvecCollaborateur = new HashMap<>();
            entretienAvecCollaborateur.put("entretien", entretien);

            Candidature candidature = entretien.getCandidature();
            if (candidature != null) {
                Collaborateur collaborateur = candidature.getCollaborateur();
                if (collaborateur != null) {
                    entretienAvecCollaborateur.put("nomCollaborateur", collaborateur.getCollaborateur().getNom());
                    entretienAvecCollaborateur.put("prenomCollaborateur", collaborateur.getCollaborateur().getPrenom());
                }
            }

            entretiensAvecCollaborateurs.add(entretienAvecCollaborateur);
        }

        return new ResponseEntity<>(entretiensAvecCollaborateurs, HttpStatus.OK);
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<String> updateEntretien(
            @PathVariable Long id,
            @RequestParam Long candidatureId,
            @RequestParam String dateEntretien,
            @RequestParam String heureDebut,
            @RequestParam String heureFin) {
        try {
            entretienService.updateEntretien(id, candidatureId, dateEntretien, heureDebut, heureFin);
            return ResponseEntity.ok("Entretien updated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntretien(@PathVariable Long id) {
        entretienService.deleteEntretien(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/collaborateur")
    public ResponseEntity<List<Map<String, Object>>> getEntretiensDuCollaborateurConnecte() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long collaboratorId = userDetails.getId();
        System.out.println(collaboratorId); // Journal de débogage pour enregistrer l'ID
        Long userId = userDetails.getId();

        // Recherche du collaborateur correspondant à l'utilisateur
        Collaborateur collaborateur = collaborateurRepository.findByCollaborateurUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouvé avec l'ID de l'utilisateur : " + userId));
        Long collaboratorIdd = collaborateur.getId();

        List<Entretien> entretiensDuCollaborateur = entretienService.getEntretiensByCollaborateurId(collaboratorIdd);
        List<Map<String, Object>> entretiensAvecCollaborateurs = new ArrayList<>();
        for (Entretien entretien : entretiensDuCollaborateur) {
            Map<String, Object> entretienAvecCollaborateur = new HashMap<>();
            entretienAvecCollaborateur.put("entretien", entretien);

            Candidature candidature = entretien.getCandidature();
            if (candidature != null) {
                Collaborateur collaborateurr = candidature.getCollaborateur();
                if (collaborateurr != null) {
                    entretienAvecCollaborateur.put("nomCollaborateur", collaborateurr.getCollaborateur().getNom());
                    entretienAvecCollaborateur.put("prenomCollaborateur", collaborateurr.getCollaborateur().getPrenom());
                }
            }

            entretiensAvecCollaborateurs.add(entretienAvecCollaborateur);
        }

        return new ResponseEntity<>(entretiensAvecCollaborateurs, HttpStatus.OK);
    }
    @GetMapping("/managerService")
    public ResponseEntity<List<Map<String, Object>>> getEntretiensDumanagerServiceConnecte() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long collaboratorId = userDetails.getId();
        System.out.println(collaboratorId); // Journal de débogage pour enregistrer l'ID
        Long userId = userDetails.getId();
        ManagerService collaborateur = managerServiceRepository.findByManagerManagerId(userId)
                .orElseThrow(() -> new EntityNotFoundException("ManagerService non trouvé avec l'ID : " + userId));
       Long collaboratorIdd = collaborateur.getId();

        List<Entretien> entretiensDuCollaborateur = entretienService.getEntretiensByCollaborateurId(collaboratorIdd);
        List<Map<String, Object>> entretiensAvecCollaborateurs = new ArrayList<>();
        for (Entretien entretien : entretiensDuCollaborateur) {
            Map<String, Object> entretienAvecCollaborateur = new HashMap<>();
            entretienAvecCollaborateur.put("entretien", entretien);

            Candidature candidature = entretien.getCandidature();
            if (candidature != null) {
                Collaborateur collaborateurr = candidature.getCollaborateur();
                if (collaborateurr != null) {
                    entretienAvecCollaborateur.put("nomCollaborateur", collaborateurr.getCollaborateur().getNom());
                    entretienAvecCollaborateur.put("prenomCollaborateur", collaborateurr.getCollaborateur().getPrenom());
                }
            }

            entretiensAvecCollaborateurs.add(entretienAvecCollaborateur);
        }

        return new ResponseEntity<>(entretiensAvecCollaborateurs, HttpStatus.OK);
    }
}
