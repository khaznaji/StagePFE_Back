package com.example.backend.services;


import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.backend.Entity.*;
import com.example.backend.Repository.ManagerServiceRepository;
import com.example.backend.Security.services.UserDetailsImpl;
import com.example.backend.Service.ManagerServiceService;
import com.example.backend.exception.MatriculeAlreadyExistsException;
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

import java.util.*;


@ExtendWith(MockitoExtension.class)
class ManagerServiceImplMock {
    @Mock
    ManagerServiceRepository managerServiceRepository;

    @InjectMocks
    ManagerServiceService managerServiceService;

    @Test
    void getAllManagerServices_Success() {
        // Mocking the repository response
        List<ManagerService> managerServices = new ArrayList<>();
        // Add some manager services to the list
        when(managerServiceRepository.findAll()).thenReturn(managerServices);

        // Call the method to be tested
        List<ManagerService> result = managerServiceService.getAllManagerServices();

        // Verify that the repository method is called once
        verify(managerServiceRepository, times(1)).findAll();
        // Assert that the result matches the mocked list of manager services
        assertEquals(managerServices, result);
    }

    @Test
    void getManagerServiceById_Exists() {
        // Mocking the behavior of the repository to return a ManagerService
        Long managerServiceId = 1L;
        ManagerService managerService = new ManagerService();
        managerService.setId(managerServiceId);
        when(managerServiceRepository.findById(managerServiceId)).thenReturn(Optional.of(managerService));

        // Calling the method to be tested
        Optional<ManagerService> result = managerServiceService.getManagerServiceById(managerServiceId);

        // Verifying that the repository method is called once with the correct ID
        verify(managerServiceRepository, times(1)).findById(managerServiceId);
        // Asserting that the result contains the expected ManagerService
        assertEquals(Optional.of(managerService), result);
    }

    @Test
    void getManagerServiceById_NotExists() {
        // Mocking the behavior of the repository to return an empty Optional
        Long managerServiceId = 1L;
        when(managerServiceRepository.findById(managerServiceId)).thenReturn(Optional.empty());

        // Calling the method to be tested
        Optional<ManagerService> result = managerServiceService.getManagerServiceById(managerServiceId);

        // Verifying that the repository method is called once with the correct ID
        verify(managerServiceRepository, times(1)).findById(managerServiceId);
        // Asserting that the result is empty
        assertEquals(Optional.empty(), result);
    }

    @Test
    void createManagerService_Success() {
        // Creating a ManagerService object
        ManagerService managerService = new ManagerService();

        // Mocking the behavior of the repository to save the ManagerService
        when(managerServiceRepository.save(managerService)).thenReturn(managerService);

        // Calling the method to be tested
        ManagerService result = managerServiceService.createManagerService(managerService);

        // Verifying that the repository method is called once with the correct ManagerService
        verify(managerServiceRepository, times(1)).save(managerService);
        // Asserting that the result matches the saved ManagerService
        assertEquals(managerService, result);
    }
    @Test
    void updateManagerService_Exists() {
        // Mocking the behavior of the repository to return true for existsById
        Long managerServiceId = 1L;
        ManagerService updatedManagerService = new ManagerService();
        updatedManagerService.setId(managerServiceId);
        when(managerServiceRepository.existsById(managerServiceId)).thenReturn(true);
        when(managerServiceRepository.save(updatedManagerService)).thenReturn(updatedManagerService);

        // Calling the method to be tested
        ManagerService result = managerServiceService.updateManagerService(managerServiceId, updatedManagerService);

        // Verifying that the repository methods are called once with the correct ID and updatedManagerService
        verify(managerServiceRepository, times(1)).existsById(managerServiceId);
        verify(managerServiceRepository, times(1)).save(updatedManagerService);
        // Asserting that the result matches the updatedManagerService
        assertEquals(updatedManagerService, result);
    }

    @Test
    void updateManagerService_NotExists() {
        // Mocking the behavior of the repository to return false for existsById
        Long managerServiceId = 1L;
        ManagerService updatedManagerService = new ManagerService();
        when(managerServiceRepository.existsById(managerServiceId)).thenReturn(false);

        // Calling the method to be tested
        ManagerService result = managerServiceService.updateManagerService(managerServiceId, updatedManagerService);

        // Verifying that the repository method is called once with the correct ID
        verify(managerServiceRepository, times(1)).existsById(managerServiceId);
        // Asserting that the result is null
        assertNull(result);
    }

    @Test
    void deleteManagerService() {
        // Mocking the behavior of the repository
        Long managerServiceId = 1L;
        doNothing().when(managerServiceRepository).deleteById(managerServiceId);

        // Calling the method to be tested
        managerServiceService.deleteManagerService(managerServiceId);

        // Verifying that the repository method is called once with the correct ID
        verify(managerServiceRepository, times(1)).deleteById(managerServiceId);
    }
    @Test
    void getMembers() {
        // Mocking UserDetailsImpl
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        User user = new User();
        UserDetailsImpl userDetails = new UserDetailsImpl(1L, "username", "password", authorities, user);

        // Mocking Authentication
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "password", authorities);

