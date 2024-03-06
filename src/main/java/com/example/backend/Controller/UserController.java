package com.example.backend.Controller;
import com.example.backend.Configuration.MailConfig;
import com.example.backend.Entity.*;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.ManagerServiceRepository;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Security.jwt.JwtUtils;
import com.example.backend.Security.services.UserDetailsImpl;
import com.example.backend.Security.verificationCode.CodeVerification;
import com.example.backend.Security.verificationCode.CodeVerificationServiceImpl;
import com.example.backend.Service.ManagerServiceService;
import com.example.backend.Service.UserService;
import com.example.backend.exception.EmailAlreadyExistsException;
import com.example.backend.exception.MatriculeAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Controller
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/User")
public class UserController {
    @Autowired
    private  PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;
    @Autowired
    private ManagerServiceRepository managerServiceRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CollaborateurRepository collaborateurRepository;
    @Autowired
    private CodeVerificationServiceImpl codeVerificationService;

    @Autowired
    private MailConfig emailService;

    @PostMapping("/registerManagerService")
    public ResponseEntity<?> registerManagerService(
            @RequestParam("nom") String nom,
            @RequestParam("prenom") String prenom,
            @RequestParam("numtel") int numtel,
            @RequestParam("matricule") String matricule,
            @RequestParam("email") String email,
            @RequestParam("gender") Gender gender,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam("department") Departement department,
            @RequestParam("poste") String poste,
            @RequestParam(value = "bio", required = false) String bio,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateEntree,
            @RequestParam("competences") List<Competence> competences
    ) {
        try {
            User manager = new User();
            manager.setNom(nom);
            manager.setPrenom(prenom);
            manager.setNumtel(numtel);
            manager.setMatricule(matricule);
            manager.setRole(Role.ManagerService);
            manager.setEmail(email);
            manager.setPassword(passwordEncoder.encode("SopraHR2024"));
            manager.setDate(LocalDateTime.now());
            manager.setActivated(false);
            manager.setGender(gender);

            User registeredManager = userRepository.save(manager);

            ManagerService request = new ManagerService();
            request.setImage(gender == Gender.Femme ? "avatar/femme.png" : "avatar/homme.png");

            request.setManager(registeredManager);
            request.setDepartment(department);
            request.setPoste(poste);
            request.setBio(bio);

            request.setDateEntree(dateEntree);
            request.setCompetences(competences);
            managerServiceRepository.save(request);

            // Call your service method to register the manager service
            CodeVerification verificationCode = codeVerificationService.createToken(manager);
            String resetLink = "http://localhost:4200/activate-account?token=" + verificationCode.getToken();
            emailService.sendWelcomeEmail(manager.getEmail(),manager.getNom(), resetLink, verificationCode.getActivationCode());

            return new ResponseEntity<>(manager, HttpStatus.CREATED);
        } catch (EmailAlreadyExistsException | MatriculeAlreadyExistsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping("/registerCollaborateur")
    public ResponseEntity<?> registerCollaborateur(
            @RequestParam("nom") String nom,
            @RequestParam("prenom") String prenom,
            @RequestParam("numtel") int numtel,
            @RequestParam("matricule") String matricule,
            @RequestParam("email") String email,
            @RequestParam("gender") Gender gender,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam("poste") String poste,
            @RequestParam("managerService") ManagerService managerService,
            @RequestParam(value = "bio", required = false) String bio,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateEntree,
            @RequestParam("competences") List<Competence> competences
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
            User registeredManager = userRepository.save(manager);
            Collaborateur request = new Collaborateur();
            request.setCollaborateur(registeredManager);
            request.setImage(gender == Gender.Femme ? "avatar/femme.png" : "avatar/homme.png");
            request.setManagerService(managerService); // Associer le collaborateur avec le ManagerService
            request.setDepartment(managerService.getDepartment());
            request.setPoste(poste);
            request.setBio(bio);
            request.setDateEntree(dateEntree);
            request.setCompetences(competences);
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

    @PostMapping("/registerCollab")
    public ResponseEntity<Map<String, String>> registerCollab(@RequestBody User request) {
        try {
            User registeredUser = userService.registerCollaborateur(request);
            CodeVerification verificationCode = codeVerificationService.createToken(registeredUser);
            String resetLink = "http://localhost:4200/activate-account?token=" + verificationCode.getToken();
            emailService.sendVerificationCodeByEmail(registeredUser.getEmail(), resetLink, verificationCode.getActivationCode());

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "User registered successfully");
            return ResponseEntity.ok(responseBody);
        } catch (EmailAlreadyExistsException e) {
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("error", "Cet e-mail est déjà utilisé. Veuillez choisir un autre e-mail.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
        }catch (MatriculeAlreadyExistsException e) {
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("error", "Ce matricule est déjà utilisé. Veuillez choisir un autre matricule.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);

        }
    }
    @Autowired
    JwtUtils jwtUtils;
    @Autowired
    AuthenticationManager authenticationManager;
    @GetMapping("/managerServices")
    public ResponseEntity<List<User>> getManagerServices() {
        List<User> managerServices = userService.getUsersByRole(Role.ManagerService);

        if (managerServices.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }

        return ResponseEntity.ok(managerServices);
    }
    @GetMapping("/collaborateur")
    public ResponseEntity<List<User>> getCollaborateur() {
        List<User> managerServices = userService.getUsersByRole(Role.Collaborateur);

        if (managerServices.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }

        return ResponseEntity.ok(managerServices);
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}/activate")
    public ResponseEntity<String> activateUser(@PathVariable Long userId) {
        User user = userService.getUserById(userId);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
        }
        boolean emailSent = userService.updateActivationStatus(user, !user.isActivated());

        if (emailSent) {
            String activationStatus = user.isActivated() ? "désactivé" : "activé";
            return ResponseEntity.ok("Le statut d'activation de l'utilisateur avec l'ID " + userId + " a été inversé. Un e-mail a été envoyé à " + user.getEmail() + ". Le compte est maintenant " + activationStatus);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'envoi de l'e-mail");
        }
    }

    @GetMapping("/finduserbyid/{id}")
    public User getUserByid(@PathVariable Long id) {
        return userService.getUserById(id);
    }


}