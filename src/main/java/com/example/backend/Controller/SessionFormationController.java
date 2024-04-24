package com.example.backend.Controller;

import com.example.backend.Entity.Collaborateur;
import com.example.backend.Entity.Formateur;
import com.example.backend.Entity.ModaliteSession;
import com.example.backend.Entity.SessionFormation;
import com.example.backend.Service.CollaborateurService;
import com.example.backend.Service.FormateurService;
import com.example.backend.Service.SessionFormationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/add/{formationId}")
    public ResponseEntity<SessionFormation> addSessionFormation(@PathVariable("formationId") Long formationId,
                                                                @RequestParam("groupId") Long groupId,
                                                                @RequestParam("dateDebut") String dateDebut,
                                                                @RequestParam("dateFin") String dateFin) {
        SessionFormation newSession = sessionFormationService.addSessionFormation(formationId , groupId, dateDebut, dateFin);
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


}
