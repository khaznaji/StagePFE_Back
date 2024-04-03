package com.example.backend.Controller;

import com.example.backend.Entity.Candidature;
import com.example.backend.Entity.Collaborateur;
import com.example.backend.Entity.Entretien;
import com.example.backend.Entity.Poste;
import com.example.backend.Repository.CandidatureRepository;
import com.example.backend.Service.EntretienService;
import com.example.backend.Service.PosteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
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
    private  PosteService posteService;


    @GetMapping
    public ResponseEntity<List<Entretien>> getAllEntretiens() {
        List<Entretien> entretiens = entretienService.getAllEntretiens();
        return new ResponseEntity<>(entretiens, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Entretien> getEntretienById(@PathVariable Long id) {
        Optional<Entretien> entretien = entretienService.getEntretienById(id);
        return entretien.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
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

    @PutMapping("/{id}")
    public ResponseEntity<Entretien> updateEntretien(@PathVariable Long id, @RequestBody Entretien entretien) {
        Entretien updatedEntretien = entretienService.updateEntretien(id, entretien);
        if (updatedEntretien != null) {
            return new ResponseEntity<>(updatedEntretien, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntretien(@PathVariable Long id) {
        entretienService.deleteEntretien(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
