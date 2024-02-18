package com.example.backend.Service;

import com.example.backend.Configuration.MailConfig;
import com.example.backend.Entity.Gender;
import com.example.backend.Entity.User;
import com.example.backend.Repository.UserRepository;
import com.example.backend.exception.EmailAlreadyExistsException;
import com.example.backend.exception.InvalidVerificationCodeException;
import com.example.backend.exception.MatriculeAlreadyExistsException;
import com.example.backend.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public User registerUser(User request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet e-mail est déjà utilisé. Veuillez choisir un autre e-mail.");
        }

        if (userRepository.existsByMatricule(request.getMatricule())) {
            throw new MatriculeAlreadyExistsException();
        }
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

        if (request.getGender() == Gender.Femme) {
            user.setImage("avatar/femme.png");

        } else {
            user.setImage("avatar/homme.png");
        }

        User registeredUser = userRepository.save(user);
        // Return the registered user
        return registeredUser;    }
    @Override
    public void registerUserAdmin(User request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        if (userRepository.existsByMatricule(request.getMatricule())) {
            throw new MatriculeAlreadyExistsException();
        }
        User user = new User();
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setNumtel(request.getNumtel());
        user.setMatricule(request.getMatricule());
        user.setRole(request.getRole());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode("SopraHR2024"));
        user.setDate(LocalDateTime.now());
        user.setActivated(true);
        user.setGender(request.getGender());

        // Définir l'image en fonction du genre
        if (request.getGender() == Gender.Femme) {
            user.setImage("avatar/femme.png");
        } else {
            user.setImage("avatar/homme.png");
        }

        userRepository.save(user);

        // Envoyer un e-mail au nouvel utilisateur
        String subject = "Bienvenue sur 4YOU";
        String body = "Chère/Cher " + user.getNom() +",\n\n" +
                "Nous sommes ravis de vous informer que votre compte 4YOU a été créé avec succès!\n\n" +
                "Email: " + user.getEmail() + "\n" +
                "Mot de passe: SopraHR2024\n\n" +
                "Vous pouvez vous connecter à votre compte en utilisant ce lien: http://localhost:4200/signin\n\n" +
                "Nous vous recommandons de changer votre mot de passe dès que possible pour des raisons de sécurité.\n\n" +
                "Merci de faire partie de la communauté 4YOU.\n\n" +
                "Cordialement,\n" +
                "L'équipe 4YOU";


        emailService.sendActivationEmail(user.getEmail(), subject, body);
    }


    @Override

    public boolean isValidCredentials(User credentials) {
        // Recherche de l'utilisateur dans la base de données
        Optional<User> userOptional = userRepository.findByEmail(credentials.getEmail());

        // Vérification de l'existence de l'utilisateur et du mot de passe correspondant
        return userOptional.isPresent() && passwordEncoder.matches(credentials.getPassword(), userOptional.get().getPassword());
    }
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    @Override

    public void updateStatus(Long id, boolean newValue) {
        // Recherche de l'objet MyClass correspondant à l'identifiant "id"
        User myObject = userRepository.findById(id).orElse(null);

        // Vérification que l'objet a été trouvé
        if (myObject != null) {
            // Modification de la propriété "property2"
            myObject.setActivated(newValue);
            userRepository.save(myObject);
        }

    }

    @Autowired
    private MailConfig emailService;

    public boolean updateActivationStatus(User user, boolean newStatus) {
        // Mettez à jour le champ isActivated dans la base de données
        user.setActivated(newStatus);
        userRepository.save(user);

        // Envoyez un e-mail d'activation ou de désactivation en fonction du statut
        String subject, body;

        if (newStatus) {
            subject = "Activation de votre compte - 4YOU";
            body = "Cher(e) " + user.getNom() + ",\n\n" +
                    "Nous espérons que ce message vous trouve bien. Nous sommes ravis de vous informer que votre compte sur 4YOU a été examiné par notre équipe d'administration et activé avec succès.\n\n" +
                    "Vous pouvez maintenant vous connecter à votre compte en utilisant ce lien:\n\n" +
                    "http://localhost:4200/signin\n\n" +
                    "Nous vous remercions de votre patience pendant le processus d'examen. Si vous avez des questions ou des préoccupations, n'hésitez pas à répondre à cet e-mail.\n\n" +
                    "Merci de faire partie de la communauté 4YOU.\n\n" +
                    "Cordialement,\n" +
                    "L'équipe 4YOU";
        } else {
            subject = "Désactivation de votre compte - 4YOU";
            body = "Cher(e) " + user.getNom() + ",\n\n" +
                    "Nous espérons que ce message vous trouve bien. Nous tenons à vous informer que votre compte sur 4YOU a été désactivé.\n\n" +
                    "Si vous avez des questions ou des préoccupations concernant cette désactivation, n'hésitez pas à répondre à cet e-mail.\n\n" +
                    "Merci de faire partie de la communauté 4YOU.\n\n" +
                    "Cordialement,\n" +
                    "L'équipe 4YOU";
        }

        // Envoyez l'e-mail
        try {
            emailService.sendActivationEmail(user.getEmail(), subject, body);
            return true;  // L'e-mail a été envoyé avec succès
        } catch (Exception e) {
            // Gérez l'erreur, par exemple, en journalisant
            return false;  // Erreur lors de l'envoi de l'e-mail
        }
    }
        public User getUserById(Long userId) {
            return userRepository.findById(userId).orElse(null);
        }
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    } public boolean matriculeExists(String matricule) {
        return userRepository.existsByMatricule(matricule);
    }
    public Optional<User> findByEmail(String email) {
       return userRepository.findByEmail(email);
    }
    public User save(User user) {
        return userRepository.save(user);
    }
    public String encodePassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }

}
