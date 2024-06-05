package com.example.backend.services;

import com.example.backend.Entity.Competence;
import com.example.backend.Entity.Domaine;
import com.example.backend.Repository.CompetenceRepository;
import com.example.backend.Service.CompetenceService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CompetenceServiceImpMock {

    @Mock
    private CompetenceRepository competenceRepository;

    @InjectMocks
    private CompetenceService competenceService;

    @BeforeEach
    void setUp() {
        log.info("Setting up mocks");
        // MockitoAnnotations.openMocks(this); // This is not needed when using @ExtendWith(MockitoExtension.class)
    }

    @Test
    void testAddCompetence() {
        Competence competence = new Competence();
        competence.setNom("Java");

        when(competenceRepository.save(competence)).thenReturn(competence);

        Competence result = competenceService.addCompetence(competence);
        assertNotNull(result);
        assertEquals("Java", result.getNom());
        log.info("testAddCompetence passed");
    }

    @Test
    void testGetCompetenceById() {
        Competence competence = new Competence();
        competence.setId(1L);
        competence.setNom("Java");

        when(competenceRepository.findById(1L)).thenReturn(Optional.of(competence));

        Competence result = competenceService.getCompetenceById(1L);
        assertNotNull(result);
        assertEquals("Java", result.getNom());
        log.info("testGetCompetenceById passed");
    }

    @Test
    void testGetAllCompetences() {
        Competence competence1 = new Competence();
        competence1.setNom("Java");

        Competence competence2 = new Competence();
        competence2.setNom("Python");

        when(competenceRepository.findAll()).thenReturn(Arrays.asList(competence1, competence2));

        List<Competence> result = competenceService.getAllCompetences();
        assertEquals(2, result.size());
        log.info("testGetAllCompetences passed");
    }

    @Test
    void testUpdateCompetence() {
        Competence existingCompetence = new Competence();
        existingCompetence.setId(1L);
        existingCompetence.setNom("Java");

        Competence newCompetence = new Competence();
        newCompetence.setNom("Python");

        when(competenceRepository.findById(1L)).thenReturn(Optional.of(existingCompetence));
        when(competenceRepository.save(any(Competence.class))).thenReturn(existingCompetence);

        Competence result = competenceService.updateCompetence(1L, newCompetence);
        assertNotNull(result);
        assertEquals("Python", result.getNom());
        log.info("testUpdateCompetence passed");
    }

    @Test
    void testDeleteCompetence() {
        doNothing().when(competenceRepository).deleteById(1L);

        competenceService.deleteCompetence(1L);

        verify(competenceRepository, times(1)).deleteById(1L);
        log.info("testDeleteCompetence passed");
    }

    @Test
    void testGetCompetencesByDomain() {
        Domaine domaine = Domaine.HardSkills;

        Competence competence1 = new Competence();
        competence1.setNom("Java");
        competence1.setDomaine(domaine);

        Competence competence2 = new Competence();
        competence2.setNom("Python");
        competence2.setDomaine(domaine);

        when(competenceRepository.findByDomaine(domaine)).thenReturn(Arrays.asList(competence1, competence2));

        List<Competence> result = competenceService.getCompetencesByDomain(domaine);
        assertEquals(2, result.size());
        log.info("testGetCompetencesByDomain passed");
    }
}
