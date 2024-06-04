package com.example.backend.Controller;
import com.example.backend.Configuration.MailConfig;
import com.example.backend.Entity.*;
import com.example.backend.Repository.*;
import com.example.backend.Security.jwt.JwtUtils;
import com.example.backend.Security.services.UserDetailsImpl;
import com.example.backend.Security.verificationCode.CodeVerification;
import com.example.backend.Security.verificationCode.CodeVerificationServiceImpl;
import com.example.backend.Service.ManagerServiceService;
import com.example.backend.Service.UserService;
import com.example.backend.exception.EmailAlreadyExistsException;
import com.example.backend.exception.ManagerServiceNotFoundException;
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
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/User")
public class UserController {
    @Autowired
    private  PasswordEncoder passwordEncoder;
    @Autowired
    private PosteRepository posteRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ManagerServiceRepository managerServiceRepository;
    @Autowired
    private FormateurRepository formateurRepository;
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
            @RequestParam(value = "dateEntree") String dateEntree,
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
            manager.setImage(gender == Gender.Femme ? "avatar/femme.png" : "avatar/homme.png");

            User registeredManager = userRepository.save(manager);

            ManagerService request = new ManagerService();

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
        } catch (EmailAlreadyExistsException emailException) {
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("emailError", "L'adresse e-mail est déjà utilisée. Veuillez en choisir une autre.");
            return ResponseEntity.badRequest().body(responseMap);
        } catch (MatriculeAlreadyExistsException matriculeException) {
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("matriculeError", "Le matricule est déjà utilisé. Veuillez en choisir un autre.");
            return ResponseEntity.badRequest().body(responseMap);
        }



    }


    @GetMapping("/countCollaborateurs")
    public long countCollaborateurs() {
        return userService.countCollaborateurs();
    }
    @GetMapping("/countManagerServices")
    public long countManagerServices() {
        return userService.countManagerServices();
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
            @RequestParam("managerServiceId") Long managerServiceId,
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam("dateEntree") String dateEntree,
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

    @PutMapping("/updateProfileCollaborateur")
    public ResponseEntity<Map<String, String>> updateCollaborateurProfile(
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam(value = "competences", required = false) List<Competence> competences
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
            collaborateurRepository.save(collaborateur);
            response.put("competences", "Competences updated successfully");
        }

        // Return success message
        response.put("success", "Profile updated successfully");
        return ResponseEntity.ok(response);
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
    @GetMapping("/managerRh")
    public ResponseEntity<List<User>> getManagerRh() {
        List<User> managerServices = userService.getUsersByRole(Role.ManagerRh);
        if (managerServices.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }

        return ResponseEntity.ok(managerServices);
    }
    @DeleteMapping("/deleteAccount/{id}")
    @ResponseBody
    public void deleteAccount(@PathVariable("id")Long id){
        userService.deleteUser(id);
    }
    @GetMapping("/collaborateur")
    public ResponseEntity<List<User>> getCollaborateur() {
        List<User> managerServices = userService.getUsersByRole(Role.Collaborateur);

        if (managerServices.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }

        return ResponseEntity.ok(managerServices);
    }
        @GetMapping("/formateur")
        public ResponseEntity<List<User>> getFormateur() {
        List<User> managerServices = userService.getUsersByRole(Role.Formateur);

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
    @GetMapping("/allUsersByRole")
    public List<User> allUsersByRole() {
        List<User> allUsers = userService.getAllUsers();
        return allUsers.stream()
                .filter(user -> user.getRole() == Role.Formateur || user.getRole() == Role.Collaborateur || user.getRole() == Role.ManagerService)
                .collect(Collectors.toList());
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
    @PutMapping("/updatePassword")
    public ResponseEntity<Map<String, String>>  updatePassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        User user = userDetails.getUser();
        Map<String, String> response = new HashMap<>();

        // Vérifier si l'ancien mot de passe fourni correspond au mot de passe actuel
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            response.put("error", "L'ancien mot de passe est incorrect");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); }

        // Mettre à jour le mot de passe
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        response.put("success", "Mot de passe mis à jour avec succès");
        return ResponseEntity.ok(response);    }
    // Add this method to your UserController
    @PutMapping("/updateProfile")
    public ResponseEntity<Map<String, String>> updateProfile(
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "numtel", required = false) Integer numtel,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();

        Map<String, String> response = new HashMap<>();

        // Check if at least one of the parameters is provided
        // Check if at least one of the parameters is provided
        if (email == null && numtel == null && image == null) {
            response.put("error", "Provide at least one parameter (newEmail, newNumtel, or image)");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        // Check if the new email is not already taken
        if (email != null && userRepository.existsByEmailAndIdNot(email, user.getId())) {
            response.put("error", "Email already in use");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Update the email if provided
        if (email != null) {
            user.setEmail(email);
        }

        // Update the numtel if provided
        if (numtel != null) {
            user.setNumtel(numtel);
        }
        // Handle image upload
        if (image != null && !image.isEmpty()) {
            try {
                // Generate a timestamp for the image file name
                String timestamp = String.valueOf(System.currentTimeMillis());

                // Determine the subfolder path based on the user's role
                String subfolderPath;
                switch (user.getRole()) {
                    case ManagerRh:
                        subfolderPath = "ManagerRh";
                        break;
                    case ManagerService:
                        subfolderPath = "ManagerService";
                        break;
                    case Collaborateur:
                        subfolderPath = "Collaborateur";
                        break;
                    default:
                        // Handle other roles if needed
                        response.put("error", "Invalid user role");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }

                // Determine the folder path
                String folderPath = "C:\\Users\\olkhaznaji\\Desktop\\StagePFE\\Frontend\\src\\assets\\images\\" + subfolderPath;

                // Create the folder if it doesn't exist
                File folder = new File(folderPath);
                if (!folder.exists()) {
                    folder.mkdirs();
                }

                // Save the image to the subfolder with the timestamp as part of the file name
                String fileName = timestamp + "_" + image.getOriginalFilename();
                File filePath = new File(folder, fileName);
                image.transferTo(filePath);

                // Update the user's image file name in the database
                user.setImage(subfolderPath + "/" + fileName);
            } catch (IOException e) {
                response.put("error", "Error saving the image");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }

        // Save the changes
        userRepository.save(user);

        response.put("success", "Profile updated successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUserDetails() {
        User currentUser;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            currentUser = userDetails.getUser();
        } else {
            // Utiliser un utilisateur par défaut avec un nom d'utilisateur spécifique
            currentUser = userRepository.findUserByEmail("coach1@gmail.com"); // Remplacez "user1@gmail.com" par le nom d'utilisateur spécifique
        }

        return ResponseEntity.ok(currentUser);
    }
    @GetMapping("/finduserbyid/{id}")
    public User getUserByid(@PathVariable Long id) {
        return userService.getUserById(id);
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserDetails(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user != null) {
            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("id", user.getId());
            userDetails.put("nom", user.getNom());
            userDetails.put("prenom", user.getPrenom());
            userDetails.put("email", user.getEmail());
            userDetails.put("password", user.getPassword());
            userDetails.put("matricule", user.getMatricule());
            userDetails.put("date", user.getDate());
            userDetails.put("numtel", user.getNumtel());
            userDetails.put("role", user.getRole());
            userDetails.put("gender", user.getGender());
            userDetails.put("isActivated", user.isActivated());
            userDetails.put("image", user.getImage());

            // Ajoutez d'autres propriétés selon vos besoins

            if (user.getRole() == Role.ManagerService) {
                ManagerService managerService = user.getManagerService();
                userDetails.put("managerService", managerService);
                userDetails.put("competences", managerService.getCompetences());
                List<User> users = userService.getUsersByManagerService(managerService);

                // Extract names and surnames and add them to the response
                List<Map<String, Object>> userNames = new ArrayList<>();

                for (User u : users) {
                    Map<String, Object> userNameMap = new HashMap<>();
                    userNameMap.put("id", u.getId());
                    userNameMap.put("nom", u.getNom());
                    userNameMap.put("prenom", u.getPrenom());

                    userNames.add(userNameMap);
                }

                userDetails.put("users", userNames);
                // Ajoutez également la liste des collaborateurs associés au ManagerService
                List<Map<String, Object>> collaborateurs = new ArrayList<>();

                for (Collaborateur collaborateur : managerService.getCollaborateurs()) {
                    Map<String, Object> collaborateurMap = new HashMap<>();
                    collaborateurMap.put("id", collaborateur.getCollaborateur().getId());
                    collaborateurMap.put("nom", collaborateur.getCollaborateur().getNom());
                    collaborateurMap.put("prenom", collaborateur.getCollaborateur().getPrenom());
                    collaborateurs.add(collaborateurMap);

                }

                userDetails.put("collaborateurs", collaborateurs);

            } else if (user.getRole() == Role.Collaborateur) {
                Collaborateur collaborateur = user.getCollaborateur();
                userDetails.put("collaborateur", collaborateur);
                userDetails.put("competences", collaborateur.getCompetences());
                userDetails.put("ManagerServices", collaborateur.getManagerService());
                userDetails.put("Nom ManagerServices", collaborateur.getManagerService().getManager().getNom());
                userDetails.put("Prenom ManagerServices", collaborateur.getManagerService().getManager().getPrenom());

            }


            return ResponseEntity.ok(userDetails);
        } else {
            return ResponseEntity.notFound().build();
        }
    }




}