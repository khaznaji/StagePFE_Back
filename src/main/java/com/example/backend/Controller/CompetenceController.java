package com.example.backend.Controller;

import com.example.backend.Entity.Competence;
import com.example.backend.Service.ICompetenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/Competence")
public class CompetenceController {
    private final ICompetenceService competenceService;

    @Autowired
    public CompetenceController(ICompetenceService competenceService) {
        this.competenceService = competenceService;
    }

    @PostMapping
    public ResponseEntity<Competence> addCompetence(@RequestBody Competence competence) {
        Competence addedCompetence = competenceService.addCompetence(competence);
        return new ResponseEntity<>(addedCompetence, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Competence> getCompetenceById(@PathVariable Long id) {
        Competence competence = competenceService.getCompetenceById(id);
        return new ResponseEntity<>(competence, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Competence>> getAllCompetences() {
        List<Competence> competences = competenceService.getAllCompetences();
        return new ResponseEntity<>(competences, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Competence> updateCompetence(@PathVariable Long id, @RequestBody Competence newCompetence) {
        Competence updatedCompetence = competenceService.updateCompetence(id, newCompetence);
        return new ResponseEntity<>(updatedCompetence, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompetence(@PathVariable Long id) {
        competenceService.deleteCompetence(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
