package com.example.backend.Controller;
import com.example.backend.Configuration.MailConfig;
import com.example.backend.Entity.User;
import com.example.backend.Security.jwt.JwtUtils;
import com.example.backend.Security.services.UserDetailsImpl;
import com.example.backend.Security.verificationCode.CodeVerification;
import com.example.backend.Security.verificationCode.CodeVerificationServiceImpl;
import com.example.backend.Service.UserService;
import com.example.backend.exception.EmailAlreadyExistsException;
import com.example.backend.exception.MatriculeAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Controller
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/User")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private CodeVerificationServiceImpl codeVerificationService;
    @Autowired
    private MailConfig emailService;

    @PostMapping("/registerManagerService")
    public ResponseEntity<Map<String, String>> registerManagerService(@RequestBody User request) {
        try {
            User registeredUser = userService.registerManagerService(request);
            CodeVerification verificationCode = codeVerificationService.createToken(registeredUser);
            String resetLink = "http://localhost:4200/activate-account?token=" + verificationCode.getToken();
            emailService.sendWelcomeEmail(registeredUser.getEmail(),registeredUser.getNom(), resetLink, verificationCode.getActivationCode());

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