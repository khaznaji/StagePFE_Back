package com.example.backend.services;

import com.example.backend.Entity.*;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.FormationRepository;
import com.example.backend.Repository.GroupsRespository;
import com.example.backend.Repository.SessionFormationRepository;
import com.example.backend.Service.SessionFormationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionFormationImplMock {

    @Mock
    private SessionFormationRepository sessionFormationRepository;
    @Mock
    private CollaborateurRepository collaborateurRepository;

    @Mock
    private GroupsRespository groupsRepository;

    @Mock
    private FormationRepository formationRepository;

    @InjectMocks
    private SessionFormationService sessionFormationService;

@Test
public void testAddSessionFormation() {
    // Données de test
    Long formationId = 1L;
    Long groupId = 2L;
    String dateDebut = "2024-06-05";
    String dateFin = "2024-06-06";

    // Création de group simulé et de formation simulée
    Groups group = new Groups();
    group.setId(groupId);
    Formation formation = new Formation();
    formation.setId(formationId);

    // Définition du comportement simulé des repositories
    when(groupsRepository.findById(groupId)).thenReturn(Optional.of(group));
    when(formationRepository.findById(formationId)).thenReturn(Optional.of(formation));
    when(sessionFormationRepository.save(any(SessionFormation.class))).thenAnswer(invocation -> {
        SessionFormation savedSessionFormation = invocation.getArgument(0);
        savedSessionFormation.setId(1L); // Simuler l'attribution d'un ID lors de l'enregistrement
        return savedSessionFormation;
    });

    // Génération d'un identifiant de salle

    // Appel de la méthode à tester
    SessionFormation sessionFormation = sessionFormationService.addSessionFormation(formationId, groupId, dateDebut, dateFin);

    // Vérification des actions sur le group
    assertEquals(Etat.Termine, group.getEtat());
    verify(groupsRepository, times(1)).save(group);

    // Vérification de la session de formation créée
    assertNotNull(sessionFormation);
    assertEquals(group, sessionFormation.getGroup());
    assertEquals(formation, sessionFormation.getFormation());
    assertEquals(dateDebut, sessionFormation.getDateDebut());
    assertEquals(dateFin, sessionFormation.getDateFin());
    verify(sessionFormationRepository, times(1)).save(sessionFormation);
}


    @Test
    void testGetAllSessionsByFormation() {
        // Données de test
        Long formationId = 1L;
        SessionFormation session1 = new SessionFormation();
        SessionFormation session2 = new SessionFormation();
        List<SessionFormation> sessions = new ArrayList<>();
        sessions.add(session1);
        sessions.add(session2);

        // Configurer le mock du repository pour retourner les sessions de formation
        when(sessionFormationRepository.findByFormationId(formationId)).thenReturn(sessions);

        // Appeler la méthode à tester
        List<SessionFormation> result = sessionFormationService.getAllSessionsByFormation(formationId);

        // Vérifier
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testUpdateSessionFormation() {
        // Données de test
        Long sessionId = 1L;
        Long groupId = 1L;
        String dateDebut = "2024-06-05";
        String dateFin = "2024-06-06";

        // Création de la session de formation existante
        SessionFormation existingSession = new SessionFormation();
        existingSession.setId(sessionId);

        // Création du groupe
        Groups group = new Groups();
        group.setId(groupId);

        // Mock de la recherche de la session de formation par ID
        when(sessionFormationRepository.findById(sessionId)).thenReturn(Optional.of(existingSession));

        // Mock de la recherche du groupe par ID
        when(groupsRepository.findById(groupId)).thenReturn(Optional.of(group));

        // Mock de la méthode save du repository sessionFormationRepository pour renvoyer l'objet sauvegardé
        when(sessionFormationRepository.save(any(SessionFormation.class))).thenAnswer(invocation -> {
            SessionFormation savedSessionFormation = invocation.getArgument(0);
            savedSessionFormation.setId(1L); // Simuler l'attribution d'un ID lors de l'enregistrement
            return savedSessionFormation;
        });

        // Appel de la méthode à tester
        SessionFormation updatedSession = sessionFormationService.updateSessionFormation(sessionId, groupId, dateDebut, dateFin);

        // Vérifications
        assertNotNull(updatedSession); // Vérifie que la session mise à jour n'est pas nulle
        assertEquals(groupId, updatedSession.getGroup().getId()); // Vérifie que le groupe a été mis à jour correctement
        assertEquals(dateDebut, updatedSession.getDateDebut()); // Vérifie que la date de début a été mise à jour correctement
        assertEquals(dateFin, updatedSession.getDateFin()); // Vérifie que la date de fin a été mise à jour correctement
        verify(sessionFormationRepository, times(1)).save(existingSession); // Vérifie que la méthode save a été appelée une fois avec la session mise à jour
    }

    @Test
    void testGetSessionFormationById() {
        // Données de test
        Long sessionId = 1L;
        SessionFormation session = new SessionFormation();

        // Configurer le mock du repository pour retourner une session de formation optionnelle
        when(sessionFormationRepository.findById(sessionId)).thenReturn(Optional.of(session));

        // Appeler la méthode à tester
        SessionFormation result = sessionFormationService.getSessionFormationById(sessionId);

        // Vérifier
        assertNotNull(result);
        assertEquals(session, result);
    }

    @Test
    void testDeleteSessionFormation() {
        // Données de test
        Long sessionId = 1L;

        // Appeler la méthode à tester
        sessionFormationService.deleteSessionFormation(sessionId);

        // Vérifier que la méthode deleteById de sessionFormationRepository a été appelée une fois avec l'ID donné
        verify(sessionFormationRepository, times(1)).deleteById(sessionId);
    }
    @Test
    void testGetSessionsByFormateur() {
        // Données de test
        Formateur formateur = new Formateur();
        Groups group1 = new Groups();
        group1.setId(1L);
        Groups group2 = new Groups();
        group2.setId(2L);
        List<Groups> groups = new ArrayList<>();
        groups.add(group1);
        groups.add(group2);
        formateur.setGroups(groups);

        List<SessionFormation> sessionsForGroup1 = new ArrayList<>();
        sessionsForGroup1.add(new SessionFormation());
        sessionsForGroup1.add(new SessionFormation());
        List<SessionFormation> sessionsForGroup2 = new ArrayList<>();
        sessionsForGroup2.add(new SessionFormation());
        when(sessionFormationRepository.findByGroup_Id(1L)).thenReturn(sessionsForGroup1);
        when(sessionFormationRepository.findByGroup_Id(2L)).thenReturn(sessionsForGroup2);

        // Appeler la méthode à tester
        List<SessionFormation> sessions = sessionFormationService.getSessionsByFormateur(formateur);

        // Vérifier
        assertEquals(3, sessions.size()); // Vérifiez que le nombre de sessions est correct
    }


    @Test
    void testGetSessionsByCollaborateur() {
        // Données de test
        Collaborateur collaborateur = new Collaborateur();
        Groups group1 = new Groups();
        group1.setId(1L);
        Groups group2 = new Groups();
        group2.setId(2L);
        List<Groups> groups = new ArrayList<>();
        groups.add(group1);
        groups.add(group2);
        collaborateur.setGroups(groups);

        List<SessionFormation> sessionsForGroup1 = new ArrayList<>();
        sessionsForGroup1.add(new SessionFormation());
        sessionsForGroup1.add(new SessionFormation());
        List<SessionFormation> sessionsForGroup2 = new ArrayList<>();
        sessionsForGroup2.add(new SessionFormation());
        when(sessionFormationRepository.findByGroup_Id(1L)).thenReturn(sessionsForGroup1);
        when(sessionFormationRepository.findByGroup_Id(2L)).thenReturn(sessionsForGroup2);

        // Appeler la méthode à tester
        List<SessionFormation> sessions = sessionFormationService.getSessionsByCollaborateur(collaborateur);

        // Vérifier
        assertEquals(3, sessions.size()); // Vérifiez que le nombre de sessions est correct
    }
    @Test
    void testGetUsersInGroup() {
        // Données de test
        Long groupId = 1L;
        List<Collaborateur> collaborateurs = new ArrayList<>();
        collaborateurs.add(new Collaborateur());
        collaborateurs.add(new Collaborateur());
        when(collaborateurRepository.findByGroupsId(groupId)).thenReturn(collaborateurs);

        // Appeler la méthode à tester
        List<Collaborateur> result = sessionFormationService.getUsersInGroup(groupId);

        // Vérifier
        assertEquals(2, result.size()); // Vérifiez que le nombre de collaborateurs retournés est correct
    }
}