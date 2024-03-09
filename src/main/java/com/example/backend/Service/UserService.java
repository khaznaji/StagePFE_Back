package com.example.backend.Service;

import com.example.backend.Configuration.MailConfig;
import com.example.backend.Entity.Gender;
import com.example.backend.Entity.ManagerService;
import com.example.backend.Entity.Role;
import com.example.backend.Entity.User;
import com.example.backend.Repository.ManagerServiceRepository;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Security.verificationCode.CodeVerificationRepository;
import com.example.backend.exception.EmailAlreadyExistsException;
import com.example.backend.exception.InvalidVerificationCodeException;
import com.example.backend.exception.MatriculeAlreadyExistsException;
import com.example.backend.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements IUserService{
    private final UserRepository userRepository;
    @Autowired
    private  ManagerServiceRepository managerServiceRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    @Autowired
    private CodeVerificationRepository codeVerificationRepository;
    @Transactional
    public void deleteUser(Long userId) {
        // Delete CodeVerification records first
        codeVerificationRepository.deleteByUserId(userId);

        // Then delete the user
        userRepository.deleteById(userId);
    }


    public List<User> getUsersByManagerService(ManagerService managerService) {
        return userRepository.findByManagerService(managerService);
    }
    @Override
    public User registerUser(User request) {
        // Vérifie si l'e-mail est déjà utilisé
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet e-mail est déjà utilisé. Veuillez choisir un autre e-mail.");
        }

        // Vérifie si le matricule est déjà utilisé
        if (userRepository.existsByMatricule(request.getMatricule())) {
            throw new MatriculeAlreadyExistsException();
        }

        // Crée un nouvel utilisateur
        User user = new User();
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setNumtel(request.getNumtel());
        user.setMatricule(request.getMatricule());
        user.setRole(Role.ManagerRh);
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDate(LocalDateTime.now());
        user.setActivated(false);
        user.setGender(request.getGender());
        user.setImage(request.getGender() == Gender.Femme ? "avatar/femme.png" : "avatar/homme.png");

        User registeredUser = userRepository.save(user);

        // Retourne l'utilisateur enregistré
        return registeredUser;
    }
    @Override
    public User registerCollaborateur(User request) {
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
        user.setRole(Role.ManagerService);
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode("SopraHR2024"));
        user.setDate(LocalDateTime.now());
        user.setActivated(true);
        user.setGender(request.getGender());


        userRepository.save(user);

        User registeredUser = userRepository.save(user);

        // Retourne l'utilisateur enregistré
        return registeredUser;
    }

    @Override
    public User registerManagerService(User request) {
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
        user.setRole(Role.Collaborateur);
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode("SopraHR2024"));
        user.setDate(LocalDateTime.now());
        user.setActivated(true);
        user.setGender(request.getGender());


        User registeredUser = userRepository.save(user);
        return registeredUser;

        // Envoyer un e-mail au nouvel utilisateur

    }
    public ManagerService registerManagerService(ManagerService request) {
        if (userRepository.existsByEmail(request.getManager().getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        if (userRepository.existsByMatricule(request.getManager().getMatricule())) {
            throw new MatriculeAlreadyExistsException();
        }

        // Créer un nouvel utilisateur pour le manager
        User manager = new User();
        manager.setNom(request.getManager().getNom());
        manager.setPrenom(request.getManager().getPrenom());
        manager.setNumtel(request.getManager().getNumtel());
        manager.setMatricule(request.getManager().getMatricule());
        manager.setRole(Role.ManagerRh);
        manager.setEmail(request.getManager().getEmail());
        manager.setPassword(passwordEncoder.encode("SopraHR2024"));
        manager.setDate(LocalDateTime.now());
        manager.setActivated(true);
        manager.setGender(request.getManager().getGender());



        // Enregistrer le manager dans la base de données
        User registeredManager = userRepository.save(manager);

        // Créer un nouveau ManagerService avec les informations du manager et les informations spécifiques au ManagerService
        ManagerService managerService = new ManagerService();
        managerService.setManager(registeredManager);
        managerService.setDepartment(request.getDepartment());
        managerService.setPoste(request.getPoste());
        managerService.setBio(request.getBio());
        managerService.setDateEntree(request.getDateEntree());
        managerService.setCompetences(request.getCompetences());

        // Enregistrer le ManagerService dans la base de données
        return managerServiceRepository.save(managerService);
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
    @Override
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
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
