package com.example.backend.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.backend.Entity.*;
import com.example.backend.Repository.*;
import com.example.backend.Service.EntretienService;
import com.example.backend.Configuration.MailConfig;
import com.example.backend.Security.services.UserDetailsImpl;
import org.apache.catalina.Manager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.util.*;

@ExtendWith(MockitoExtension.class)
public class EntretienServiceImplMock {
    @Mock
    private EntretienRepository entretienRepository;

    @Mock
    private CandidatureRepository candidatureRepository;

    @Mock
    private PosteRepository posteRepository;

    @Mock
    private ManagerServiceRepository managerServiceRepository;

    @Mock
    private MailConfig emailService;

    @Mock
    private CollaborateurRepository collaborateurRepository;

    @InjectMocks
    private EntretienService entretienService;;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    private Entretien entretien;
    private Poste poste;
    private Candidature candidature;
    private ManagerService managerService;
    private Collaborateur collaborateur;
    private User user;


    @BeforeEach
    void setUp() {
        entretien = new Entretien();
        entretien.setId(1L);
        entretien.setDateEntretien("2023-06-05");
        entretien.setHeureDebut("10:00");
        entretien.setHeureFin("11:00");
        entretien.setTypeEntretien(TypeEntretien.Technique);
        entretien.setEtatEntretien(EtatEntretien.En_Attente);

        poste = new Poste();
        poste.setId(1L);
        poste.setTitre("Developer");

        candidature = new Candidature();
        candidature.setId(1L);
        candidature.setPoste(poste);

        managerService = new ManagerService();
        managerService.setId(1L);
        managerService.setManager(new User());
        managerService.getManager().setNom("Manager Name");
        managerService.getManager().setEmail("manager@example.com");

        collaborateur = new Collaborateur();
        collaborateur.setId(1L);
        collaborateur.setCollaborateur(new User());
        collaborateur.getCollaborateur().setId(1L);
        collaborateur.getCollaborateur().setNom("Collaborateur Name");
        collaborateur.getCollaborateur().setEmail("collaborateur@example.com");
        MockitoAnnotations.openMocks(this);

    }