        // Setting up SecurityContext
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Mocking ManagerService and Collaborateur
        ManagerService managerService = new ManagerService();
        Collaborateur collaborateur1 = new Collaborateur();
        collaborateur1.setId(1L);
        User user1 = new User();
        user1.setId(1L);
        user1.setNom("John");
        user1.setPrenom("Doe");
        user1.setImage("image1.jpg");
        collaborateur1.setCollaborateur(user1);

        Collaborateur collaborateur2 = new Collaborateur();
        collaborateur2.setId(2L);
        User user2 = new User();
        user2.setId(2L);
        user2.setNom("Alice");
        user2.setPrenom("Smith");
        user2.setImage("image2.jpg");
        collaborateur2.setCollaborateur(user2);

        List<Collaborateur> collaborateurs = new ArrayList<>();
        collaborateurs.add(collaborateur1);
        collaborateurs.add(collaborateur2);

        managerService.setCollaborateurs(collaborateurs);

        // Mocking the repository method
        when(managerServiceRepository.findByManagerManagerId(1L)).thenReturn(Optional.of(managerService));

        // Calling the method to be tested
        List<List<Map<String, Object>>> result = managerServiceService.getMembers();

        // Asserting the result
        assertEquals(2, result.size()); // Two collaborateurs, so two lists
        assertEquals(5, result.get(0).get(0).size()); // Each collaborateur has five attributes
        assertEquals(5, result.get(1).get(0).size());


        // Verifying repository method call
        verify(managerServiceRepository, times(1)).findByManagerManagerId(1L);
    }
    @Test
    void getTopThreeCompetencesByCategory() {
        // Mocking UserDetailsImpl
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        User user = new User();
        UserDetailsImpl userDetails = new UserDetailsImpl(1L, "username", "password", authorities, user);

        // Mocking Authentication
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "password", authorities);

        // Setting up SecurityContext
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Mocking ManagerService and Collaborateur
        ManagerService managerService = new ManagerService();
        Collaborateur collaborateur = new Collaborateur();
        Evaluation evaluation1 = new Evaluation();
        evaluation1.setCompetence(new Competence("Hard Skill 1", Domaine.HardSkills));
        evaluation1.setEvaluation(5);
        Evaluation evaluation2 = new Evaluation();
        evaluation2.setCompetence(new Competence("Hard Skill 2", Domaine.HardSkills));
        evaluation2.setEvaluation(4);
        Evaluation evaluation3 = new Evaluation();
        evaluation3.setCompetence(new Competence("Hard Skill 3", Domaine.HardSkills));
        evaluation3.setEvaluation(3);
        collaborateur.setEvaluations(Arrays.asList(evaluation1, evaluation2, evaluation3));
        managerService.setCollaborateurs(Collections.singletonList(collaborateur));

        // Mocking the repository method
        when(managerServiceRepository.findByManagerManagerId(1L)).thenReturn(Optional.of(managerService));

        // Calling the method to be tested
        Map<String, List<String>> result = managerServiceService.getTopThreeCompetencesByCategory();

        // Asserting the result
        assertEquals(3, result.get("Hard Skills").size()); // Three hard skills
        assertEquals("Hard Skill 1", result.get("Hard Skills").get(0)); // Highest rated hard skill
        assertEquals("Hard Skill 2", result.get("Hard Skills").get(1)); // Second highest rated hard skill
        assertEquals("Hard Skill 3", result.get("Hard Skills").get(2)); // Third highest rated hard skill

        // Verifying repository method call
        verify(managerServiceRepository, times(1)).findByManagerManagerId(1L);
    }
    @Test
    void getPostesWithCandidatureCount() {
        // Mocking UserDetailsImpl
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        User user = new User();
        UserDetailsImpl userDetails = new UserDetailsImpl(1L, "username", "password", authorities, user);

        // Mocking Authentication
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "password", authorities);

        // Setting up SecurityContext
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Mocking ManagerService and Poste
        ManagerService managerService = new ManagerService();
        Poste poste1 = new Poste();
        poste1.setTitre("Poste 1");
        poste1.setPoste(EtatPoste.Publie);
        poste1.setCandidatures(Arrays.asList(new Candidature(), new Candidature()));
        Poste poste2 = new Poste();
        poste2.setTitre("Poste 2");
        poste2.setPoste(EtatPoste.Publie);
        poste2.setCandidatures(Arrays.asList(new Candidature()));
        managerService.setPostes(Arrays.asList(poste1, poste2));

        // Mocking the repository method
        when(managerServiceRepository.findByManagerManagerId(1L)).thenReturn(Optional.of(managerService));

        // Calling the method to be tested
        List<Map<String, Object>> result = managerServiceService.getPostesWithCandidatureCount();

        // Asserting the result
        assertEquals(2, result.size()); // Two postes
        assertEquals("Poste 1", result.get(0).get("titre")); // Titre of first poste
        assertEquals(2, result.get(0).get("nbr")); // Number of candidatures for first poste
        assertEquals("Poste 2", result.get(1).get("titre")); // Titre of second poste
        assertEquals(1, result.get(1).get("nbr")); // Number of candidatures for second poste

        // Verifying repository method call
        verify(managerServiceRepository, times(1)).findByManagerManagerId(1L);
    }



}
