package com.example.backend.services;
import com.example.backend.Configuration.MailConfig;
import com.example.backend.Entity.Gender;
import com.example.backend.Entity.Role;
import com.example.backend.Entity.User;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Security.services.UserDetailsImpl;
import com.example.backend.Service.UserService;
import com.example.backend.exception.EmailAlreadyExistsException;
import com.example.backend.exception.MatriculeAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplMock {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MailConfig emailService;

    @InjectMocks
    private UserService userService;

    @Test
    void testRegisterUser() {
        // Données de test
        User userRequest = new User();
        userRequest.setNom("John");
        userRequest.setPrenom("Doe");
        userRequest.setEmail("john.doe@example.com");
        userRequest.setMatricule("123456");
        userRequest.setPassword("password");

        // Simuler le comportement du UserRepository pour ne pas trouver d'utilisateur avec le même e-mail
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByMatricule(userRequest.getMatricule())).thenReturn(false);

        // Simuler le cryptage du mot de passe
        when(passwordEncoder.encode(userRequest.getPassword())).thenReturn("hashedPassword");

        // Simuler l'enregistrement de l'utilisateur
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L); // Simuler l'attribution d'un ID lors de l'enregistrement
            return user;
        });

        // Appeler la méthode à tester
        User registeredUser = userService.registerUser(userRequest);

        // Vérifications
        assertNotNull(registeredUser);
        assertEquals("John", registeredUser.getNom());
        assertEquals("Doe", registeredUser.getPrenom());
        assertEquals("john.doe@example.com", registeredUser.getEmail());
        assertEquals("123456", registeredUser.getMatricule());
        assertEquals("hashedPassword", registeredUser.getPassword());
        assertEquals(Role.ManagerRh, registeredUser.getRole());
        assertFalse(registeredUser.isActivated());
    }




    @Test
    void testRegisterCollaborateur() {
        // Données de test
        User userRequest = new User();
        userRequest.setNom("John");
        userRequest.setPrenom("Doe");
        userRequest.setEmail("john.doe@example.com");
        userRequest.setMatricule("123456");
        userRequest.setGender(Gender.Homme);
        userRequest.setNumtel(123456789);

        // Simuler le comportement du UserRepository pour ne pas trouver d'utilisateur avec le même e-mail ou matricule
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByMatricule(userRequest.getMatricule())).thenReturn(false);

        // Simuler le cryptage du mot de passe
        when(passwordEncoder.encode("SopraHR2024")).thenReturn("hashedPassword");

        // Simuler l'enregistrement de l'utilisateur
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L); // Simuler l'attribution d'un ID lors de l'enregistrement
            return user;
        });

        // Appeler la méthode à tester
        User registeredUser = userService.registerCollaborateur(userRequest);

        // Vérifications
        assertNotNull(registeredUser);
        assertEquals("John", registeredUser.getNom());
        assertEquals("Doe", registeredUser.getPrenom());
        assertEquals("john.doe@example.com", registeredUser.getEmail());
        assertEquals("123456", registeredUser.getMatricule());
        assertEquals("hashedPassword", registeredUser.getPassword());
        assertEquals(Role.ManagerService, registeredUser.getRole());
        assertTrue(registeredUser.isActivated());
        assertEquals(Gender.Homme, registeredUser.getGender()); // Comparaison avec l'énumération Gender directement
        assertEquals(123456789, registeredUser.getNumtel());
    }

    @Test
    void testRegisterCollaborateur_EmailAlreadyExists() {
        // Données de test
        User userRequest = new User();
        userRequest.setEmail("john.doe@example.com");

        // Simuler le comportement du UserRepository pour trouver un utilisateur avec le même e-mail
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(true);

        // Vérifier que l'exception EmailAlreadyExistsException est levée
        assertThrows(EmailAlreadyExistsException.class, () -> userService.registerCollaborateur(userRequest));

        // S'assurer que save() n'est jamais appelé
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterCollaborateur_MatriculeAlreadyExists() {
        // Données de test
        User userRequest = new User();
        userRequest.setMatricule("123456");

        // Simuler le comportement du UserRepository pour trouver un utilisateur avec le même matricule
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByMatricule(userRequest.getMatricule())).thenReturn(true);

        // Vérifier que l'exception MatriculeAlreadyExistsException est levée
        assertThrows(MatriculeAlreadyExistsException.class, () -> userService.registerCollaborateur(userRequest));

        // S'assurer que save() n'est jamais appelé
        verify(userRepository, never()).save(any(User.class));
    }
    @Test
    void testRegisterManagerService() {
        // Données de test
        User userRequest = new User();
        userRequest.setNom("Jane");
        userRequest.setPrenom("Doe");
        userRequest.setEmail("jane.doe@example.com");
        userRequest.setMatricule("654321");
        userRequest.setGender(Gender.Femme);
        userRequest.setNumtel(987654321);

        // Simuler le comportement du UserRepository pour ne pas trouver d'utilisateur avec le même e-mail ou matricule
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByMatricule(userRequest.getMatricule())).thenReturn(false);

        // Simuler le cryptage du mot de passe
        when(passwordEncoder.encode("SopraHR2024")).thenReturn("hashedPassword");

        // Simuler l'enregistrement de l'utilisateur
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L); // Simuler l'attribution d'un ID lors de l'enregistrement
            return user;
        });

        // Appeler la méthode à tester
        User registeredUser = userService.registerManagerService(userRequest);

        // Vérifications
        assertNotNull(registeredUser);
        assertEquals("Jane", registeredUser.getNom());
        assertEquals("Doe", registeredUser.getPrenom());
        assertEquals("jane.doe@example.com", registeredUser.getEmail());
        assertEquals("654321", registeredUser.getMatricule());
        assertEquals("hashedPassword", registeredUser.getPassword());
        assertEquals(Role.Collaborateur, registeredUser.getRole());
        assertTrue(registeredUser.isActivated());
        assertEquals(Gender.Femme, registeredUser.getGender());
        assertEquals(987654321, registeredUser.getNumtel());
    }

    @Test
    void testRegisterManagerService_EmailAlreadyExists() {
        // Données de test
        User userRequest = new User();
        userRequest.setEmail("jane.doe@example.com");

        // Simuler le comportement du UserRepository pour trouver un utilisateur avec le même e-mail
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(true);

        // Vérifier que l'exception EmailAlreadyExistsException est levée
        assertThrows(EmailAlreadyExistsException.class, () -> userService.registerManagerService(userRequest));

        // S'assurer que save() n'est jamais appelé
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterManagerService_MatriculeAlreadyExists() {
        // Données de test
        User userRequest = new User();
        userRequest.setMatricule("654321");

        // Simuler le comportement du UserRepository pour trouver un utilisateur avec le même matricule
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByMatricule(userRequest.getMatricule())).thenReturn(true);

        // Vérifier que l'exception MatriculeAlreadyExistsException est levée
        assertThrows(MatriculeAlreadyExistsException.class, () -> userService.registerManagerService(userRequest));

        // S'assurer que save() n'est jamais appelé
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testIsValidCredentials_ValidCredentials() {
        // Données de test
        User credentials = new User();
        credentials.setEmail("test@example.com");
        credentials.setPassword("password");

        // Simuler la recherche de l'utilisateur dans la base de données
        User user = new User();
        user.setPassword("hashedPassword"); // Mot de passe encodé
        when(userRepository.findByEmail(credentials.getEmail())).thenReturn(Optional.of(user));

        // Simuler la comparaison des mots de passe encodés
        when(passwordEncoder.matches(credentials.getPassword(), user.getPassword())).thenReturn(true);

        // Appeler la méthode à tester
        boolean isValid = userService.isValidCredentials(credentials);

        // Vérifier que les bons appels de méthodes ont été effectués
        verify(userRepository, times(1)).findByEmail(credentials.getEmail());
        verify(passwordEncoder, times(1)).matches(credentials.getPassword(), user.getPassword());

        // Vérifier que les identifiants sont valides
        assertTrue(isValid);
    }

    @Test
    void testIsValidCredentials_InvalidCredentials() {
        // Données de test
        User credentials = new User();
        credentials.setEmail("test@example.com");
        credentials.setPassword("password");

        // Simuler la recherche de l'utilisateur dans la base de données
        when(userRepository.findByEmail(credentials.getEmail())).thenReturn(Optional.empty());

        // Appeler la méthode à tester
        boolean isValid = userService.isValidCredentials(credentials);

        // Vérifier que les bons appels de méthodes ont été effectués
        verify(userRepository, times(1)).findByEmail(credentials.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());

        // Vérifier que les identifiants ne sont pas valides
        assertFalse(isValid);
    }

    @Test
    void testGetAllUsers() {
        // Données de test
        User user1 = new User();
        User user2 = new User();
        List<User> userList = Arrays.asList(user1, user2);

        // Simuler la récupération de tous les utilisateurs depuis le repository
        when(userRepository.findAll()).thenReturn(userList);

        // Appeler la méthode à tester
        List<User> result = userService.getAllUsers();

        // Vérifier que la méthode du repository a été appelée une fois
        verify(userRepository, times(1)).findAll();

        // Vérifier que la liste retournée correspond à celle du repository
        assertEquals(userList, result);
    }

    @Test
    void testUpdateStatus_UserFound() {
        // Données de test
        Long id = 1L;
        boolean newValue = true;

        // Simuler la recherche de l'utilisateur dans la base de données
        User user = new User();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        // Appeler la méthode à tester
        userService.updateStatus(id, newValue);

        // Vérifier que les bons appels de méthodes ont été effectués
        verify(userRepository, times(1)).findById(id);
        verify(userRepository, times(1)).save(user);

        // Vérifier que la propriété "activated" a été mise à jour
        assertTrue(user.isActivated());
    }

    @Test
    void testUpdateStatus_UserNotFound() {
        // Données de test
        Long id = 1L;
        boolean newValue = true;

        // Simuler que l'utilisateur n'est pas trouvé dans la base de données
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Appeler la méthode à tester
        userService.updateStatus(id, newValue);

        // Vérifier que les bons appels de méthodes ont été effectués
        verify(userRepository, times(1)).findById(id);
        verify(userRepository, never()).save(any());
    }

    @Test
    void testGetUsersByRole() {
        // Données de test
        Role role = Role.ManagerService;
        List<User> expectedUsers = Collections.singletonList(new User());

        // Simuler la recherche d'utilisateurs par rôle dans la base de données
        when(userRepository.findByRole(role)).thenReturn(expectedUsers);

        // Appeler la méthode à tester
        List<User> result = userService.getUsersByRole(role);

        // Vérifier que la méthode du repository a été appelée une fois
        verify(userRepository, times(1)).findByRole(role);

        // Vérifier que la liste retournée correspond à celle du repository
        assertEquals(expectedUsers, result);
    }





    @Test
    void testGetUserById_UserFound() {
        // Données de test
        Long userId = 1L;
        User expectedUser = new User();

        // Simuler la recherche de l'utilisateur dans la base de données
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        // Appel de la méthode à tester
        User result = userService.getUserById(userId);

        // Vérification que l'utilisateur retourné est celui attendu
        assertEquals(expectedUser, result);
    }

    @Test
    void testGetUserById_UserNotFound() {
        // Données de test
        Long userId = 1L;

        // Simuler un utilisateur non trouvé dans la base de données
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Appel de la méthode à tester
        User result = userService.getUserById(userId);

        // Vérification que l'utilisateur retourné est null
        assertNull(result);
    }
    @Test
    void testEmailExists_EmailExists() {
        // Données de test
        String existingEmail = "test@example.com";

        // Simuler le comportement du UserRepository
        when(userRepository.existsByEmail(existingEmail)).thenReturn(true);

        // Appel de la méthode à tester
        boolean result = userService.emailExists(existingEmail);

        // Vérification
        assertTrue(result);
    }

    @Test
    void testEmailExists_EmailDoesNotExist() {
        // Données de test
        String nonExistingEmail = "nonexistent@example.com";

        // Simuler le comportement du UserRepository
        when(userRepository.existsByEmail(nonExistingEmail)).thenReturn(false);

        // Appel de la méthode à tester
        boolean result = userService.emailExists(nonExistingEmail);

        // Vérification
        assertFalse(result);
    }

    @Test
    void testMatriculeExists_MatriculeExists() {
        // Données de test
        String existingMatricule = "12345";

        // Simuler le comportement du UserRepository
        when(userRepository.existsByMatricule(existingMatricule)).thenReturn(true);

        // Appel de la méthode à tester
        boolean result = userService.matriculeExists(existingMatricule);

        // Vérification
        assertTrue(result);
    }

    @Test
    void testMatriculeExists_MatriculeDoesNotExist() {
        // Données de test
        String nonExistingMatricule = "67890";

        // Simuler le comportement du UserRepository
        when(userRepository.existsByMatricule(nonExistingMatricule)).thenReturn(false);

        // Appel de la méthode à tester
        boolean result = userService.matriculeExists(nonExistingMatricule);

        // Vérification
        assertFalse(result);
    }
    @Test
    void testFindByEmail_EmailExists() {
        // Données de test
        String existingEmail = "test@example.com";
        User user = new User();
        user.setEmail(existingEmail);

        // Simuler le comportement du UserRepository
        when(userRepository.findByEmail(existingEmail)).thenReturn(Optional.of(user));

        // Appel de la méthode à tester
        Optional<User> result = userService.findByEmail(existingEmail);

        // Vérification
        assertTrue(result.isPresent());
        assertEquals(existingEmail, result.get().getEmail());
    }

    @Test
    void testFindByEmail_EmailDoesNotExist() {
        // Données de test
        String nonExistingEmail = "nonexistent@example.com";

        // Simuler le comportement du UserRepository
        when(userRepository.findByEmail(nonExistingEmail)).thenReturn(Optional.empty());

        // Appel de la méthode à tester
        Optional<User> result = userService.findByEmail(nonExistingEmail);

        // Vérification
        assertFalse(result.isPresent());
    }

    @Test
    void testSave() {
        // Données de test
        User userToSave = new User();
        userToSave.setNom("John");
        userToSave.setPrenom("Doe");
        // Autres propriétés...

        // Simuler le comportement du UserRepository
        when(userRepository.save(userToSave)).thenReturn(userToSave);

        // Appel de la méthode à tester
        User savedUser = userService.save(userToSave);

        // Vérification
        assertNotNull(savedUser);
        assertEquals("John", savedUser.getNom());
        assertEquals("Doe", savedUser.getPrenom());
        // Vérification des autres propriétés...
    }
    @Test
    void testCountCollaborateurs() {
        // Données de test
        long expectedCount = 5L;

        // Simuler le comportement du UserRepository
        when(userRepository.countByRole(Role.Collaborateur)).thenReturn(expectedCount);

        // Appel de la méthode à tester
        long actualCount = userService.countCollaborateurs();

        // Vérification
        assertEquals(expectedCount, actualCount);
    }

    @Test
    void testCountManagerServices() {
        // Données de test
        long expectedCount = 3L;

        // Simuler le comportement du UserRepository
        when(userRepository.countByRole(Role.ManagerService)).thenReturn(expectedCount);

        // Appel de la méthode à tester
        long actualCount = userService.countManagerServices();

        // Vérification
        assertEquals(expectedCount, actualCount);
    }
}
