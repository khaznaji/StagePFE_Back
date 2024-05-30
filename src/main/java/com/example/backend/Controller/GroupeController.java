package com.example.backend.Controller;

import com.example.backend.Entity.*;
import com.example.backend.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.*;
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
    @Autowired
    private ParticipationFormationRepository participationFormationRepository;
    @PostMapping("/ajouterGroupe/{formationId}")
    public ResponseEntity<Groups> createGroup(@PathVariable Long formationId,
                                              @RequestParam("nom") String nom,
                                              @RequestParam("collaborateursId") List<Long> collaborateursId) {
        try {
            Optional<Formation> formationData = formationRepository.findById(formationId);
            if (formationData.isPresent()) {
                Formation formation = formationData.get();
                // Récupérer le formateur associé à la formation
                Formateur formateur = formation.getFormateur();

                // Créer un nouveau groupe avec les données fournies
                Groups newGroup = new Groups();
                newGroup.setNom(nom);
                newGroup.setEtat(Etat.En_Attente);
                newGroup.setFormation(formation);
                newGroup.setFormateur(formateur);
                List<Collaborateur> collaborateurs = collaborateurRepository.findAllById(collaborateursId);
                newGroup.setCollaborateurs(collaborateurs);
                // Récupérer les collaborateurs à partir de leurs IDs
                for (Collaborateur collaborateur : collaborateurs) {
                    // Recherche de la participation existante pour cet utilisateur et cette formation
                    ParticipationFormation participation = participationFormationRepository.findByCollaborateurAndFormation(collaborateur, formation);
                    if (participation != null) {
                        // Mise à jour de l'état de participation existant
                        participation.setEtat(EtatParticipation.AFFECTE);
                        participationFormationRepository.save(participation);
                    } else {
                        // Si aucune participation n'existe, vous pouvez choisir de ne rien faire ou de lever une exception
                    }
                }

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



    @PutMapping("/editerNomGroupe/{groupId}")
    public ResponseEntity<Groups> editGroupName(@PathVariable Long groupId,
                                                @RequestParam("nom") String nom) {
        try {
            Optional<Groups> groupData = groupsRepository.findById(groupId);
            if (groupData.isPresent()) {
                Groups group = groupData.get();
                group.setNom(nom);
                Groups groupeMaj = groupsRepository.save(group);
                return new ResponseEntity<>(groupeMaj, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/detailsGroupe/{groupId}")
    public ResponseEntity<Map<String, Object>> getGroupDetails(@PathVariable Long groupId) {
        try {
            Optional<Groups> groupData = groupsRepository.findById(groupId);
            if (groupData.isPresent()) {
                Groups group = groupData.get();
                Formateur formateur = group.getFormateur();
                List<Collaborateur> collaborateurs = group.getCollaborateurs();

                Map<String, Object> groupDetails = new HashMap<>();
                groupDetails.put("group", group);
                groupDetails.put("formateurNom", formateur.getFormateur().getNom());
                groupDetails.put("formateurPrenom", formateur.getFormateur().getPrenom());

                List<Map<String, Object>> collaborateursList = new ArrayList<>();
                for (Collaborateur collaborateur : collaborateurs) {
                    Map<String, Object> collaborateurDetails = new HashMap<>();
                    collaborateurDetails.put("id", collaborateur.getCollaborateur().getId());

                    collaborateurDetails.put("nom", collaborateur.getCollaborateur().getNom());
                    collaborateurDetails.put("prenom", collaborateur.getCollaborateur().getPrenom());
                    collaborateurDetails.put("image", collaborateur.getCollaborateur().getImage());
                    collaborateursList.add(collaborateurDetails);
                }
                groupDetails.put("collaborateurs", collaborateursList);

                return new ResponseEntity<>(groupDetails, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/ajouterCollaborateursAuGroupe/{groupId}/{formationId}")
    public ResponseEntity<Groups> addCollaborateursToGroup(@PathVariable Long groupId,
                                                           @PathVariable Long formationId,
                                                           @RequestParam("collaborateursId") List<Long> collaborateursId) {
        try {
            Optional<Groups> groupData = groupsRepository.findById(groupId);
            if (groupData.isPresent()) {
                Groups group = groupData.get();

                // Vérifier si le groupe appartient à la formation spécifiée
                if (group.getFormation().getId().equals(formationId)) {
                    // Récupérer les collaborateurs à partir de leurs IDs
                    List<Collaborateur> collaborateursToAdd = collaborateurRepository.findAllById(collaborateursId);

                    // Mettre à jour l'état de participation des collaborateurs existants
                    for (Collaborateur collaborateur : collaborateursToAdd) {
                        // Recherche de la participation existante pour cet utilisateur et cette formation
                        ParticipationFormation participation = participationFormationRepository.findByCollaborateurAndFormation(collaborateur, group.getFormation());
                        if (participation != null) {
                            // Mise à jour de l'état de participation existant
                            participation.setEtat(EtatParticipation.AFFECTE);
                            participationFormationRepository.save(participation);
                        } else {
                            // Si aucune participation n'existe, vous pouvez choisir de ne rien faire ou de lever une exception
                        }
                    }

                    // Ajouter les nouveaux collaborateurs à la liste existante
                    List<Collaborateur> currentCollaborateurs = group.getCollaborateurs();
                    currentCollaborateurs.addAll(collaborateursToAdd);
                    group.setCollaborateurs(currentCollaborateurs);

                    // Enregistrer les modifications du groupe
                    Groups updatedGroup = groupsRepository.save(group);

                    return new ResponseEntity<>(updatedGroup, HttpStatus.OK);
                } else {
                    // Le groupe n'appartient pas à la formation spécifiée
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            } else {
                // Le groupe n'est pas trouvé
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @DeleteMapping("/supprimerUtilisateurDuGroupe/{groupId}/{userId}")
    public ResponseEntity<HttpStatus> removeUserFromGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        try {
            Optional<Groups> groupData = groupsRepository.findById(groupId);
            if (groupData.isPresent()) {
                Groups group = groupData.get();

                // Vérifier si l'utilisateur fait partie du groupe
                List<Collaborateur> collaborateurs = group.getCollaborateurs();
                Collaborateur collaborateurToRemove = collaborateurs.stream()
                        .filter(c -> c.getCollaborateur().getId().equals(userId))
                        .findFirst()
                        .orElse(null);

                if (collaborateurToRemove != null) {
                    // Mettre à jour l'état de participation de l'utilisateur à "En_Attente" par exemple
                    // Vous pouvez ajuster l'état selon votre logique métier
                    ParticipationFormation participation = participationFormationRepository.findByCollaborateurAndFormation(collaborateurToRemove, group.getFormation());
                    if (participation != null) {
                        participation.setEtat(EtatParticipation.CONFIRME); // Ou tout autre état approprié
                        participationFormationRepository.save(participation);
                    } else {
                        // Si aucune participation n'existe, vous pouvez choisir de ne rien faire ou de lever une exception
                    }

                    // Retirer l'utilisateur du groupe
                    collaborateurs.remove(collaborateurToRemove);
                    groupsRepository.save(group);
                    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                } else {
                    // L'utilisateur n'est pas trouvé dans le groupe
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
            } else {
                // Le groupe n'est pas trouvé
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            // Erreur serveur interne
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @GetMapping("/groupesParFormation/{formationId}")
    public ResponseEntity<List<Map<String, Object>>> getGroupesByFormation(@PathVariable Long formationId) {
        try {
            Optional<Formation> formationData = formationRepository.findById(formationId);
            if (formationData.isPresent()) {
                Formation formation = formationData.get();
                List<Groups> groupes = groupsRepository.findByFormation(formation);
                List<Map<String, Object>> groupesAvecFormateurs = new ArrayList<>();

                // Pour chaque groupe, vérifier s'il y a un formateur associé et récupérer son image
                for (Groups groupe : groupes) {
                    Formateur formateur = groupe.getFormateur(); // Supposons que la relation entre Group et User soit représentée par la méthode getFormateur()
                    Map<String, Object> groupeAvecFormateur = new HashMap<>();
                    groupeAvecFormateur.put("groupe", groupe);

                    if (formateur != null) {
                        // Récupérer l'URL de l'image du formateur
                        String formateurImageUrl = formateur.getFormateur().getImage();
                        String formateurnom = formateur.getFormateur().getNom();
                        String formateurprenom = formateur.getFormateur().getPrenom();
                        // Supposons que getImageUrl() récupère l'URL de l'image du formateur
                        // Ajouter l'URL de l'image du formateur au groupe
                        groupeAvecFormateur.put("formateurImageUrl", formateurImageUrl);
                        groupeAvecFormateur.put("formateurnom", formateurnom);
                        groupeAvecFormateur.put("formateurprenom", formateurprenom);
                        int nombreMembres = groupe.getCollaborateurs().size(); // Supposons que la relation entre Group et Membre soit représentée par la méthode getMembres()
                        groupeAvecFormateur.put("nombreMembres", nombreMembres);

                        // Récupérer les images des trois premiers collaborateurs du groupe
                        List<String> collaborateurImages = new ArrayList<>();
                        List<Collaborateur> collaborateurs = groupe.getCollaborateurs(); // Supposons que la relation entre Group et Collaborateur soit représentée par la méthode getCollaborateurs()
                        for (int i = 0; i < Math.min(3, collaborateurs.size()); i++) {
                            String collaborateurImage = collaborateurs.get(i).getCollaborateur().getImage(); // Supposons que getImage() récupère l'URL de l'image du collaborateur
                            collaborateurImages.add(collaborateurImage);
                        }
                        groupeAvecFormateur.put("collaborateurImages", collaborateurImages);
                    }

                    groupesAvecFormateurs.add(groupeAvecFormateur);
                }

                return new ResponseEntity<>(groupesAvecFormateurs, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @DeleteMapping("/supprimerGroupe/{groupId}")
    public ResponseEntity<HttpStatus> deleteGroup(@PathVariable Long groupId) {
        try {
            Optional<Groups> groupData = groupsRepository.findById(groupId);
            if (groupData.isPresent()) {
                Groups group = groupData.get();
                groupsRepository.delete(group);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




}
