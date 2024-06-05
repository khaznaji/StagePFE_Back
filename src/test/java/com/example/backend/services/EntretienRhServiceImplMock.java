package com.example.backend.services;

import com.example.backend.Service.EntretienRhService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.backend.Configuration.MailConfig;
import com.example.backend.Entity.*;
import com.example.backend.Repository.CandidatureRepository;
import com.example.backend.Repository.EntretienRhRepository;
import com.example.backend.Repository.PosteRepository;
import com.example.backend.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class EntretienRhServiceImplMock {

    @Mock
    private PosteRepository posteRepository;

    @Mock
    private CandidatureRepository candidatureRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EntretienRhRepository entretienRhRepository;

    @Mock
    private MailConfig emailService;

    @InjectMocks
    private EntretienRhService entretienRhService;

    @Test
    void testCreateEntretienForPosteAndCandidatureAndUser() {
        // Mock les données d'entrée
        Long postId = 1L;
        Long candidatureId = 1L;
        String dateEntretien = "2023-06-05";
        String heureDebut = "10:00";
        String heureFin = "11:00";
        Long userId = 1L;

        // Stub les méthodes des repositories pour retourner les objets nécessaires
        Poste poste = new Poste();
        when(posteRepository.findById(postId)).thenReturn(Optional.of(poste));

        Candidature candidature = new Candidature();
        when(candidatureRepository.findById(candidatureId)).thenReturn(Optional.of(candidature));

        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Appel de la méthode à tester
        entretienRhService.createEntretienForPosteAndCandidatureAndUser(postId, candidatureId, dateEntretien, heureDebut, heureFin, userId);

        // Vérification que l'entretien est sauvegardé dans le repository
        verify(entretienRhRepository, times(1)).save(any(EntretienRh.class));
    }
    @Test
    void testGetEntretienById() {
        // Mock les données d'entrée
        Long id = 1L;
        EntretienRh entretien = new EntretienRh();
        when(entretienRhRepository.findById(id)).thenReturn(Optional.of(entretien));

        // Appel de la méthode à tester
        Optional<EntretienRh> result = entretienRhService.getEntretienById(id);

        // Vérification que l'entretien retourné correspond à celui dans le repository
        assertTrue(result.isPresent());
        assertEquals(entretien, result.get());
    }
/*@Test
void testUpdateEntretien() {
    Long id = 1L;
    String dateEntretien = "2023-06-05";
    String heureDebut = "10:00";
    String heureFin = "11:00";
    Long userId = 1L;

    // Mock the user
    User user = mock(User.class);
    when(user.getId()).thenReturn(userId);
    when(user.getEmail()).thenReturn("collaborateur@example.com");
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    // Mock the candidature and its associated objects
    Poste poste = mock(Poste.class);
    when(poste.getTitre()).thenReturn("Titre du poste");

    Collaborateur collaborateur = mock(Collaborateur.class);
    when(collaborateur.getCollaborateur()).thenReturn(user);

    Candidature candidature = mock(Candidature.class);
    when(candidature.getPoste()).thenReturn(poste);
    when(candidature.getCollaborateur()).thenReturn(collaborateur);

    // Mock entretien
    EntretienRh entretien = new EntretienRh();
    entretien.setCandidature(candidature);
    when(entretienRhRepository.findById(id)).thenReturn(Optional.of(entretien));

    // Call the method to test
    entretienRhService.updateEntretien(id, dateEntretien, heureDebut, heureFin, userId);

    // Verify the fields of the entretien have been updated
    assertEquals(dateEntretien, entretien.getDateEntretien());
    assertEquals(heureDebut, entretien.getHeureDebut());
    assertEquals(heureFin, entretien.getHeureFin());

    // Verify the user associated with the entretien has been updated
    verify(userRepository, times(1)).findById(userId);
    assertEquals(user, entretien.getUser());

    // Verify the modifications have been saved to the repository
    verify(entretienRhRepository, times(1)).save(entretien);

    // Verify emails have been sent to the concerned users
    verify(emailService, times(2)).sendEmail(anyString(), anyString(), anyString());
}
*/
    @Test
    void testUpdateEntretien() {
        Long id = 1L;
        String dateEntretien = "2023-06-05";
        String heureDebut = "10:00";
        String heureFin = "11:00";
        Long userId = 1L;

        // Mock the user
        User user = mock(User.class);
        lenient().when(user.getId()).thenReturn(userId);
        lenient().when(user.getEmail()).thenReturn("collaborateur@example.com");
        lenient().when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Mock the candidature and its associated objects
        Candidature candidature = mock(Candidature.class);
        Poste poste = mock(Poste.class);
        lenient().when(poste.getTitre()).thenReturn("Titre du poste");
        lenient().when(candidature.getPoste()).thenReturn(poste);

        // Mock collaborateur and set the user
        Collaborateur collaborateur = mock(Collaborateur.class);
        lenient().when(collaborateur.getCollaborateur()).thenReturn(user);
        lenient().when(candidature.getCollaborateur()).thenReturn(collaborateur);

        // Mock entretien
        EntretienRh entretien = new EntretienRh();
        entretien.setCandidature(candidature);
        lenient().when(entretienRhRepository.findById(id)).thenReturn(Optional.of(entretien));

        // Call the method to test
        entretienRhService.updateEntretien(id, dateEntretien, heureDebut, heureFin, userId);

        // Verify the fields of the entretien have been updated
        assertEquals(dateEntretien, entretien.getDateEntretien());
        assertEquals(heureDebut, entretien.getHeureDebut());
        assertEquals(heureFin, entretien.getHeureFin());

        // Verify the user associated with the entretien has been updated
        verify(userRepository, times(1)).findById(userId);
        assertNotNull(entretien.getUser());

        // Verify the modifications have been saved to the repository
        verify(entretienRhRepository, times(1)).save(entretien);

        // Verify emails have been sent to the concerned users
        verify(emailService, times(2)).sendEmail(anyString(), anyString(), anyString());
    }



    @Test
    void testGetAllEntretiens() {
        // Mock des données d'entretiens
        List<EntretienRh> entretiens = new ArrayList<>();
        entretiens.add(new EntretienRh());
        entretiens.add(new EntretienRh());
        when(entretienRhRepository.findAll()).thenReturn(entretiens);

        // Appel de la méthode à tester
        List<EntretienRh> result = entretienRhService.getAllEntretiens();

        // Vérification que la liste retournée contient tous les entretiens
        assertEquals(entretiens.size(), result.size());
        assertTrue(result.containsAll(entretiens));
    }

    @Test
    void testGetEntretiensByPosteId() {
        // Mock des données d'entretiens
        Long posteId = 1L;
        List<EntretienRh> entretiens = new ArrayList<>();
        entretiens.add(new EntretienRh());
        entretiens.add(new EntretienRh());
        when(entretienRhRepository.findByPosteId(posteId)).thenReturn(entretiens);

        // Appel de la méthode à tester
        List<EntretienRh> result = entretienRhService.getEntretiensByPosteId(posteId);

        // Vérification que la liste retournée contient tous les entretiens pour le poste spécifié
        assertEquals(entretiens.size(), result.size());
        assertTrue(result.containsAll(entretiens));
    }
    @Test
    void testNoterEntretien() {
        // Données de test
        Long id = 1L;
        int salaire = 50000;
        EntretienRh entretien = new EntretienRh();
        when(entretienRhRepository.findById(id)).thenReturn(Optional.of(entretien));

        // Appel de la méthode à tester
        assertDoesNotThrow(() -> entretienRhService.noterEntretien(id, salaire));

        // Vérification que le salaire a été mis à jour et que l'état de l'entretien est Terminé
        assertEquals(salaire, entretien.getSalaire());
        assertEquals(EtatEntretien.Termine, entretien.getEtatEntretien());
        verify(entretienRhRepository, times(1)).save(entretien);
    }

    @Test
    void testGetEntretiensByPoste() {
        // Données de test
        Long postId = 1L;
        List<EntretienRh> entretiens = new ArrayList<>();
        entretiens.add(new EntretienRh());
        entretiens.add(new EntretienRh());
        when(entretienRhRepository.findByPosteId(postId)).thenReturn(entretiens);

        // Appel de la méthode à tester
        List<EntretienRh> result = entretienRhService.getEntretiensByPoste(postId);

        // Vérification que la liste retournée contient tous les entretiens pour le poste spécifié
        assertEquals(entretiens.size(), result.size());
        assertTrue(result.containsAll(entretiens));
    }
    @Test
    void testDeleteEntretien() {
        // Données de test
        Long id = 1L;
        doNothing().when(entretienRhRepository).deleteById(id);

        // Appel de la méthode à tester
        assertDoesNotThrow(() -> entretienRhService.deleteEntretien(id));

        // Vérification que la méthode deleteById du repository est appelée une fois avec l'ID spécifié
        verify(entretienRhRepository, times(1)).deleteById(id);
    }

    @Test
    void testGetEntretiensByManagerRhId() {
        // Données de test
        Long managerRhId = 1L;
        List<EntretienRh> entretiens = new ArrayList<>();
        entretiens.add(new EntretienRh());
        entretiens.add(new EntretienRh());
        when(entretienRhRepository.findByUser_Id(managerRhId)).thenReturn(entretiens);

        // Appel de la méthode à tester
        List<EntretienRh> result = entretienRhService.getEntretiensByManagerRhId(managerRhId);

        // Vérification que la liste retournée contient tous les entretiens pour le manager RH spécifié
        assertEquals(entretiens.size(), result.size());
        assertTrue(result.containsAll(entretiens));
    }
}
