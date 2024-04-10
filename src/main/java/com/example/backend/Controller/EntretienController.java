package com.example.backend.Controller;
import com.example.backend.Entity.*;
import com.example.backend.Repository.CandidatureRepository;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.ManagerServiceRepository;
import com.example.backend.Repository.PosteRepository;
import com.example.backend.Security.services.UserDetailsImpl;
import com.example.backend.Service.EntretienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

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
            @RequestParam String heureFin) {
        try {
            // Créer l'entretien pour le poste et la candidature donnés
            entretienService.createEntretienForPosteAndCandidature(postId, candidatureId, dateEntretien, heureDebut, heureFin);
            Optional<Candidature> optionalCandidature = candidatureRepository.findById(candidatureId);

            if (optionalCandidature.isPresent()) {
                // Mettre à jour l'état de la candidature à "Entretien"
                Candidature candidature = optionalCandidature.get();
                candidature.setEtat(EtatPostulation.Entretien);
                candidatureRepository.save(candidature); // Sauvegarder la candidature mise à jour
                return ResponseEntity.ok("Entretien créé avec succès et état de la candidature mis à jour à 'Entretien'");
            } else {
                // Si la candidature n'est pas trouvée, renvoyer une réponse de mauvaise requête
                return ResponseEntity.badRequest().body("Candidature non trouvée pour l'ID : " + candidatureId);
            }
        } catch (IllegalArgumentException e) {
            // En cas d'erreur, renvoyer une réponse de mauvaise requête avec le message d'erreur
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
        Optional<Entretien> optionalEntretien = entretienService.getEntretienById(id);
        if (optionalEntretien.isPresent()) {
            Entretien entretien = optionalEntretien.get();
            Candidature candidature = entretien.getCandidature();
            candidature.setEtat(EtatPostulation.EN_ATTENTE_ENTRETIEN);
            candidatureRepository.save(candidature); // Sauvegarder la candidature mise à jour
            entretienService.deleteEntretien(id);

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            // Si l'entretien n'est pas trouvé, retourner une réponse de mauvaise requête
            return ResponseEntity.notFound().build();
        }
    }



    @GetMapping("/collaborateur")
    public ResponseEntity<List<Map<String, Object>>> getEntretiensDuCollaborateurConnecte() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        // Recherche du collaborateur correspondant à l'utilisateur
        Collaborateur collaborateur = collaborateurRepository.findByCollaborateurUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouvé avec l'ID de l'utilisateur : " + userId));

        List<Entretien> entretiensDuCollaborateur = entretienService.getEntretiensByCollaborateurId(collaborateur.getId());

        // Filtrer les entretiens pour ne récupérer que ceux avec l'état "En_Attente"
        List<Map<String, Object>> entretiensAvecCollaborateurs = entretiensDuCollaborateur.stream()
                .filter(entretien -> entretien.getEtatEntretien() == EtatEntretien.En_Attente)
                .map(entretien -> {
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

                    return entretienAvecCollaborateur;
                })
                .collect(Collectors.toList());

        return new ResponseEntity<>(entretiensAvecCollaborateurs, HttpStatus.OK);
    }

    @GetMapping("/managerService")
    public ResponseEntity<?> getEntretiensDumanagerServiceConnecte() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        ManagerService collaborateur = managerServiceRepository.findByManagerManagerId(userId)
                .orElseThrow(() -> new EntityNotFoundException("ManagerService non trouvé avec l'ID : " + userId));

        List<Poste> postesDuManager = collaborateur.getPostes();

        if (postesDuManager.isEmpty()) {
            return new ResponseEntity<>("Le manager n'est pas associé à un poste", HttpStatus.NOT_FOUND);
        }

        List<Entretien> entretiensDuManager = new ArrayList<>();
        for (Poste poste : postesDuManager) {
            List<Entretien> entretiensDuPoste = poste.getEntretiens();
            entretiensDuManager.addAll(entretiensDuPoste);
        }

        List<Map<String, Object>> entretiensAvecCollaborateurs = new ArrayList<>();
        for (Entretien entretien : entretiensDuManager) {
            if (entretien.getEtatEntretien() == EtatEntretien.En_Attente) { // Vérifiez si l'état de l'entretien est En_Attente
                Map<String, Object> entretienAvecCollaborateur = new HashMap<>();
                entretienAvecCollaborateur.put("entretien", entretien);
                Candidature candidature = entretien.getCandidature();
                if (candidature != null) {
                    Collaborateur collaborateurr = candidature.getCollaborateur();
                    if (collaborateur != null) {
                        entretienAvecCollaborateur.put("nomCollaborateur", collaborateurr.getCollaborateur().getNom());
                        entretienAvecCollaborateur.put("prenomCollaborateur", collaborateurr.getCollaborateur().getPrenom());
                    }
                }
                entretiensAvecCollaborateurs.add(entretienAvecCollaborateur);
            }
        }

        if (entretiensAvecCollaborateurs.isEmpty()) {
            return new ResponseEntity<>("Aucun entretien En_Attente trouvé pour ce manager", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(entretiensAvecCollaborateurs, HttpStatus.OK);
    }


    @PutMapping("/{id}/noter")
    public ResponseEntity<String> noterEntretien(
            @PathVariable Long id,
            @RequestParam int note,
            @RequestParam String commentaire) {
        try {
            entretienService.noterEntretien(id, note, commentaire);
            return ResponseEntity.ok("Entretien noté avec succès");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/EntretiensSpecifiques/{postId}")
    public ResponseEntity<List<Map<String, String>>> getEntretiensSpecifiques(@PathVariable Long postId) {
        List<Entretien> entretiens = entretienService.getEntretiensByPoste(postId);
        List<Map<String, String>> entretiensInfo = new ArrayList<>();
        for (Entretien entretien : entretiens) {
            if (entretien.getEtatEntretien() == EtatEntretien.Termine) { // Filtrer les entretiens avec l'état "Termine"
                Collaborateur collaborateur = entretien.getCandidature().getCollaborateur(); // Récupérer le collaborateur associé à l'entretien
                Map<String, String> entretienInfo = new HashMap<>();
                entretienInfo.put("entretien_id", entretien.getId().toString()); // ID de l'entretien
                entretienInfo.put("dateEntretien", entretien.getDateEntretien());
                entretienInfo.put("heureDebut", entretien.getHeureDebut());
                entretienInfo.put("heureFin", entretien.getHeureFin());
                entretienInfo.put("poste_id", entretien.getPoste().getId().toString()); // ID du poste
                entretienInfo.put("commentaire", entretien.getCommentaire());
                entretienInfo.put("note", String.valueOf(entretien.getNote())); // Convert int to String
                if (collaborateur != null) {
                    entretienInfo.put("nomCollaborateur", collaborateur.getCollaborateur().getNom());
                    entretienInfo.put("prenomCollaborateur", collaborateur.getCollaborateur().getPrenom());
                }

                entretiensInfo.add(entretienInfo);
            }
        }

        return new ResponseEntity<>(entretiensInfo, HttpStatus.OK);
    }


}
