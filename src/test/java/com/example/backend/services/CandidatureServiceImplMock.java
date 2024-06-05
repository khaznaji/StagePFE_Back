package com.example.backend.services;

import com.example.backend.Entity.*;
import com.example.backend.Repository.CandidatureRepository;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.ManagerServiceRepository;
import com.example.backend.Repository.PosteRepository;
import com.example.backend.Service.CandidatureService;
import com.example.backend.Service.EntretienService;
import com.example.backend.exception.CandidatureNotFoundException;
import com.example.backend.Configuration.MailConfig;
import org.apache.catalina.Manager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CandidatureServiceImplMock {

    @Mock
    private CandidatureRepository candidatureRepository;

    @Mock
    private CollaborateurRepository collaborateurRepository;

    @Mock
    private PosteRepository posteRepository;

    @Mock
    private EntretienService entretienService;

    @Mock
    private MailConfig emailService;

    @InjectMocks
    private CandidatureService candidatureService;

    @BeforeEach
    void setUp() {
        // Set up any common behavior or objects needed for the tests
    }

    @Test
    void testGetCandidature() {
        // Create a sample candidature
        Candidature candidature = new Candidature();
        candidature.setId(1L);

        // Mock the repository behavior
        when(candidatureRepository.findById(1L)).thenReturn(Optional.of(candidature));

        // Call the service method
        Candidature result = candidatureService.getCandidature(1L);

        // Verify that the result matches the mocked candidature
        assertEquals(candidature, result);
    }

    @Test
    void testModifierEtat() {
        // Create sample input data
        Long collaborateurId = 1L;
        Long posteId = 2L;
        EtatPostulation nouvelEtat = EtatPostulation.ACCEPTEE;

        // Create a sample collaborateur
        Collaborateur collaborateur = new Collaborateur();
        collaborateur.setId(collaborateurId);

        // Create a sample poste
        Poste poste = new Poste();
        poste.setId(posteId);

        // Create a sample candidature
        Candidature candidature = new Candidature();
        candidature.setCollaborateur(collaborateur);
        candidature.setPoste(poste); // Set the poste directly

        // Mock the repository behavior
        when(candidatureRepository.findByCollaborateurIdAndPosteId(collaborateurId, posteId)).thenReturn(candidature);
        when(candidatureRepository.save(candidature)).thenReturn(candidature);

        // Call the service method
        Candidature result = candidatureService.modifierEtat(collaborateurId, posteId, nouvelEtat);

        // Verify that the state is modified as expected
        assertEquals(nouvelEtat, result.getEtat());
    }
    @Test
    void testGetCandidaturesByPost() {
        // Given
        Long postId = 1L;
        List<Candidature> expectedCandidatures = new ArrayList<>();
        when(candidatureRepository.findByPosteId(postId)).thenReturn(expectedCandidatures);

        // When
        List<Candidature> actualCandidatures = candidatureService.getCandidaturesByPost(postId);

        // Then
        assertEquals(expectedCandidatures, actualCandidatures);
        verify(candidatureRepository, times(1)).findByPosteId(postId);
    }

    @Test
    void testUpdateCandidaturesToEnAttente() {
        // Given
        Long candidatureId1 = 1L;
        Long candidatureId2 = 2L;
        List<Long> candidatureIds = Arrays.asList(candidatureId1, candidatureId2);

        Candidature candidature1 = new Candidature();
        candidature1.setId(candidatureId1);
        candidature1.setEtat(EtatPostulation.ACCEPTEE);

        Candidature candidature2 = new Candidature();
        candidature2.setId(candidatureId2);
        candidature2.setEtat(EtatPostulation.ACCEPTEE);

        when(candidatureRepository.findById(candidatureId1)).thenReturn(Optional.of(candidature1));
        when(candidatureRepository.findById(candidatureId2)).thenReturn(Optional.of(candidature2));

        // When
        candidatureService.updateCandidaturesToEnAttente(candidatureIds);

        // Then
        verify(candidatureRepository, times(1)).findById(candidatureId1);
        verify(candidatureRepository, times(1)).findById(candidatureId2);
        verify(candidatureRepository, times(1)).save(candidature1);
        verify(candidatureRepository, times(1)).save(candidature2);

        assertEquals(EtatPostulation.EN_ATTENTE_ENTRETIEN_RH, candidature1.getEtat());
        assertEquals(EtatPostulation.EN_ATTENTE_ENTRETIEN_RH, candidature2.getEtat());
    }

}
