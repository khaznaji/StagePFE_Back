package com.example.backend.Controller;

import com.example.backend.Entity.Collaborateur;
import com.example.backend.Entity.Competence;
import com.example.backend.Entity.Evaluation;
import com.example.backend.Entity.User;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.EvaluationRepository;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Security.services.UserDetailsImpl;
import com.example.backend.Service.CollaborateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/Collaborateur")
public class CollaborateurController {
    @Autowired
    private CollaborateurRepository collaborateurRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EvaluationRepository evaluationRepository;


    @PutMapping("/updateProfileCollaborateur")
    public ResponseEntity<Map<String, String>> updateCollaborateurProfile(
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam(value = "competences", required = false) List<Competence> competences,
            @RequestParam(value = "evaluations", required = false) List<Integer> evaluations,
            @RequestParam(value = "resume", required = false) MultipartFile resume
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();

        Map<String, String> response = new HashMap<>();

        // Update the bio if provided
        if (bio != null) {
            Collaborateur collaborateur = user.getCollaborateur();
            collaborateur.setBio(bio);
            collaborateurRepository.save(collaborateur);
            response.put("bio", "Bio updated successfully");
        }

        // Update the competences if provided
        if (competences != null) {
            Collaborateur collaborateur = user.getCollaborateur();
            collaborateur.setCompetences(competences);

            // Add evaluations for each competence
            if (evaluations != null && evaluations.size() == competences.size()) {
                for (int i = 0; i < competences.size(); i++) {
                    Competence competence = competences.get(i);
                    int evaluationScore = evaluations.get(i);

                    // Add the evaluation
                    Evaluation evaluation = new Evaluation();
                    evaluation.setCollaborateur(collaborateur);
                    evaluation.setCompetence(competence);
                    evaluation.setEvaluation(evaluationScore);

                    // Save the evaluation
                    evaluationRepository.save(evaluation);
                }
            }

            collaborateurRepository.save(collaborateur);
            response.put("competences", "Competences updated successfully");
        }

        // Handle PDF file upload
        // Handle PDF file upload
        if (resume != null) {
            try {
                // Directory where PDF files will be stored
                String uploadDir = "C:\\Users\\olkhaznaji\\Desktop\\StagePFE\\Frontend\\src\\assets\\Resume\\";

                // Create unique file name for the PDF
                String fileName = UUID.randomUUID().toString() + "_" + resume.getOriginalFilename();

                // Path to save the PDF file
                Path filePath = Paths.get(uploadDir + fileName);

                // Save the PDF file to the server
                Files.copy(resume.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Save the file path to the database
                Collaborateur collaborateur = user.getCollaborateur();

                // Delete the previous resume file if it exists
                String previousResumePath = collaborateur.getResume();
                if (previousResumePath != null) {
                    try {
                        Path previousFilePath = Paths.get(previousResumePath);
                        Files.deleteIfExists(previousFilePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                        // Handle file deletion error
                        response.put("error", "Failed to delete previous Resume file");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                    }
                }

                // Set the new resume path in the collaborateur entity
// Construire le chemin relatif pour enregistrer le fichier
                String relativeFilePath = "Resume/" + fileName;

// Sauvegarder le chemin relatif dans la base de données
                collaborateur.setResume(relativeFilePath);

                collaborateurRepository.save(collaborateur);

                response.put("resume", "Resume updated successfully");
            } catch (IOException e) {
                e.printStackTrace();
                // Handle file upload error
                response.put("error", "Failed to upload Resume file");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }


        // Return success message
        response.put("success", "Profile updated successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/updateCollaborateurBio")
    public ResponseEntity<Map<String, String>> updateCollaborateurBio(
            @RequestParam(value = "bio", required = false) String bio

    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();

        Map<String, String> response = new HashMap<>();

        // Update the bio if provided
        if (bio != null) {
            Collaborateur collaborateur = user.getCollaborateur();
            collaborateur.setBio(bio);
            collaborateurRepository.save(collaborateur);
            response.put("bio", "Bio updated successfully");
        }
        // Return success message
        response.put("success", "Profile updated successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/updateCollaborateurResume")
    public ResponseEntity<Map<String, String>> updateCollaborateurResume(

            @RequestParam(value = "resume", required = false) MultipartFile resume
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();

        Map<String, String> response = new HashMap<>();

        if (resume != null) {
            try {
                // Directory where PDF files will be stored
                String uploadDir = "C:\\Users\\olkhaznaji\\Desktop\\StagePFE\\Frontend\\src\\assets\\Resume\\";

                // Create unique file name for the PDF
                String fileName = UUID.randomUUID().toString() + "_" + resume.getOriginalFilename();

                // Path to save the PDF file
                Path filePath = Paths.get(uploadDir + fileName);

                // Save the PDF file to the server
                Files.copy(resume.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Save the file path to the database
                Collaborateur collaborateur = user.getCollaborateur();

                // Delete the previous resume file if it exists
                String previousResumePath = collaborateur.getResume();
                if (previousResumePath != null) {
                    try {
                        Path previousFilePath = Paths.get(previousResumePath);
                        Files.deleteIfExists(previousFilePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                        // Handle file deletion error
                        response.put("error", "Failed to delete previous Resume file");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                    }
                }

                String relativeFilePath = "Resume/" + fileName;

                collaborateur.setResume(relativeFilePath);

                collaborateurRepository.save(collaborateur);

                response.put("resume", "Resume updated successfully");
            } catch (IOException e) {
                e.printStackTrace();
                // Handle file upload error
                response.put("error", "Failed to upload Resume file");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }


        // Return success message
        response.put("success", "Profile updated successfully");
        return ResponseEntity.ok(response);
    }
    @GetMapping("/collaborateur/info")
        public Map<String, Object> getCollaborateurInfo(@AuthenticationPrincipal UserDetails userDetails) {
            // Récupérez le Collaborateur connecté
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Collaborateur collaborateur = user.getCollaborateur();

            // Construisez le map pour contenir les informations
            Map<String, Object> collaborateurInfo = new HashMap<>();
            collaborateurInfo.put("nom", user.getNom());
            collaborateurInfo.put("prenom", user.getPrenom());
            collaborateurInfo.put("poste", collaborateur.getPoste());
            collaborateurInfo.put("dateEntree", collaborateur.getDateEntree());

            collaborateurInfo.put("managerName", collaborateur.getCollaborateur().getNom());
            collaborateurInfo.put("managerPrenom", collaborateur.getCollaborateur().getPrenom()); // Assurez-vous que ManagerService a un nom
// Assurez-vous que ManagerService a un nom
            collaborateurInfo.put("departement", collaborateur.getDepartment().name()); // Assurez-vous que Departement est un enum
            collaborateurInfo.put("bio", collaborateur.getBio());
            collaborateurInfo.put("image", user.getImage());
            collaborateurInfo.put("resume", collaborateur.getResume());

            // Récupérez les évaluations des compétences
        List<Map<String, Object>> evaluations = collaborateur.getCompetences().stream()
                .filter(competence -> {
                    Evaluation evaluation = evaluationRepository.findByCollaborateurAndCompetence(collaborateur, competence)
                            .orElse(null);
                    return evaluation != null; // Garder uniquement les compétences avec évaluation
                })
                .map(competence -> {
                    Evaluation evaluation = evaluationRepository.findByCollaborateurAndCompetence(collaborateur, competence)
                            .orElse(null);
                    Map<String, Object> evaluationInfo = new HashMap<>();
                    evaluationInfo.put("competenceName", competence.getNom());
                    evaluationInfo.put("evaluation", evaluation.getEvaluation());
                    return evaluationInfo;
                })
                .collect(Collectors.toList());

        collaborateurInfo.put("evaluations", evaluations);

            return collaborateurInfo;
        }
    }

