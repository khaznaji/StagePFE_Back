package com.example.backend.Controller;

import com.example.backend.Configuration.MailConfig;
import com.example.backend.Entity.*;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.EvaluationRepository;
import com.example.backend.Repository.ManagerServiceRepository;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Security.services.UserDetailsImpl;
import com.example.backend.Security.verificationCode.CodeVerification;
import com.example.backend.Security.verificationCode.CodeVerificationServiceImpl;
import com.example.backend.Service.CollaborateurService;
import com.example.backend.exception.EmailAlreadyExistsException;
import com.example.backend.exception.ManagerServiceNotFoundException;
import com.example.backend.exception.MatriculeAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ManagerServiceRepository managerServiceRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CodeVerificationServiceImpl codeVerificationService;

    @Autowired
    private MailConfig emailService;
    @Autowired
    private CollaborateurRepository collaborateurRepository;
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
        if (bio != null) {
            Collaborateur collaborateur = user.getCollaborateur();
            collaborateur.setBio(bio);
            collaborateurRepository.save(collaborateur);
            response.put("bio", "Bio updated successfully");
        }
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
        collaborateurInfo.put("isVerified", collaborateur.isVerified()); // Ajoutez cette ligne

        collaborateurInfo.put("dateEntree", collaborateur.getDateEntree());
        String managerName = collaborateur.getManagerService().getManager().getNom();
        String managerPrenom = collaborateur.getManagerService().getManager().getPrenom();
        String managerImage = collaborateur.getManagerService().getManager().getImage();

// Maintenant vous pouvez les utiliser comme vous le souhaitez
        collaborateurInfo.put("managerName", managerName);
        collaborateurInfo.put("managerPrenom", managerPrenom);
        collaborateurInfo.put("managerImage", managerImage);

// Assurez-vous que ManagerService a un nom
            collaborateurInfo.put("departement", collaborateur.getDepartment().name()); // Assurez-vous que Departement est un enum
            collaborateurInfo.put("bio", collaborateur.getBio());
            collaborateurInfo.put("image", user.getImage());
            collaborateurInfo.put("resume", collaborateur.getResume());

            // Récupérez les évaluations des compétences
        List<Map<String, Object>> evaluations = collaborateur.getEvaluations().stream()
                .map(evaluation -> {
                    Map<String, Object> evaluationInfo = new HashMap<>();
                    evaluationInfo.put("competenceName", evaluation.getCompetence().getNom());
                    evaluationInfo.put("domaine", evaluation.getCompetence().getDomaine());

                    evaluationInfo.put("evaluation", evaluation.getEvaluation());
                    return evaluationInfo;
                })
                .collect(Collectors.toList());


        collaborateurInfo.put("evaluations", evaluations);

            return collaborateurInfo;
        }
    @PostMapping("/registerCollaborateur")
    public ResponseEntity<?> registerCollaborateur(
            @RequestParam("nom") String nom,
            @RequestParam("prenom") String prenom,
            @RequestParam("numtel") int numtel,
            @RequestParam("matricule") String matricule,
            @RequestParam("email") String email,
            @RequestParam("gender") Gender gender,
            @RequestParam("poste") String poste,
            @RequestParam("managerServiceId") Long managerServiceId,
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam(value = "dateEntree", required = false) String dateEntree
    ) {
        try {
            User manager = new User();
            manager.setNom(nom);
            manager.setPrenom(prenom);
            manager.setNumtel(numtel);
            manager.setMatricule(matricule);
            manager.setRole(Role.Collaborateur);
            manager.setEmail(email);
            manager.setPassword(passwordEncoder.encode("SopraHR2024"));
            manager.setDate(LocalDateTime.now());
            manager.setActivated(false);
            manager.setGender(gender);
            manager.setImage(gender == Gender.Femme ? "avatar/femme.png" : "avatar/homme.png");
            User manager1 = userRepository.findByRoleAndId(Role.ManagerService, managerServiceId)
                    .orElseThrow(() -> new ManagerServiceNotFoundException("Manager not found with id: " + managerServiceId));

            User registeredManager = userRepository.save(manager);
            Collaborateur request = new Collaborateur();
            request.setCollaborateur(registeredManager);
            request.setManagerService(manager1.getManagerService());
            request.setDepartment(manager1.getDepartment());
            request.setPoste(poste);
            request.setBio(bio);
            request.setDateEntree(dateEntree);
            collaborateurRepository.save(request);
            // Call your service method to register the manager service
            CodeVerification verificationCode = codeVerificationService.createToken(manager);
            String resetLink = "http://localhost:4200/activate-account?token=" + verificationCode.getToken();
            emailService.sendWelcomeEmail(manager.getEmail(),manager.getNom(), resetLink, verificationCode.getActivationCode());
            return new ResponseEntity<>(manager, HttpStatus.CREATED);
        } catch (EmailAlreadyExistsException | MatriculeAlreadyExistsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    }

