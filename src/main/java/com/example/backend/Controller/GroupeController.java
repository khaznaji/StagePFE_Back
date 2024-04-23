package com.example.backend.Controller;

import com.example.backend.Entity.Collaborateur;
import com.example.backend.Entity.Formateur;
import com.example.backend.Entity.Formation;
import com.example.backend.Entity.Groups;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.FormateurRepository;
import com.example.backend.Repository.FormationRepository;
import com.example.backend.Repository.GroupsRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/groups")
public class GroupeController {
    @Autowired
    private GroupsRespository groupsRepository;

    @Autowired
    private FormationRepository formationRepository;

    @Autowired
    private CollaborateurRepository collaborateurRepository;

    @PostMapping("/ajouterGroupe/{formationId}")
    public ResponseEntity<Groups> createGroup(@PathVariable Long formationId,
                                              @RequestParam("nom") String nom,
                                              @RequestParam("collaborateursId") List<Long> collaborateurIds) {
        try {
            Optional<Formation> formationData = formationRepository.findById(formationId);
            if (formationData.isPresent()) {
                Formation formation = formationData.get();
                // Récupérer le formateur associé à la formation
                Formateur formateur = formation.getFormateur();

                // Créer un nouveau groupe avec les données fournies
                Groups newGroup = new Groups();
                newGroup.setNom(nom);
                newGroup.setFormation(formation);
                newGroup.setFormateur(formateur);

                // Récupérer les collaborateurs à partir de leurs IDs
                List<Collaborateur> collaborateurs = collaborateurRepository.findAllById(collaborateurIds);
                newGroup.setCollaborateurs(collaborateurs);

                // Enregistrer le groupe
                Groups savedGroup = groupsRepository.save(newGroup);
                return new ResponseEntity<>(savedGroup, HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }





}
