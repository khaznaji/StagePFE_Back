package com.example.backend.Controller;

import com.example.backend.Configuration.MailConfig;
import com.example.backend.Entity.*;
import com.example.backend.Repository.FormationRepository;
import com.example.backend.Repository.ManagerServiceRepository;
import com.example.backend.Repository.PosteRepository;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Security.services.UserDetailsImpl;
import com.example.backend.Security.verificationCode.CodeVerification;
import com.example.backend.Security.verificationCode.CodeVerificationServiceImpl;
import com.example.backend.Service.ManagerServiceService;
import com.example.backend.exception.EmailAlreadyExistsException;
import com.example.backend.exception.MatriculeAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/ManagerService")
public class ManagerServiceController {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ManagerServiceRepository managerServiceRepository;
    @Autowired
    private ManagerServiceService managerServiceService;
    @Autowired
    private UserRepository userRepository;
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
            @RequestParam("department") Departement department,
            @RequestParam("poste") String poste,
            @RequestParam("dateEntree") String dateEntree
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
            request.setDateEntree(dateEntree);
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

    @Autowired
    private  PosteRepository posteRepository ;
    @Autowired
    private FormationRepository formationRepository ;
    @GetMapping("/members")
    public List<List<Map<String, Object>>> getMembersOfAuthenticatedManagerService() {
        return managerServiceService.getMembers();
    }
    @GetMapping("/collaborateurs/count")
    public long getNombreCollaborateurs() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long managerServiceId = userDetails.getId();
        ManagerService managerService = managerServiceRepository.findByManagerManagerId(managerServiceId)
                .orElseThrow(() -> new EntityNotFoundException("ManagerService non trouvé avec l'ID : " + managerServiceId));

        return managerService.getCollaborateurs().size();
    }


    @GetMapping("/postesPublies/count")
    public long getNombrePostesPublies() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long managerServiceId = userDetails.getId();
        ManagerService managerService = managerServiceRepository.findByManagerManagerId(managerServiceId)
                .orElseThrow(() -> new EntityNotFoundException("ManagerService non trouvé avec l'ID : " + managerServiceId));

        return managerService.getNombrePostesPublies();
    }

    @GetMapping("/demandesFormation/count")
    public long getNombreDemandesFormation() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long managerServiceId = userDetails.getId();
        ManagerService managerService = managerServiceRepository.findByManagerManagerId(managerServiceId)
                .orElseThrow(() -> new EntityNotFoundException("ManagerService non trouvé avec l'ID : " + managerServiceId));

        return managerService.getNombreDemandesFormation();
    }

    @GetMapping("/postesApprouves/count")
    public long getNombrePostesApprouves() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long managerServiceId = userDetails.getId();
        ManagerService managerService = managerServiceRepository.findByManagerManagerId(managerServiceId)
                .orElseThrow(() -> new EntityNotFoundException("ManagerService non trouvé avec l'ID : " + managerServiceId));

        return managerService.getNombrePostesApprouves();
    }
    @GetMapping("/top-three-competences")
    public ResponseEntity<Map<String, List<String>>> getTopThreeCompetencesByCategory() {
        try {
            Map<String, List<String>> topThreeCompetencesByCategory = managerServiceService.getTopThreeCompetencesByCategory();
            return ResponseEntity.ok(topThreeCompetencesByCategory);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/postesWithCandidatureCount")
    public ResponseEntity<List<Map<String, Object>>> getPostesWithCandidatureCount() {
        try {
            List<Map<String, Object>> postesWithCandidatureCount = managerServiceService.getPostesWithCandidatureCount();
            return ResponseEntity.ok(postesWithCandidatureCount);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



}