    @Test
    public void testAjoutEntretienAnnuel() {
        // Setup
        Long collaborateurId = 1L;
        String dateEntretien = "2024-06-10";
        String heureDebut = "10:00";
        String heureFin = "11:00";
        Long managerServiceId = 2L;
        User collab = new User();
        collab.setEmail("a@gmail.com");
        collab.setNom("a");

        Collaborateur collaborateur = new Collaborateur();
        collaborateur.setId(collaborateurId);
        collaborateur.setCollaborateur(collab); // Set the User object in Collaborateur

        User manager = new User();
        manager.setId(managerServiceId);
        manager.setNom("Manager Name");
        manager.setEmail("manager@example.com");

        ManagerService managerService = new ManagerService();
        managerService.setManager(manager);

        UserDetailsImpl userDetails = new UserDetailsImpl(managerServiceId, "username", "password", new ArrayList<>(), new User());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.setContext(securityContext);

        when(managerServiceRepository.findByManagerManagerId(managerServiceId)).thenReturn(Optional.of(managerService));
        when(collaborateurRepository.findById(collaborateurId)).thenReturn(Optional.of(collaborateur));
        when(entretienRepository.save(any(Entretien.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execution
        entretienService.ajoutEntretienAnnuel(collaborateurId, dateEntretien, heureDebut, heureFin);

        // Verification
        verify(entretienRepository).save(any(Entretien.class));
        verify(emailService, times(2)).sendEmail(anyString(), anyString(), anyString());
    }
    @Test
    void testGetAllEntretiens() {
        List<Entretien> entretiens = Collections.singletonList(entretien);
        when(entretienRepository.findAll()).thenReturn(entretiens);

        List<Entretien> result = entretienService.getAllEntretiens();

        assertEquals(1, result.size());
        verify(entretienRepository, times(1)).findAll();
    }

    @Test
    void testGetEntretienById() {
        when(entretienRepository.findById(1L)).thenReturn(Optional.of(entretien));

        Optional<Entretien> result = entretienService.getEntretienById(1L);

        assertTrue(result.isPresent());
        assertEquals(entretien.getId(), result.get().getId());
        verify(entretienRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateEntretien() {
        when(entretienRepository.save(entretien)).thenReturn(entretien);

        Entretien result = entretienService.createEntretien(entretien);

        assertNotNull(result);
        assertEquals(entretien.getId(), result.getId());
        verify(entretienRepository, times(1)).save(entretien);
    }


    @Test
    public void testCreateEntretienForPosteAndCandidature() {
        Long posteId = 1L;
        Long candidatureId = 2L;
        String dateEntretien = "2024-06-10";
        String heureDebut = "10:00";
        String heureFin = "11:00";

        Poste poste = new Poste();
        poste.setId(posteId);

        Candidature candidature = new Candidature();
        candidature.setId(candidatureId);

        when(posteRepository.findById(posteId)).thenReturn(Optional.of(poste));
        when(candidatureRepository.findById(candidatureId)).thenReturn(Optional.of(candidature));
        when(entretienRepository.save(any(Entretien.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Entretien entretien = entretienService.createEntretienForPosteAndCandidature(posteId, candidatureId, dateEntretien, heureDebut, heureFin);

        assertNotNull(entretien);
        assertEquals(dateEntretien, entretien.getDateEntretien());
        assertEquals(heureDebut, entretien.getHeureDebut());
        assertEquals(heureFin, entretien.getHeureFin());
        assertEquals(poste, entretien.getPoste());
        assertEquals(candidature, entretien.getCandidature());
    }

    @Test
    public void testUpdateEntretien() {
        Long entretienId = 1L;
        String dateEntretien = "2024-06-10";
        String heureDebut = "10:00";
        String heureFin = "11:00";

        Candidature candidature = new Candidature();
        User collaborateurUser = new User();
        collaborateurUser.setEmail("collaborateur@example.com");
        collaborateur.setCollaborateur(collaborateurUser);
        candidature.setCollaborateur(collaborateur);
        ManagerService managerService = new ManagerService();
        User manager = new User();
        manager.setEmail("manager@example.com");
        managerService.setManager(manager);
        Poste poste = new Poste();
        poste.setManagerService(managerService);
        candidature.setPoste(poste);

        Entretien entretien = new Entretien();
        entretien.setId(entretienId);
        entretien.setCandidature(candidature);

        when(entretienRepository.findById(entretienId)).thenReturn(Optional.of(entretien));
        when(entretienRepository.save(any(Entretien.class))).thenAnswer(invocation -> invocation.getArgument(0));

        entretienService.updateEntretien(entretienId, dateEntretien, heureDebut, heureFin);

        verify(entretienRepository).save(any(Entretien.class));
        verify(emailService, times(2)).sendEmail(anyString(), anyString(), anyString());
    }



    @Test
    void testDeleteEntretien() {
        when(entretienRepository.findById(1L)).thenReturn(Optional.of(entretien));

        entretienService.deleteEntretien(1L);

        verify(entretienRepository, times(1)).findById(1L);
        verify(entretienRepository, times(1)).save(any(Entretien.class));
        verify(entretienRepository, times(1)).deleteById(1L);
    }
    @Test
    void testUpdateEntretienAnnuel() {
        // Mock UserDetailsImpl
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        User user = new User();
        user.setId(2L);
        user.setEmail("collaborateur@example.com");
        user.setNom("CollaborateurNom");
        UserDetailsImpl userDetails = new UserDetailsImpl(1L, "username", "password", authorities, user);

        // Mock Authentication
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "password", authorities);
        SecurityContextHolder.setContext(new SecurityContextImpl(authentication));

        // Mock ManagerService and Entretien
        ManagerService managerService = new ManagerService();
        managerService.setId(1L);
        Collaborateur collaborateur = new Collaborateur();
        collaborateur.setId(2L);
        User managerUser = new User();
        managerUser.setId(3L); // Assuming manager user ID is 3
        managerUser.setEmail("manager@example.com");
        managerUser.setNom("ManagerNom");
        managerService.setManager(managerUser); // Set the manager user

        collaborateur.setCollaborateur(user);

        Entretien existingEntretien = new Entretien();
        existingEntretien.setId(1L);
        existingEntretien.setCollaborateurs(collaborateur);

        // Mock repository methods
        when(managerServiceRepository.findByManagerManagerId(1L)).thenReturn(Optional.of(managerService));
        when(entretienRepository.findById(1L)).thenReturn(Optional.of(existingEntretien));

        // Mock EmailService
        MailConfig emailService = mock(MailConfig.class);

        // Create EntretienService and pass the mocked EmailService
        EntretienService entretienService = new EntretienService(managerServiceRepository, entretienRepository, emailService);

        // Calling the method under test
        entretienService.updateEntretienAnnuel(1L, "2024-06-07", "09:00", "10:00");

        // Verifying repository method calls
        verify(managerServiceRepository, times(1)).findByManagerManagerId(1L);
        verify(entretienRepository, times(1)).findById(1L);
        verify(entretienRepository, times(1)).save(any(Entretien.class));

        // Verifying email sending
        verify(emailService, times(1)).sendEmail(eq("collaborateur@example.com"), anyString(), anyString());
        verify(emailService, times(1)).sendEmail(eq("manager@example.com"), anyString(), anyString());
    }







    @Test
    void testNoterEntretien() {
        when(entretienRepository.findById(1L)).thenReturn(Optional.of(entretien));

        entretienService.noterEntretien(1L, 5);

        assertEquals(5, entretien.getNote());
        assertEquals(EtatEntretien.Termine, entretien.getEtatEntretien());
        verify(entretienRepository, times(1)).findById(1L);
        verify(entretienRepository, times(1)).save(entretien);
    }

    @Test
    void testGetTypeEntretien() {
        when(entretienRepository.findById(1L)).thenReturn(Optional.of(entretien));

        TypeEntretien result = entretienService.getTypeEntretien(1L);

        assertEquals(TypeEntretien.Technique, result);
        verify(entretienRepository, times(1)).findById(1L);
    }
}
