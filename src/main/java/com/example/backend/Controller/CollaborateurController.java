package com.example.backend.Controller;

import com.example.backend.Entity.Collaborateur;
import com.example.backend.Entity.User;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Security.services.UserDetailsImpl;
import com.example.backend.Service.CollaborateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/Collaborateur")
public class CollaborateurController {
    @Autowired
    private CollaborateurService collaborateurService;
    @Autowired
    private UserRepository userRepository;




   /* @GetMapping("/me")
    public ResponseEntity<User> getCurrentUserDetails() {
        User currentUser;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            currentUser = userDetails.getUser();
        } else {
            // Utiliser un utilisateur par défaut avec un nom d'utilisateur spécifique
            currentUser = userRepository.findByPrenom("kk"); // Remplacez "user1@gmail.com" par le nom d'utilisateur spécifique
        }

        return ResponseEntity.ok(currentUser);
    }*/
}
