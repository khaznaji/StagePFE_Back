package com.example.backend.Controller;

import com.example.backend.Entity.*;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.FormateurRepository;
import com.example.backend.Repository.FormationRepository;
import com.example.backend.Security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/Formation")

public class FormationController {
    @Autowired
    FormateurRepository formateurRepository;
    @Autowired
    CollaborateurRepository collaborateurRepository;
    @Autowired
    FormationRepository formationRepository;
    @PostMapping("/create")
    public ResponseEntity<?> createFormation(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("disponibilite") boolean disponibilite,
            @RequestParam("chapitre") int chapitre,
            @RequestParam("duree") int duree,
            @RequestParam("departement") Departement departement,
            @RequestParam("image") MultipartFile image
    ) {
        if (image.isEmpty()) {
            return new ResponseEntity<>("Veuillez sélectionner une image", HttpStatus.BAD_REQUEST);
        }

        try {
            // Obtenez le chemin du dossier pour enregistrer l'image
            String folderPath = "C:\\Users\\olkhaznaji\\Desktop\\StagePFE\\Frontend\\src\\assets\\images\\Formations\\";

            // Obtenez le nom de fichier original
            String originalFilename = image.getOriginalFilename();

            // Générez un nom de fichier unique avec un timestamp
            String uniqueFileName = UUID.randomUUID() + "_" + System.currentTimeMillis() + "_" + originalFilename;

            // Définissez le chemin complet du fichier
            String filePath = folderPath + uniqueFileName;

            // Créez les répertoires s'ils n'existent pas déjà
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // Enregistrez le fichier dans le dossier spécifié
            Path path = Paths.get(filePath);
            Files.write(path, image.getBytes());

            // Obtenez l'authentification actuelle pour récupérer l'ID du formateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long managerServiceId = userDetails.getId();
            Formateur managerService = formateurRepository.findByFormateurFormateurId(managerServiceId)
                    .orElseThrow(() -> new EntityNotFoundException("ManagerService non trouvé avec l'ID : " + managerServiceId));

            // Assurez-vous que le formateur existe
            if (managerService == null) {
                return new ResponseEntity<>("Formateur non trouvé", HttpStatus.NOT_FOUND);
            }

            // Créez une nouvelle formation
            Formation formation = new Formation();
            formation.setTitle(title);
            formation.setDepartment(departement);

            formation.setDuree(duree);
            formation.setChapitre(chapitre);
            formation.setDescription(description);
            formation.setDisponibilite(disponibilite);
            formation.setImage(uniqueFileName); // Enregistrez le nom de fichier unique dans la base de données
            formation.setCreatedAt(LocalDateTime.now());
            formation.setFormateur(managerService);

            Formation savedFormation =formationRepository.save(formation);
            ;

            return new ResponseEntity<>(savedFormation, HttpStatus.CREATED);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Erreur lors de l'enregistrement de l'image", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping("/edit/{formationId}")
    public ResponseEntity<?> editFormation(
            @PathVariable Long formationId,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("disponibilite") boolean disponibilite,
            @RequestParam("chapitre") int chapitre,
            @RequestParam("duree") int duree,
            @RequestParam("departement") Departement departement,
            @RequestParam(value = "image", required = false) MultipartFile image // Marquez le paramètre comme facultatif pour autoriser les mises à jour sans changement d'image
    ) {
        // Vérifiez si la formation existe
        Formation existingFormation = formationRepository.findById(formationId)
                .orElseThrow(() -> new EntityNotFoundException("Formation non trouvée avec l'ID : " + formationId));

        // Mettez à jour les propriétés de la formation avec les nouvelles valeurs
        existingFormation.setTitle(title);
        existingFormation.setDescription(description);
        existingFormation.setDisponibilite(disponibilite);
        existingFormation.setChapitre(chapitre);
        existingFormation.setDuree(duree);
        existingFormation.setDepartment(departement);

        // Vérifiez s'il y a une nouvelle image à enregistrer
        if (image != null && !image.isEmpty()) {
            try {
                // Générez un nom de fichier unique avec un timestamp
                String originalFilename = image.getOriginalFilename();
                String uniqueFileName = UUID.randomUUID() + "_" + System.currentTimeMillis() + "_" + originalFilename;

                // Obtenez le chemin complet du dossier pour enregistrer l'image
                String folderPath = "C:\\Users\\olkhaznaji\\Desktop\\StagePFE\\Frontend\\src\\assets\\images\\Formations\\";
                String filePath = folderPath + uniqueFileName;

                // Enregistrez le fichier dans le dossier spécifié
                Path path = Paths.get(filePath);
                Files.write(path, image.getBytes());

                // Supprimez l'ancienne image s'il y en a une
                if (existingFormation.getImage() != null && !existingFormation.getImage().isEmpty()) {
                    String oldFilePath = folderPath + existingFormation.getImage();
                    Files.deleteIfExists(Paths.get(oldFilePath));
                }

                // Mettez à jour le nom de fichier de l'image dans la base de données
                existingFormation.setImage(uniqueFileName);
            } catch (IOException e) {
                e.printStackTrace();
                return new ResponseEntity<>("Erreur lors de l'enregistrement de la nouvelle image", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        // Sauvegardez les modifications dans la base de données
        Formation updatedFormation = formationRepository.save(existingFormation);

        return new ResponseEntity<>(updatedFormation, HttpStatus.OK);
    }
    @DeleteMapping("/delete/{formationId}")
    public ResponseEntity<?> deleteFormation(@PathVariable Long formationId) {
        // Recherchez la formation par son identifiant
        Optional<Formation> formationOptional = formationRepository.findById(formationId);

        // Vérifiez si la formation existe
        if (formationOptional.isPresent()) {
            // Si la formation existe, supprimez-la de la base de données
            Formation formation = formationOptional.get();

            // Supprimez l'image associée s'il y en a une
            if (formation.getImage() != null && !formation.getImage().isEmpty()) {
                try {
                    // Obtenez le chemin complet du dossier contenant l'image
                    String folderPath = "C:\\Users\\olkhaznaji\\Desktop\\StagePFE\\Frontend\\src\\assets\\images\\Formations\\";
                    String filePath = folderPath + formation.getImage();

                    // Supprimez le fichier image
                    Files.deleteIfExists(Paths.get(filePath));
                } catch (IOException e) {
                    e.printStackTrace();
                    return new ResponseEntity<>("Erreur lors de la suppression de l'image", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }

            // Supprimez la formation de la base de données
            formationRepository.delete(formation);

            // Renvoyez une réponse OK avec un message de succès
            return ResponseEntity.ok("Formation supprimée avec succès");
        } else {
            // Si la formation n'existe pas, renvoyez une réponse avec un statut NOT_FOUND
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/mesformations")
    public ResponseEntity<List<Formation>> mesformations() {
        // Obtenez l'authentification actuelle pour récupérer l'ID du formateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long managerServiceId = userDetails.getId();
        Formateur managerService = formateurRepository.findByFormateurFormateurId(managerServiceId)
                .orElseThrow(() -> new EntityNotFoundException("ManagerService non trouvé avec l'ID : " + managerServiceId));



        List<Formation> formations = formationRepository.findAllByFormateur(managerService);


        return ResponseEntity.ok(formations);
    }
    @GetMapping("/{formationId}")
    public ResponseEntity<?> getFormationById(@PathVariable Long formationId) {
        // Recherchez la formation par son identifiant
        Optional<Formation> formationOptional = formationRepository.findById(formationId);

        // Vérifiez si la formation existe
        if (formationOptional.isPresent()) {
            // Si la formation existe, renvoyez-la dans la réponse
            return ResponseEntity.ok(formationOptional.get());
        } else {
            // Si la formation n'existe pas, renvoyez une réponse avec un statut NOT_FOUND
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/getFormationByIdForCollab/{formationId}")
    public ResponseEntity<?> getFormationByIdForCollab(@PathVariable Long formationId) {
        // Recherchez la formation par son identifiant
        Optional<Formation> formationOptional = formationRepository.findById(formationId);

        // Vérifiez si la formation existe
        if (formationOptional.isPresent()) {
            // Récupérez la formation
            Formation formation = formationOptional.get();

            // Chargez le formateur associé à la formation
            Formateur formateur = formation.getFormateur();

            // Vérifiez si le formateur existe
            if (formateur != null) {
                // Récupérez le nom, le prénom et l'image du formateur

                // Assignez les informations du formateur à la formation
                formation.setFormateurName(formateur.getFomarteur().getNom() + " " + formateur.getFomarteur().getPrenom());
                formation.setFormateurImage(formateur.getFomarteur().getImage()); // Vous devrez peut-être ajuster cela en fonction de la structure de votre objet Formateur


                // Renvoyez la formation avec les informations du formateur dans la réponse
                return ResponseEntity.ok(formation);
            } else {
                // Si le formateur n'existe pas, renvoyez une réponse avec un statut NOT_FOUND
                return ResponseEntity.notFound().build();
            }
        } else {
            // Si la formation n'existe pas, renvoyez une réponse avec un statut NOT_FOUND
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/formationsCollaborateur")
    public ResponseEntity<List<Formation>> formationsCollaborateur() {
        // Obtenez l'authentification actuelle pour récupérer l'utilisateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Récupérez le collaborateur à partir de l'utilisateur connecté
        Collaborateur collaborateur = collaborateurRepository.findByCollaborateurUserId(userDetails.getId())
                .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouvé avec l'ID de l'utilisateur : " + userDetails.getId()));

        List<Formation> formations;
        if (collaborateur.getDepartment() != null) {
            formations = formationRepository.findByDepartment(collaborateur.getDepartment());
        } else {
            formations = formationRepository.findAll(); // Si le département du collaborateur n'est pas défini, récupérez toutes les formations
        }

        // Parcourez chaque formation et récupérez le nom et le prénom du formateur associé
        for (Formation formation : formations) {
            Formateur formateur = formation.getFormateur(); // Récupérez l'objet Formateur associé à la formation
            if (formateur != null) {
                // Assignez le nom et le prénom du formateur à la formation
                formation.setFormateurName(formateur.getFomarteur().getNom() + " " + formateur.getFomarteur().getPrenom());
                formation.setFormateurImage(formateur.getFomarteur().getImage()); // Vous devrez peut-être ajuster cela en fonction de la structure de votre objet Formateur
            }
        }

        return ResponseEntity.ok(formations);
    }





}
