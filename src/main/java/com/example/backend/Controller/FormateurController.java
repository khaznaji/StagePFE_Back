package com.example.backend.Controller;

import com.example.backend.Configuration.MailConfig;
import com.example.backend.Entity.*;
import com.example.backend.Repository.*;
import com.example.backend.Security.verificationCode.CodeVerification;
import com.example.backend.Security.verificationCode.CodeVerificationServiceImpl;
import com.example.backend.Service.UserService;
import com.example.backend.exception.EmailAlreadyExistsException;
import com.example.backend.exception.MatriculeAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/Formateur")
public class FormateurController {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserService userService;
    @Autowired
    private FormateurRepository formateurRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CodeVerificationServiceImpl codeVerificationService;
    @Autowired
    private MailConfig emailService;
    @GetMapping("/formateur")
    public ResponseEntity<List<User>> getFormateur() {
        List<User> managerServices = userService.getUsersByRole(Role.Formateur);

        if (managerServices.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }

        return ResponseEntity.ok(managerServices);
    }
    @PostMapping("/registerFormateur")
    public ResponseEntity<?> registerFormateur(
            @RequestParam("nom") String nom,
            @RequestParam("prenom") String prenom,
            @RequestParam("numtel") int numtel,
            @RequestParam("email") String email,
            @RequestParam("gender") Gender gender,
            @RequestParam(value = "image", required = false) MultipartFile image ,
            @RequestParam("specialite") String specialite,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateEntree
    ) {
        try {

            User manager = new User();
            manager.setNom(nom);
            manager.setPrenom(prenom);
            manager.setNumtel(numtel);
            manager.setRole(Role.Formateur);
            manager.setEmail(email);
            manager.setPassword(passwordEncoder.encode("SopraHR2024"));
            manager.setDate(LocalDateTime.now());
            manager.setActivated(false);
            manager.setGender(gender);
            manager.setImage(gender == Gender.Femme ? "avatar/femme.png" : "avatar/homme.png");
            User registeredManager = userRepository.save(manager);

            Formateur request = new Formateur();
            request.setFormateur(registeredManager);
            request.setDateEntree(dateEntree);
            request.setSpecialite(specialite);
            formateurRepository.save(request);

            CodeVerification verificationCode = codeVerificationService.createToken(manager);
            String resetLink = "http://localhost:4200/activate-account?token=" + verificationCode.getToken();
            emailService.sendWelcomeEmail(manager.getEmail(),manager.getNom(), resetLink, verificationCode.getActivationCode());

            return new ResponseEntity<>(manager, HttpStatus.CREATED);
        } catch (EmailAlreadyExistsException | MatriculeAlreadyExistsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/formateur/info")
    public Map<String, Object> getFormateurInfo(@AuthenticationPrincipal UserDetails userDetails) {
        // Récupérez le Collaborateur connecté
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Map<String, Object> userInf = new HashMap<>();
        userInf.put("id", user.getId());

        Map<String, Object> userInfo = new HashMap<>();
        Formateur formateur = user.getFormateur();
        userInfo.put("nom", formateur.getFormateur().getNom());
        userInfo.put("prenom", formateur.getFormateur().getPrenom());
        userInfo.put("dateEntree", formateur.getDateEntree());
        userInfo.put("departement", formateur.getSpecialite());
        userInfo.put("image", formateur.getFormateur().getImage());
        userInfo.put("formationCrees", formateur.getFormation().stream()
                .map(poste -> {
                    Map<String, Object> posteMap = new HashMap<>();
                    posteMap.put("id", poste.getId());
                    posteMap.put("titre", poste.getTitle());
                    posteMap.put("imagePoste", poste.getImage());
                    posteMap.put("chapitre", poste.getChapitre());
                    posteMap.put("duree", poste.getDuree());
                    posteMap.put("departement", poste.getDepartment());

                    return posteMap;
                })
                .collect(Collectors.toList()));
        userInfo.put("nombreformationCrees", formateur.getFormation().size());

        return userInfo;
    }

}
