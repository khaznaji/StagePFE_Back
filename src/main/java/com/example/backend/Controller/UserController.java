package com.example.backend.Controller;

import com.example.backend.Entity.User;
import com.example.backend.Payload.request.LoginRequest;
import com.example.backend.Payload.response.JwtResponse;
import com.example.backend.Payload.response.MessageResponse;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Security.jwt.JwtUtils;
import com.example.backend.Security.services.UserDetailsImpl;
import com.example.backend.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/User")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(@RequestBody User request) {
        userService.registerUser(request);
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "User registered successfully");
        return ResponseEntity.ok(responseBody);
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

            // Vérification de l'état d'activation
            if (userRepository.findUserByIsActived(loginRequest.getEmail()) == 0) {
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


}
