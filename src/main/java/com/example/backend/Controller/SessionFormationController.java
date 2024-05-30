package com.example.backend.Controller;

import com.example.backend.Configuration.MailConfig;
import com.example.backend.Entity.*;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.FormateurRepository;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Security.services.UserDetailsImpl;
import com.example.backend.Service.CollaborateurService;
import com.example.backend.Service.FormateurService;
import com.example.backend.Service.FormationService;
import com.example.backend.Service.SessionFormationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Controller
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/session")
public class SessionFormationController {
    @Autowired
    private SessionFormationService sessionFormationService;
    @Autowired
    private FormateurRepository formateurRepository;
    @Autowired
    private CollaborateurRepository collaborateurRepository;
    @Autowired
    private MailConfig mailConfig;
    @Autowired
    private FormationService formationService;

    @PostMapping("/add/{formationId}")
    public ResponseEntity<SessionFormation> addSessionFormation(@PathVariable("formationId") Long formationId,
                                                                @RequestParam("groupId") Long groupId,
                                                                @RequestParam("dateDebut") String dateDebut,
                                                                @RequestParam("dateFin") String dateFin) {
        SessionFormation newSession = sessionFormationService.addSessionFormation(formationId , groupId, dateDebut, dateFin);
        List<Collaborateur> usersInGroup = sessionFormationService.getUsersInGroup(groupId);
        Formation formation = formationService.getFormationById(formationId);

        // Send email to each user
        for (Collaborateur user : usersInGroup) {
            String to = user.getCollaborateur().getEmail();
            String subject = "Nouvelle session ajoutée";
            String text = "Bonjour " + user.getCollaborateur().getNom() + ",\n\n"
                    + "Nous avons le plaisir de vous informer que vous avez été ajouté à une nouvelle session de formation.\n\n"
                    + "Détails de la session :\n"
                    + "Nom de la formation : " + formation.getTitle() + "\n"
                    + "Date de début : " + dateDebut + "\n"
                    + "Date de fin : " + dateFin + "\n\n"
                    + "Merci de votre attention.\n\n"
                    + "Cordialement,\n"
                    + "L'équipe RH";

            mailConfig.sendEmail(to, subject, text);
        }

        return new ResponseEntity<>(newSession, HttpStatus.CREATED);
    }
    @GetMapping("/allsession/{formationId}")
    public ResponseEntity<List<SessionFormation>> getAllSessionsByFormation(@PathVariable Long formationId) {
        try {
            List<SessionFormation> sessions = sessionFormationService.getAllSessionsByFormation(formationId);
            return new ResponseEntity<>(sessions, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping("/update/{sessionId}")
    public ResponseEntity<SessionFormation> updateSessionFormation(@PathVariable("sessionId") Long sessionId,
                                                                   @RequestParam("groupId") Long groupId,
                                                                   @RequestParam("dateDebut") String dateDebut,
                                                                   @RequestParam("dateFin") String dateFin) {
        try {
            SessionFormation updatedSession = sessionFormationService.updateSessionFormation(sessionId, groupId,dateDebut,dateFin);
            return new ResponseEntity<>(updatedSession, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/get/{sessionId}")
    public ResponseEntity<SessionFormation> getSessionFormationById(@PathVariable Long sessionId) {
        try {
            // Appel de la méthode du service pour récupérer la session de formation par son ID
            SessionFormation session = sessionFormationService.getSessionFormationById(sessionId);

            // Vérification si la session existe
            if (session != null) {
                // Si la session est trouvée, la retourner avec le code de statut OK
                return new ResponseEntity<>(session, HttpStatus.OK);
            } else {
                // Si la session n'est pas trouvée, retourner un code de statut NOT FOUND
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            // Si une exception est levée, retourner un code de statut INTERNAL SERVER ERROR
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{sessionId}")
    public ResponseEntity<HttpStatus> deleteSessionFormation(@PathVariable Long sessionId) {
        try {
            // Appel de la méthode du service pour supprimer la session de formation par son ID
            sessionFormationService.deleteSessionFormation(sessionId);

            // Retourner un code de statut OK si la suppression est réussie
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            // Si une exception est levée, retourner un code de statut INTERNAL SERVER ERROR
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/sessions")
    public List<SessionFormation> getSessions() {
        // Obtenez l'ID du formateur connecté à partir de votre logique d'authentification
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long formateurId = userDetails.getId();

        // Récupérez le formateur connecté
        Formateur formateur = formateurRepository.findByFormateurFormateurId(formateurId)
                .orElseThrow(() -> new EntityNotFoundException("Formateur non trouvé avec l'ID : " + formateurId));

        // Récupérez les sessions de formation associées à ce formateur
        return sessionFormationService.getSessionsByFormateur(formateur);
    }
    @GetMapping("/sessionsCollab")
    public List<SessionFormation> getSessionsCollab() {
        // Obtenez l'ID du collaborateur connecté à partir de votre logique d'authentification
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId(); // ID de l'utilisateur

        // Utilisez la nouvelle méthode pour trouver le Collaborateur par l'ID de l'utilisateur
        Collaborateur collaborateur = collaborateurRepository.findByCollaborateurUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouvé avec l'ID : " + userId));

        // Récupérez les sessions de formation associées à ce collaborateur
        return sessionFormationService.getSessionsByCollaborateur(collaborateur);
    }


}
