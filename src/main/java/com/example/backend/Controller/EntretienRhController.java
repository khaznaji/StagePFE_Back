package com.example.backend.Controller;

import com.example.backend.Configuration.MailConfig;
import com.example.backend.Entity.*;
import com.example.backend.Repository.CandidatureRepository;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Security.services.UserDetailsImpl;
import com.example.backend.Service.EntretienRhService;
import com.example.backend.Service.EntretienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/entretienRh")
public class EntretienRhController {
    @Autowired
    private EntretienRhService entretienRhService;
    @Autowired

    private CandidatureRepository candidatureRepository;
    @Autowired

    private UserRepository userRepository;
    @Autowired
    private MailConfig emailService ;
    @PostMapping("/create")
    public ResponseEntity<String> createEntretien(
            @RequestParam Long postId,
            @RequestParam Long candidatureId,
            @RequestParam String dateEntretien,
            @RequestParam String heureDebut,
            @RequestParam String heureFin,
            @RequestParam Long userId) {
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                if (user.getRole() == Role.ManagerRh) {
                    // Créer l'entretien pour le poste et la candidature donnés
                    entretienRhService.createEntretienForPosteAndCandidatureAndUser(postId, candidatureId, dateEntretien, heureDebut, heureFin, userId);

                    Optional<Candidature> optionalCandidature = candidatureRepository.findById(candidatureId);
                    if (optionalCandidature.isPresent()) {
                        // Mettre à jour l'état de la candidature à "Entretien"
                        Candidature candidature = optionalCandidature.get();
                        candidature.setEtat(EtatPostulation.Entretien_Rh);
                        candidatureRepository.save(candidature); // Sauvegarder la candidature mise à jour
                        String titrePoste = candidature.getPoste().getTitre();

                        // Envoyer un e-mail au manager RH
                        String subjectManager = "Nouvel entretien RH programmé : " + titrePoste ;
                        String contentManager = "Cher Manager RH, un nouvel entretien RH a été programmé pour une candidature. Merci de vérifier.";

                        emailService.sendEmail(user.getEmail(), subjectManager, contentManager);

                        // Envoyer un e-mail au collaborateur
                        String subjectCollaborateur =  "Nouvel entretien RH programmé : " + titrePoste ;
                        String contentCollaborateur = "Cher Collaborateur, un entretien RH a été programmé pour vous. Merci de vérifier.";

                        // Récupérer l'e-mail du collaborateur depuis la candidature (supposons qu'il existe un champ email dans Candidature)
                        String collaborateurEmail = candidature.getCollaborateur().getCollaborateur().getEmail();

                        emailService.sendEmail(collaborateurEmail, subjectCollaborateur, contentCollaborateur);

                        return ResponseEntity.ok("Entretien créé avec succès et état de la candidature mis à jour à 'Entretien'");
                    } else {
                        // Si la candidature n'est pas trouvée, renvoyer une réponse de mauvaise requête
                        return ResponseEntity.badRequest().body("Candidature non trouvée pour l'ID : " + candidatureId);
                    }
                } else {
                    return ResponseEntity.badRequest().body("L'utilisateur avec l'ID : " + userId + " n'a pas le rôle de ManagerRh");
                }
            } else {
                return ResponseEntity.badRequest().body("Utilisateur non trouvé avec l'ID : " + userId);
            }
        } catch (IllegalArgumentException e) {
            // En cas d'erreur, renvoyer une réponse de mauvaise requête avec le message d'erreur
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping
    public ResponseEntity<List<EntretienRh>> getAllEntretiens() {
        List<EntretienRh> entretiens = entretienRhService.getAllEntretiens();
        return new ResponseEntity<>(entretiens, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getEntretienById(@PathVariable Long id) {
        Optional<EntretienRh> entretienOptional = entretienRhService.getEntretienById(id);

        if (entretienOptional.isPresent()) {
            EntretienRh entretien = entretienOptional.get();

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
    @GetMapping("/poste/{posteId}")
    public ResponseEntity<List<Map<String, Object>>> getEntretiensByPosteId(@PathVariable Long posteId) {
        List<EntretienRh> entretiens = entretienRhService.getEntretiensByPosteId(posteId);
        List<Map<String, Object>> entretiensAvecCollaborateurs = new ArrayList<>();
        for (EntretienRh entretien : entretiens) {
            Map<String, Object> entretienAvecCollaborateur = new HashMap<>();
            entretienAvecCollaborateur.put("entretien", entretien);

            Candidature candidature = entretien.getCandidature();
            if (candidature != null) {
                Collaborateur collaborateur = candidature.getCollaborateur();
                if (collaborateur != null) {
                    entretienAvecCollaborateur.put("nomCollaborateur", collaborateur.getCollaborateur().getNom());
                    entretienAvecCollaborateur.put("prenomCollaborateur", collaborateur.getCollaborateur().getPrenom());
                    entretienAvecCollaborateur.put("candidatureId", collaborateur.getCollaborateur().getId());

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
            @RequestParam Long userId,
            @RequestParam String dateEntretien,
            @RequestParam String heureDebut,
            @RequestParam String heureFin) {
        try {
            entretienRhService.updateEntretien(id, candidatureId, dateEntretien, heureDebut, heureFin , userId);
            return ResponseEntity.ok("Entretien updated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntretien(@PathVariable Long id) {
        Optional<EntretienRh> optionalEntretien = entretienRhService.getEntretienById(id);
        if (optionalEntretien.isPresent()) {
            EntretienRh entretien = optionalEntretien.get();
            Candidature candidature = entretien.getCandidature();
            candidature.setEtat(EtatPostulation.EN_ATTENTE_ENTRETIEN);
            candidatureRepository.save(candidature); // Sauvegarder la candidature mise à jour
            entretienRhService.deleteEntretien(id);

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            // Si l'entretien n'est pas trouvé, retourner une réponse de mauvaise requête
            return ResponseEntity.notFound().build();
        }
    }
    @Autowired
    CollaborateurRepository collaborateurRepository ;
    @GetMapping("/collaborateur")
    public ResponseEntity<List<Map<String, Object>>> getEntretiensDuCollaborateurConnecte() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        // Recherche du collaborateur correspondant à l'utilisateur
        Collaborateur collaborateur = collaborateurRepository.findByCollaborateurUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouvé avec l'ID de l'utilisateur : " + userId));

        List<EntretienRh> entretiensDuCollaborateur = entretienRhService.getEntretiensByCollaborateurId(collaborateur.getId());

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

    @GetMapping("/managerRh")
    public ResponseEntity<List<Map<String, Object>>> getEntretiensDuManagerRhConnecte() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        // Recherche du ManagerRh correspondant à l'utilisateur
        User managerRh = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("ManagerRh non trouvé avec l'ID de l'utilisateur : " + userId));

        List<EntretienRh> entretiensDuManagerRh = entretienRhService.getEntretiensByManagerRhId(managerRh.getId());

        // Filtrer les entretiens pour ne récupérer que ceux avec l'état "En_Attente"
        List<Map<String, Object>> entretiensAvecCollaborateurs = entretiensDuManagerRh.stream()
                .filter(entretien -> entretien.getEtatEntretien() == EtatEntretien.En_Attente)
                .map(entretien -> {
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

                    return entretienAvecCollaborateur;
                })
                .collect(Collectors.toList());

        return new ResponseEntity<>(entretiensAvecCollaborateurs, HttpStatus.OK);
    }
    @PutMapping("/{id}/noter")
    public ResponseEntity<String> noterEntretien(
            @PathVariable Long id,
            @RequestParam int salaire
           ) {
        try {
            entretienRhService.noterEntretien(id, salaire);
            return ResponseEntity.ok("Entretien noté avec succès");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/EntretiensSpecifiques/{postId}")
    public ResponseEntity<List<Map<String, String>>> getEntretiensSpecifiques(@PathVariable Long postId) {
        List<EntretienRh> entretiens = entretienRhService.getEntretiensByPoste(postId);
        List<Map<String, String>> entretiensInfo = new ArrayList<>();
        for (EntretienRh entretien : entretiens) {
            if (entretien.getEtatEntretien() == EtatEntretien.Termine) { // Filtrer les entretiens avec l'état "Termine"
                Collaborateur collaborateur = entretien.getCandidature().getCollaborateur(); // Récupérer le collaborateur associé à l'entretien
                Map<String, String> entretienInfo = new HashMap<>();
                entretienInfo.put("entretien_id", entretien.getId().toString()); // ID de l'entretien
                entretienInfo.put("dateEntretien", entretien.getDateEntretien());
                entretienInfo.put("heureDebut", entretien.getHeureDebut());
                entretienInfo.put("heureFin", entretien.getHeureFin());
                entretienInfo.put("poste_id", entretien.getPoste().getId().toString()); // ID du poste
                entretienInfo.put("salaire", String.valueOf(entretien.getSalaire())); // Convert int to String
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
