package com.example.backend.Controller;

import com.example.backend.Configuration.MailConfig;
import com.example.backend.Entity.User;
import com.example.backend.Payload.request.LoginRequest;
import com.example.backend.Payload.response.JwtResponse;
import com.example.backend.Payload.response.MessageResponse;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Security.jwt.JwtUtils;
import com.example.backend.Security.password.PasswordResetToken;
import com.example.backend.Security.password.PasswordResetTokenService;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.Console;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
@Controller
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CodeVerificationServiceImpl codeVerificationService;
    @Autowired
    private MailConfig emailService;
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(@RequestBody User request) {
        try {
            User registeredUser = userService.registerUser(request);

            // Créer et envoyer le code de vérification
            CodeVerification verificationCode = codeVerificationService.createToken(registeredUser);
            String resetLink = "http://localhost:4200/activate-account?token=" + verificationCode.getToken();
            emailService.sendVerificationCodeByEmail(registeredUser.getEmail(), resetLink, verificationCode.getActivationCode());
            System.out.println("mail envoyeee");

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "User registered successfully");
            return ResponseEntity.ok(responseBody);
        } catch (EmailAlreadyExistsException e) {
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("error", "Cet e-mail est déjà utilisé. Veuillez choisir un autre e-mail.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
        } catch (MatriculeAlreadyExistsException e) {
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("error", "Ce matricule est déjà utilisé. Veuillez choisir un autre matricule.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
        }
    }

    @Autowired
    JwtUtils jwtUtils;
    @Autowired
    AuthenticationManager authenticationManager;
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Authentification
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            // Obtention des détails de l'utilisateur
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            // Vérification si l'utilisateur existe
            User user = userRepository.findUserByEmail(loginRequest.getEmail());
            if (user == null) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Email not found!"));
            }

            // Vérification de l'état d'activation
            if (!user.isActivated()) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: User Inactive!"));
            }

            // Autres vérifications de l'utilisateur si nécessaires
            // ...

            // Retourner la réponse avec le token JWT
            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    roles));

        } catch (BadCredentialsException e) {
            // Si les informations d'identification ne sont pas valides
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Invalid credentials!"));
        } catch (UsernameNotFoundException e) {
            // Si l'email n'existe pas dans la base de données
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email not found!"));
        }
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
    @GetMapping("/emailExists/{email}")
    public ResponseEntity<Boolean> checkEmailExists(@PathVariable String email) {
        // Appeler la méthode emailExists du service
        boolean emailExists = userService.emailExists(email);
        return ResponseEntity.ok(emailExists);
    }
    @GetMapping("/matricule/{matricule}")
    public ResponseEntity<Boolean> checkMatriculeExists(@PathVariable String matricule) {
        // Appeler la méthode emailExists du service
        boolean matriculeExists = userService.matriculeExists(matricule);
        return ResponseEntity.ok(matriculeExists);
    }
    @Autowired
    private PasswordResetTokenService passwordResetTokenService;
    @PostMapping("/request")
    public ResponseEntity<String> requestPasswordReset(@RequestParam("email") String email) {
        Optional<User> userOptional = userService.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            PasswordResetToken resetToken = passwordResetTokenService.createToken(user);
            String resetLink = "http://localhost:4200/reset-password?token=" + resetToken.getToken();

            // Envoyer un e-mail contenant le lien de réinitialisation
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);

            return ResponseEntity.ok("Un e-mail de réinitialisation a été envoyé à votre adresse e-mail.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not found");
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<Object> resetPassword(@RequestParam("token") String token, @RequestParam("password") String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenService.findByToken(token);
        if (resetToken == null || resetToken.isExpired()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Jeton invalide ou expiré.");
        }
        User user = resetToken.getUser();
        user.setPassword(userService.encodePassword(newPassword));
        userService.save(user);
        passwordResetTokenService.deleteToken(resetToken);
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "Mot de passe réinitialisé avec succès..");
        return ResponseEntity.ok(responseBody);
    }
    @PostMapping("/activate")
    public ResponseEntity<Object> codeActivation(@RequestParam("token") String token, @RequestParam("code") String code) {
        CodeVerification resetToken = codeVerificationService.findByToken(token);

        if (resetToken == null || resetToken.isExpired()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Jeton invalide ou expiré.");
        }

        if (!resetToken.getActivationCode().equals(code)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Code incorrect.");
        }

        User user = resetToken.getUser();
        user.setActivated(true);
        userService.save(user);
        codeVerificationService.deleteToken(resetToken);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "Compte activé avec succès.");
        return ResponseEntity.ok(responseBody);
    }
    @PostMapping("/regenerate")
    public ResponseEntity<Map<String, String>>  regenerateToken(@RequestParam("token") String token) {
        CodeVerification regeneratedToken = codeVerificationService.regenerateToken(token);
        String resetLink = "http://localhost:4200/activate-account?token=" + regeneratedToken.getToken();
        String email = regeneratedToken.getUser().getEmail(); // Adjust this according to your User class
        String verificationCode = regeneratedToken.getActivationCode();
        emailService.sendVerificationCodeByEmail(email, resetLink, verificationCode);

        if (regeneratedToken != null) {
            Map<String, String> response = new HashMap<>();

            response.put("success", "Code envoye avec succes");
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("success", "Token Invalid");
            return ResponseEntity.ok(response);

        }
    }

}
