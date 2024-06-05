package com.example.backend.services;
import com.example.backend.Entity.*;
import com.example.backend.Repository.BilanAnnuelRepository;
import com.example.backend.Service.BilanAnnuelService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

    @ExtendWith(MockitoExtension.class)
    @Slf4j
public class BilanAnnuelServiceImpMock {
    @Mock
    private BilanAnnuelRepository bilanAnnuelRepository;

    @InjectMocks
    private BilanAnnuelService bilanAnnuelService;



    private Collaborateur collaborateur1;
    private Collaborateur collaborateur2;
    private ManagerService managerService;
    private BilanAnnuel bilanAnnuel;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setNom("John Doe");

        user2 = new User();
        user2.setId(2L);
        user2.setNom("Jane Doe");

        collaborateur1 = new Collaborateur();
        collaborateur1.setId(1L);
        collaborateur1.setCollaborateur(user1);

        collaborateur2 = new Collaborateur();
        collaborateur2.setId(2L);
        collaborateur2.setCollaborateur(user2);

        managerService = new ManagerService();
        managerService.setCollaborateurs(Arrays.asList(collaborateur1, collaborateur2));

        bilanAnnuel = new BilanAnnuel();
        bilanAnnuel.setId(1L);
        bilanAnnuel.setManagerService(managerService);
        bilanAnnuel.setCollaborateur(collaborateur1);
        bilanAnnuel.setDateBilan(LocalDate.now());
        bilanAnnuel.setEtatBilan(Etat.En_Attente);
    }

    @Test
    void testEnvoyerBilanAnnuel() {
        bilanAnnuelService.envoyerBilanAnnuel(managerService);

        ArgumentCaptor<BilanAnnuel> bilanCaptor = ArgumentCaptor.forClass(BilanAnnuel.class);
        verify(bilanAnnuelRepository, times(2)).save(bilanCaptor.capture());

        List<BilanAnnuel> savedBilans = bilanCaptor.getAllValues();
        assertEquals(2, savedBilans.size());
        assertEquals(Etat.En_Attente, savedBilans.get(0).getEtatBilan());
        assertEquals(Etat.En_Attente, savedBilans.get(1).getEtatBilan());
    }
    @Test
    void testMettreAJourBilanAnnuel() {
        // Créer un bilan annuel existant avec l'ID 1L
        BilanAnnuel bilanAnnuelExistant = new BilanAnnuel();
        bilanAnnuelExistant.setId(1L);
        bilanAnnuelExistant.setObjectifAtteints("Old Objectives");
        bilanAnnuelExistant.setObjectifsFuturs("Old Future Objectives");
        bilanAnnuelExistant.setProjetsAccomplis("Old Projects");
        bilanAnnuelExistant.setChallenges("Old Challenges");
        bilanAnnuelExistant.setEtatBilan(Etat.En_Attente);

        // Configurer le mock pour retourner le bilanAnnuelExistant lorsque findById est appelé avec l'ID 1L
        when(bilanAnnuelRepository.findById(1L)).thenReturn(Optional.of(bilanAnnuelExistant));

        // Créer un bilan annuel mis à jour
        BilanAnnuel bilanAnnuelMiseAJour = new BilanAnnuel();
        bilanAnnuelMiseAJour.setId(1L); // Définir l'ID du bilan mis à jour
        bilanAnnuelMiseAJour.setObjectifAtteints("New Objectives");
        bilanAnnuelMiseAJour.setObjectifsFuturs("Future Objectives");
        bilanAnnuelMiseAJour.setProjetsAccomplis("Projects");
        bilanAnnuelMiseAJour.setChallenges("Challenges");
        bilanAnnuelMiseAJour.setEtatBilan(Etat.Sauvegarde);

        // Vérifier que bilanAnnuelMiseAJour n'est pas null
        assertNotNull(bilanAnnuelMiseAJour);

        // Simuler le comportement de la méthode save pour retourner le bilan annuel mis à jour
        when(bilanAnnuelRepository.save(any(BilanAnnuel.class))).thenReturn(bilanAnnuelMiseAJour);

        // Appeler la méthode mettreAJourBilanAnnuel() avec l'ID 1L
        BilanAnnuel result = bilanAnnuelService.mettreAJourBilanAnnuel(1L, bilanAnnuelMiseAJour);

        // Vérifier que le résultat n'est pas nul et que les attributs sont correctement mis à jour
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("New Objectives", result.getObjectifAtteints());
        assertEquals("Future Objectives", result.getObjectifsFuturs());
        assertEquals("Projects", result.getProjetsAccomplis());
        assertEquals("Challenges", result.getChallenges());
        assertEquals(Etat.Sauvegarde, result.getEtatBilan());

        // Vérifier que la méthode findById a été appelée une fois avec l'ID 1L
        verify(bilanAnnuelRepository, times(1)).findById(1L);

        // Capturer l'objet passé à la méthode save
        ArgumentCaptor<BilanAnnuel> captor = ArgumentCaptor.forClass(BilanAnnuel.class);
        verify(bilanAnnuelRepository, times(1)).save(captor.capture());

        // Vérifier que les attributs de l'objet capturé sont mis à jour
        BilanAnnuel savedBilanAnnuel = captor.getValue();
        assertEquals(1L, savedBilanAnnuel.getId());
        assertEquals("New Objectives", savedBilanAnnuel.getObjectifAtteints());
        assertEquals("Future Objectives", savedBilanAnnuel.getObjectifsFuturs());
        assertEquals("Projects", savedBilanAnnuel.getProjetsAccomplis());
        assertEquals("Challenges", savedBilanAnnuel.getChallenges());
        assertEquals(Etat.Sauvegarde, savedBilanAnnuel.getEtatBilan());
    }


    @Test
    void testMettreAJourBilanAnnuel_NotFound() {
        when(bilanAnnuelRepository.findById(1L)).thenReturn(Optional.empty());

        BilanAnnuel updatedBilan = new BilanAnnuel();

        assertThrows(EntityNotFoundException.class, () -> {
            bilanAnnuelService.mettreAJourBilanAnnuel(1L, updatedBilan);
        });
    }

    @Test
    void testMettreAJourBilanAnnuelEtEnvoye() {
        // Créer un bilan annuel existant avec l'ID 1L
        BilanAnnuel bilanAnnuelExistant = new BilanAnnuel();
        bilanAnnuelExistant.setId(1L);
        bilanAnnuelExistant.setObjectifAtteints("Old Objectives");
        bilanAnnuelExistant.setObjectifsFuturs("Old Future Objectives");
        bilanAnnuelExistant.setProjetsAccomplis("Old Projects");
        bilanAnnuelExistant.setChallenges("Old Challenges");
        bilanAnnuelExistant.setEtatBilan(Etat.Sauvegarde);

        // Configurer le mock pour retourner le bilanAnnuelExistant lorsque findById est appelé avec l'ID 1L
        when(bilanAnnuelRepository.findById(1L)).thenReturn(Optional.of(bilanAnnuelExistant));

        // Créer un bilan annuel mis à jour
        BilanAnnuel bilanAnnuelMiseAJour = new BilanAnnuel();
        bilanAnnuelMiseAJour.setId(1L); // Définir l'ID du bilan mis à jour
        bilanAnnuelMiseAJour.setObjectifAtteints("New Objectives");
        bilanAnnuelMiseAJour.setObjectifsFuturs("Future Objectives");
        bilanAnnuelMiseAJour.setProjetsAccomplis("Projects");
        bilanAnnuelMiseAJour.setChallenges("Challenges");
        bilanAnnuelMiseAJour.setEtatBilan(Etat.Envoye);

        // Vérifier que bilanAnnuelMiseAJour n'est pas null
        assertNotNull(bilanAnnuelMiseAJour);

        log.info("bilanAnnuelMiseAJour is not null");

        // Simuler le comportement de la méthode save pour retourner le bilan annuel mis à jour
        when(bilanAnnuelRepository.save(any(BilanAnnuel.class))).thenReturn(bilanAnnuelMiseAJour);

        // Appeler la méthode mettreAJourBilanAnnuel() avec l'ID 1L
        BilanAnnuel result = bilanAnnuelService.mettreAJourBilanAnnuelEtEnvoye(1L, bilanAnnuelMiseAJour);

        // Vérifier que le résultat n'est pas nul et que les attributs sont correctement mis à jour
        assertNotNull(result);
        log.info("result is not null");

        assertEquals(1L, result.getId());
        assertEquals("New Objectives", result.getObjectifAtteints());
        assertEquals("Future Objectives", result.getObjectifsFuturs());
        assertEquals("Projects", result.getProjetsAccomplis());
        assertEquals("Challenges", result.getChallenges());
        assertEquals(Etat.Envoye, result.getEtatBilan());
        log.info("Attributes of result are correctly updated");

        // Vérifier que la méthode findById a été appelée une fois avec l'ID 1L
        verify(bilanAnnuelRepository, times(1)).findById(1L);
        log.info("findById called once with ID 1L");

        // Capturer l'objet passé à la méthode save
        ArgumentCaptor<BilanAnnuel> captor = ArgumentCaptor.forClass(BilanAnnuel.class);
        verify(bilanAnnuelRepository, times(1)).save(captor.capture());

        // Vérifier que les attributs de l'objet capturé sont mis à jour
        BilanAnnuel savedBilanAnnuel = captor.getValue();
        assertEquals(1L, savedBilanAnnuel.getId());
        assertEquals("New Objectives", savedBilanAnnuel.getObjectifAtteints());
        assertEquals("Future Objectives", savedBilanAnnuel.getObjectifsFuturs());
        assertEquals("Projects", savedBilanAnnuel.getProjetsAccomplis());
        assertEquals("Challenges", savedBilanAnnuel.getChallenges());
        assertEquals(Etat.Envoye, savedBilanAnnuel.getEtatBilan());
        log.info("Attributes of savedBilanAnnuel are correctly updated");

    }

    @Test
    void testGetBilanById() {
        when(bilanAnnuelRepository.findById(1L)).thenReturn(Optional.of(bilanAnnuel));

        BilanAnnuel result = bilanAnnuelService.getBilanById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }
    @Test
    void testGetBilansByCollaborateurId() {
        when(bilanAnnuelRepository.findByCollaborateurId(1L)).thenReturn(Arrays.asList(bilanAnnuel));

        List<BilanAnnuel> bilans = bilanAnnuelService.getBilansByCollaborateurId(1L);

        assertNotNull(bilans);
        assertEquals(1, bilans.size());
        assertEquals(collaborateur1, bilans.get(0).getCollaborateur());
    }


    @Test
    void testGetBilansAnnuelByCollaborateur() {
        when(bilanAnnuelRepository.findByCollaborateur(collaborateur1)).thenReturn(Arrays.asList(bilanAnnuel));

        List<BilanAnnuel> bilans = bilanAnnuelService.getBilansAnnuelByCollaborateur(collaborateur1);

        assertNotNull(bilans);
        assertEquals(1, bilans.size());
        assertEquals(collaborateur1, bilans.get(0).getCollaborateur());
    }




}
