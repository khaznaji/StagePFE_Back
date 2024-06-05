package com.example.backend.services;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.backend.Entity.Collaborateur;
import com.example.backend.Entity.Competence;
import com.example.backend.Entity.Evaluation;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.CompetenceRepository;
import com.example.backend.Service.CollaborateurService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;

@ExtendWith(MockitoExtension.class)
public class CollaborateurServiceImplMock {

    @Mock
    private CollaborateurRepository collaborateurRepository;

    @Mock
    private CompetenceRepository competenceRepository;

    @InjectMocks
    private CollaborateurService collaborateurService;

    private Collaborateur collaborateur;
    private Competence competence;
    private Evaluation evaluation;

    @BeforeEach
    void setUp() {
        collaborateur = new Collaborateur();
        collaborateur.setId(1L);
        collaborateur.setEvaluations(new ArrayList<>());

        competence = new Competence();
        competence.setId(1L);

        evaluation = new Evaluation();
        evaluation.setId(1L);
        evaluation.setCompetence(competence);

        collaborateur.getEvaluations().add(evaluation);
    }

    @Test
    void testGetCollaborateurDetailsById() {
        when(collaborateurRepository.findByIdWithAssociations(1L)).thenReturn(Optional.of(collaborateur));

        Collaborateur result = collaborateurService.getCollaborateurDetailsById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(collaborateurRepository, times(1)).findByIdWithAssociations(1L);
    }

    @Test
    void testGetCollaborateurDetailsById_NotFound() {
        when(collaborateurRepository.findByIdWithAssociations(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                collaborateurService.getCollaborateurDetailsById(1L));

        assertEquals("Collaborator not found with ID: 1", exception.getMessage());
        verify(collaborateurRepository, times(1)).findByIdWithAssociations(1L);
    }

    @Test
    void testGetEvaluationsForCompetences() {
        List<Competence> competences = Collections.singletonList(competence);
        List<Evaluation> evaluations = Collections.singletonList(evaluation);

        when(competenceRepository.findEvaluationsByCompetence(competence)).thenReturn(evaluations);

        Map<Competence, List<Evaluation>> result = collaborateurService.getEvaluationsForCompetences(competences);

        assertNotNull(result);
        assertTrue(result.containsKey(competence));
        assertEquals(evaluations, result.get(competence));
        verify(competenceRepository, times(1)).findEvaluationsByCompetence(competence);
    }

    @Test
    void testGetCollaborateurWithEvaluations() {
        when(collaborateurRepository.findById(1L)).thenReturn(Optional.of(collaborateur));

        Collaborateur result = collaborateurService.getCollaborateurWithEvaluations(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertFalse(result.getEvaluations().isEmpty());
        verify(collaborateurRepository, times(1)).findById(1L);
    }

    @Test
    void testFindById() {
        when(collaborateurRepository.findById(1L)).thenReturn(Optional.of(collaborateur));

        Collaborateur result = collaborateurService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(collaborateurRepository, times(1)).findById(1L);
    }

    @Test
    void testFindById_NotFound() {
        when(collaborateurRepository.findById(1L)).thenReturn(Optional.empty());

        Collaborateur result = collaborateurService.findById(1L);

        assertNull(result);
        verify(collaborateurRepository, times(1)).findById(1L);
    }
}
