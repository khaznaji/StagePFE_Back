package com.example.backend.Service;

import com.example.backend.Entity.Gender;
import com.example.backend.Entity.User;
import com.example.backend.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService implements IUserService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    public void registerUser(User request) {
        User user = new User();
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setNumtel(request.getNumtel());
        user.setMatricule(request.getMatricule());
        user.setRole(request.getRole());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDate(LocalDateTime.now());
        user.setActivated(false);
        user.setGender(request.getGender());

        // Définir l'image en fonction du genre
        if (request.getGender() == Gender.Femme) {
            user.setImage("C:\\Users\\olkhaznaji\\Desktop\\StagePFE\\Frontend\\src\\assets\\avatar\\femme.png");
        } else {
            user.setImage("C:\\Users\\olkhaznaji\\Desktop\\StagePFE\\Frontend\\src\\assets\\avatar\\homme.png");
        }   

        userRepository.save(user);
    }


    @Override

    public boolean isValidCredentials(User credentials) {
        // Recherche de l'utilisateur dans la base de données
        Optional<User> userOptional = userRepository.findByEmail(credentials.getEmail());

        // Vérification de l'existence de l'utilisateur et du mot de passe correspondant
        return userOptional.isPresent() && passwordEncoder.matches(credentials.getPassword(), userOptional.get().getPassword());
    }
}
