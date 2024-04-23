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



    @PostMapping("/add")
    public ResponseEntity<SessionFormation> addSessionFormation(@RequestParam("groupId") Long groupId,
                                                                @RequestParam("dateDebut") String dateDebut,
                                                                @RequestParam("dateFin") String dateFin,
                                                                @RequestParam("roomId") String roomId,
                                                                @RequestParam("modaliteSession") ModaliteSession modaliteSession) {
        SessionFormation newSession = sessionFormationService.addSessionFormation(groupId, dateDebut, dateFin, roomId, modaliteSession);
        return new ResponseEntity<>(newSession, HttpStatus.CREATED);
    }
    public String generateRandomRoomId() {
        // Génère un identifiant UUID aléatoire
        return UUID.randomUUID().toString();
    }
}
