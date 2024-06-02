package com.example.backend.Controller;
import com.example.backend.Configuration.MailConfig;
import com.example.backend.Entity.*;
import com.example.backend.Repository.*;
import com.example.backend.Security.services.UserDetailsImpl;
import com.example.backend.Service.EntretienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
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
    private JavaMailSender emailSender;
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
    @GetMapping("/annuelbyId/{id}")
    public ResponseEntity<Map<String, Object>> getEntretienannuelbyId(@PathVariable Long id) {
        Optional<Entretien> entretienOptional = entretienService.getEntretienById(id);

        if (entretienOptional.isPresent()) {
            Entretien entretien = entretienOptional.get();

            Map<String, Object> entretienAvecCollaborateur = new HashMap<>();
            entretienAvecCollaborateur.put("entretien", entretien);


                Collaborateur collaborateur = entretien.getCollaborateurs();
                if (collaborateur != null) {
                    // Ajoutez les détails du collaborateur à la réponse
                    entretienAvecCollaborateur.put("nomCollaborateur", collaborateur.getCollaborateur().getNom());
                    entretienAvecCollaborateur.put("prenomCollaborateur", collaborateur.getCollaborateur().getPrenom());
                    entretienAvecCollaborateur.put("collaborateurId", collaborateur.getCollaborateur().getId());

            }
            return new ResponseEntity<>(entretienAvecCollaborateur, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Autowired
    private MailConfig emailService;
    @Autowired
    private PosteRepository userRepository;
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createEntretien(
            @RequestParam Long postId,
            @RequestParam Long candidatureId,
            @RequestParam String dateEntretien,
            @RequestParam String heureDebut,
            @RequestParam String heureFin) {
        Map<String, Object> response = new HashMap<>();
        try {
            entretienService.createEntretienForPosteAndCandidature(postId, candidatureId, dateEntretien, heureDebut, heureFin);
            Optional<Candidature> optionalCandidature = candidatureRepository.findById(candidatureId);

            if (optionalCandidature.isPresent()) {
                // Mettre à jour l'état de la candidature à "Entretien"
                Candidature candidature = optionalCandidature.get();
                candidature.setEtat(EtatPostulation.Entretien);
                candidatureRepository.save(candidature);
                User collaborateur = candidature.getCollaborateur().getCollaborateur();
                User manager = userRepository.findManagerByPostId(postId);

                // Envoyer un email au collaborateur
                String collaborateurEmail = collaborateur.getEmail();
                String collaborateurSubject = "Entretien technique programmé";
                String collaborateurText = "Bonjour " + collaborateur.getCollaborateur().getCollaborateur().getNom()+ ",\n\n" +
                        "Vous avez un entretien technique prévu le " + dateEntretien + " de " + heureDebut + " à " + heureFin +
                        " avec " + manager.getManagerService().getManager().getNom() + ".\n\nCordialement,\nVotre équipe RH";
                emailService.sendEmail(collaborateurEmail, collaborateurSubject, collaborateurText);

                // Envoyer un email au manager
                String managerEmail = manager.getEmail();
                String managerSubject = "Entretien technique avec un collaborateur";
                String managerText = "Bonjour " + manager.getManagerService().getManager().getNom() + ",\n\n" +
                        "Vous avez un entretien technique prévu le " + dateEntretien + " de " + heureDebut + " à " + heureFin +
                        " avec " + collaborateur.getCollaborateur().getCollaborateur().getNom() + ".\n\nCordialement,\nVotre équipe RH";
                emailService.sendEmail(managerEmail, managerSubject, managerText);

                response.put("message", "Entretien créé avec succès et état de la candidature mis à jour à 'Entretien'");
                response.put("status", "success");
                return ResponseEntity.ok(response);
            } else {
                // Si la candidature n'est pas trouvée, renvoyer une réponse de mauvaise requête
                response.put("message", "Candidature non trouvée pour l'ID : " + candidatureId);
                response.put("status", "error");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (IllegalArgumentException e) {
            // En cas d'erreur, renvoyer une réponse de mauvaise requête avec le message d'erreur
            response.put("message", e.getMessage());
            response.put("status", "error");
            return ResponseEntity.badRequest().body(response);
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
    public ResponseEntity<Map<String, Object>> updateEntretien(
            @PathVariable Long id,
            @RequestParam Long candidatureId,
            @RequestParam String dateEntretien,
            @RequestParam String heureDebut,
            @RequestParam String heureFin) {
        Map<String, Object> response = new HashMap<>();
        try {
            entretienService.updateEntretien(id, candidatureId, dateEntretien, heureDebut, heureFin);

            response.put("message", "Entretien updated successfully");
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            response.put("status", "error");
            return ResponseEntity.badRequest().body(response);
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
    public ResponseEntity<List<Map<String, Object>>> getEntretiensTechniquesDuCollaborateurConnecte() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        // Recherche du collaborateur correspondant à l'utilisateur
        Collaborateur collaborateur = collaborateurRepository.findByCollaborateurUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouvé avec l'ID de l'utilisateur : " + userId));

        List<Entretien> entretiensDuCollaborateur = entretienService.getEntretiensByCollaborateurId(collaborateur.getId());

        // Filtrer les entretiens pour ne récupérer que ceux de type "Technique" avec l'état "En_Attente"
        List<Map<String, Object>> entretiensAvecCollaborateurs = entretiensDuCollaborateur.stream()
                .filter(entretien -> entretien.getTypeEntretien() == TypeEntretien.Technique && entretien.getEtatEntretien() == EtatEntretien.En_Attente)
                .map(entretien -> {
                    Map<String, Object> entretienAvecCollaborateur = new HashMap<>();
                    entretienAvecCollaborateur.put("entretien", entretien);

                    Candidature candidature = entretien.getCandidature();
                    if (candidature != null) {
                        Collaborateur collaborateurCandidature = candidature.getCollaborateur();
                        if (collaborateurCandidature != null) {
                            entretienAvecCollaborateur.put("nomCollaborateur", collaborateurCandidature.getCollaborateur().getNom());
                            entretienAvecCollaborateur.put("prenomCollaborateur", collaborateurCandidature.getCollaborateur().getPrenom());
                        }
                    }

                    return entretienAvecCollaborateur;
                })
                .collect(Collectors.toList());

        return new ResponseEntity<>(entretiensAvecCollaborateurs, HttpStatus.OK);
    }


    @GetMapping("/managerService")
    public ResponseEntity<?> getEntretiensTechniquesDumanagerServiceConnecte() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        ManagerService managerService = managerServiceRepository.findByManagerManagerId(userId)
                .orElseThrow(() -> new EntityNotFoundException("ManagerService non trouvé avec l'ID : " + userId));

        List<Poste> postesDuManager = managerService.getPostes();

        if (postesDuManager.isEmpty()) {
            return new ResponseEntity<>("Le manager n'est pas associé à un poste", HttpStatus.NOT_FOUND);
        }

        List<Entretien> entretiensDuManager = new ArrayList<>();
        for (Poste poste : postesDuManager) {
            List<Entretien> entretiensDuPoste = poste.getEntretiens();
            // Filtrer les entretiens pour ne conserver que ceux de type "Technique"
            List<Entretien> entretiensTechniquesDuPoste = entretiensDuPoste.stream()
                    .filter(entretien -> entretien.getTypeEntretien() == TypeEntretien.Technique)
                    .collect(Collectors.toList());
            entretiensDuManager.addAll(entretiensTechniquesDuPoste);
        }

        List<Map<String, Object>> entretiensAvecCollaborateurs = new ArrayList<>();
        for (Entretien entretien : entretiensDuManager) {
            if (entretien.getEtatEntretien() == EtatEntretien.En_Attente) {
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
        }

        if (entretiensAvecCollaborateurs.isEmpty()) {
            return new ResponseEntity<>("Aucun entretien En_Attente de type Technique trouvé pour ce manager", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(entretiensAvecCollaborateurs, HttpStatus.OK);
    }


    @PutMapping("/{id}/noter")
    public ResponseEntity<Map<String, Object>> noterEntretien(
            @PathVariable Long id,
            @RequestParam int note
    ) {
        Map<String, Object> responseBody = new HashMap<>();
        try {
            Entretien entretien = entretienService.getEntretienById(id).orElseThrow(() -> new IllegalArgumentException("Entretien non trouvé"));

            // Noter l'entretien
            entretienService.noterEntretien(id, note);

            // Envoyer un email au candidat (collaborateur)
            String objet = "";
            String contenu = "";
            String emailCandidat = "";
            if (entretien.getTypeEntretien() == TypeEntretien.Annuel) {
                objet = "Résultat entretien annuel";
                contenu = "Bonjour,\n\nNous tenons à vous informer que votre entretien annuel a été évalué.\n\n" +
                        "Voici les détails de l'évaluation :\n\n" +
                        "- Note : " + note + "\n" +
                        "Nous vous remercions pour votre participation et votre engagement.\n\n" +
                        "Cordialement,\n[4You]";
                emailCandidat = entretien.getCollaborateurs().getCollaborateur().getEmail(); // Récupérer l'e-mail du collaborateur pour un entretien annuel
            } else if (entretien.getTypeEntretien() == TypeEntretien.Technique) {
                objet = "Résultat entretien technique";
                // Inclure le titre du poste si l'entretien est technique
                String titrePoste = entretien.getPoste().getTitre(); // Récupérer le titre du poste
                contenu = "Bonjour,\n\nNous vous informons que votre entretien technique pour le poste de \"" + titrePoste + "\" a été évalué.\n\n" +
                        "Voici les détails de l'évaluation :\n\n" +
                        "- Note : " + note + "\n" +
                        "Nous vous remercions pour votre intérêt pour notre entreprise.\n\n" +
                        "Cordialement,\n[4You]";                emailCandidat = entretien.getCandidature().getCollaborateur().getCollaborateur().getEmail(); // Récupérer l'e-mail du collaborateur pour un entretien technique
            }

            sendEmail(emailCandidat, objet, contenu);

            responseBody.put("message", "Entretien noté avec succès");
            return ResponseEntity.ok(responseBody);
        } catch (IllegalArgumentException e) {
            responseBody.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(responseBody);
        }
    }


    private void sendEmail(String to, String subject, String text) {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        try {
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        emailSender.send(message);
    }

    @GetMapping("/EntretiensSpecifiques/{postId}")
    public ResponseEntity<List<Map<String, String>>> getEntretiensSpecifiques(@PathVariable Long postId) {
        List<Entretien> entretiens = entretienService.getEntretiensTechniquesByPoste(postId);
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
    @PostMapping("/ajoutAnnuel")
    public ResponseEntity<Map<String, String>> ajoutEntretienAnnuel(@RequestParam Long collaborateurId,
                                                                    @RequestParam String dateEntretien,
                                                                    @RequestParam String heureDebut,
                                                                    @RequestParam String heureFin) {
        entretienService.ajoutEntretienAnnuel(collaborateurId, dateEntretien, heureDebut, heureFin);

        // Construction de la réponse
        Map<String, String> response = new HashMap<>();
        response.put("message", "Entretien technique ajouté avec succès.");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PutMapping("/updateAnnuel/{entretienId}")
    public ResponseEntity<Map<String, String>> updateEntretienAnnuel(@PathVariable Long entretienId,
                                                           @RequestParam String dateEntretien,
                                                           @RequestParam String heureDebut,
                                                           @RequestParam String heureFin) {
        entretienService.updateEntretienAnnuel(entretienId, dateEntretien, heureDebut, heureFin);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Entretien technique modifie avec succès.");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);    }
    @DeleteMapping("/deleteAnnuel/{entretienId}")
    public ResponseEntity<String> deleteEntretienTechnique(@PathVariable Long entretienId) {
        entretienService.deleteEntretien(entretienId);
        return ResponseEntity.ok("Entretien technique supprimé avec succès.");
    }



    @GetMapping("/entretiens/annuel")
    public ResponseEntity<?> getEntretiensAnnuelDuManagerConnecte() {
        List<Map<String, Object>> entretiensAvecCollaborateurs = entretienService.getEntretiensAnnuelDuManagerConnecte();

        if (entretiensAvecCollaborateurs.isEmpty()) {
            return new ResponseEntity<>("Aucun entretien annuel trouvé pour ce manager", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(entretiensAvecCollaborateurs, HttpStatus.OK);
    }
    @GetMapping("/entretiensCollab/annuel")
    public ResponseEntity<?> getEntretiensAnnuelDuCollabConnecte() {
        List<Map<String, Object>> entretiensAvecCollaborateurs = entretienService.getEntretiensAnnuelDuCollaborateurConnecte();

        if (entretiensAvecCollaborateurs.isEmpty()) {
            return new ResponseEntity<>("Aucun entretien annuel trouvé pour ce manager", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(entretiensAvecCollaborateurs, HttpStatus.OK);
    }
}
