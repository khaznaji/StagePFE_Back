package com.example.backend.Service;

import com.example.backend.Entity.*;
import com.example.backend.Repository.FormateurRepository;
import com.example.backend.Repository.FormationRepository;
import com.example.backend.Repository.GroupsRespository;
import com.example.backend.Repository.SessionFormationRepository;
import com.example.backend.Security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.mail.Session;
import javax.persistence.EntityNotFoundException;
import java.util.*;

@Service
public class SessionFormationService {

    @Autowired
    private SessionFormationRepository sessionFormationRepository;
    @Autowired
    private GroupsRespository groupsRepository;
    @Autowired
    private FormationRepository formationRepository;
    @Autowired
    private FormateurRepository formateurRepository;


    public SessionFormation addSessionFormation(Long formationId , Long groupId, String dateDebut, String dateFin) {
        Groups group = groupsRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + groupId));
        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + formationId));
        SessionFormation sessionFormation = new SessionFormation();
        group.setEtat(Etat.Termine);
        groupsRepository.save(group);
        sessionFormation.setGroup(group);
        sessionFormation.setFormation(formation);
        sessionFormation.setDateDebut(dateDebut);
        sessionFormation.setDateFin(dateFin);
        String roomId = generateRandomRoomId();
        sessionFormation.setRoomId(roomId);
        return sessionFormationRepository.save(sessionFormation);
    }
    public String generateRandomRoomId() {
        // Génère un identifiant UUID aléatoire
        return UUID.randomUUID().toString();
    }
    public List<SessionFormation> getAllSessionsByFormation(Long formationId) {
        return sessionFormationRepository.findByFormationId(formationId);
    }

    public SessionFormation updateSessionFormation(Long sessionId, Long groupId, String dateDebut, String dateFin) {
        // Recherche de la session de formation à mettre à jour
        Optional<SessionFormation> optionalSession = sessionFormationRepository.findById(sessionId);
        Groups group = groupsRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + groupId));
        // Vérification si la session existe
        if (optionalSession.isPresent()) {
            SessionFormation session = optionalSession.get();

            // Mettre à jour les champs de la session avec les nouvelles valeurs
            session.setGroup(group);
            session.setDateDebut(dateDebut);
            session.setDateFin(dateFin);

            // Enregistrer la session mise à jour dans la base de données
            return sessionFormationRepository.save(session);
        } else {
            // Si la session n'existe pas, renvoyer null ou lever une exception selon la logique de votre application
            return null;
        }
    }
    public SessionFormation getSessionFormationById(Long sessionId) {
        // Logique pour récupérer la session de formation par son ID
        Optional<SessionFormation> session = sessionFormationRepository.findById(sessionId);
        return session.orElse(null);
    }
    public void deleteSessionFormation(Long sessionId) {
        // Implémentez la logique pour supprimer la session de formation avec l'ID donné
        sessionFormationRepository.deleteById(sessionId);
    }
    public List<SessionFormation> getSessionsFormationByFormateurConnecte() {
        // Récupérer les détails de l'utilisateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long formateurId = userDetails.getId();

        // Récupérer le formateur connecté
        Formateur formateur = formateurRepository.findByFormateurFormateurId(formateurId)
                .orElseThrow(() -> new EntityNotFoundException("Formateur non trouvé avec l'ID : " + formateurId));

        // Récupérer toutes les formations du formateur
        Formation formation = formateur.getFormation();

        // Liste pour stocker les sessions de formation du formateur
        if (formation != null) {
            // Si la formation existe, récupérer ses sessions
            List<SessionFormation> sessions = formation.getSessions();
            return sessions;
        } else {
            // Si la formation n'existe pas, retourner une liste vide
            return Collections.emptyList();
        }

    }
    public List<SessionFormation> getSessionsByFormateur(Formateur formateur) {
        // Récupérer le groupe associé à ce formateur
        List<Groups> groups = formateur.getGroups();

        // Si le formateur n'est pas associé à un groupe, renvoyer une liste vide
        if (groups.isEmpty()) {
            return Collections.emptyList();
        }

        // Initialiser une liste pour stocker toutes les sessions de formation associées à ce formateur
        List<SessionFormation> sessions = new ArrayList<>();

        // Parcourir tous les groupes associés à ce formateur
        for (Groups group : groups) {
            // Récupérer les sessions de formation pour ce groupe
            List<SessionFormation> sessionsForGroup = sessionFormationRepository.findByGroup_Id(group.getId());

            // Ajouter toutes les sessions de formation de ce groupe à la liste des sessions
            sessions.addAll(sessionsForGroup);
        }

        // Renvoyer la liste complète des sessions de formation associées à ce formateur
        return sessions;
    }
    public List<SessionFormation> getSessionsByCollaborateur(Collaborateur collaborateur) {
        // Récupérer tous les groupes associés à ce collaborateur
        List<Groups> groups = collaborateur.getGroups();

        // Initialiser une liste pour stocker toutes les sessions de formation associées à ce collaborateur
        List<SessionFormation> sessions = new ArrayList<>();

        // Parcourir tous les groupes associés à ce collaborateur
        for (Groups group : groups) {
            // Récupérer les sessions de formation pour ce groupe
            List<SessionFormation> sessionsForGroup = sessionFormationRepository.findByGroup_Id(group.getId());

            // Ajouter toutes les sessions de formation de ce groupe à la liste des sessions
            sessions.addAll(sessionsForGroup);
        }

        // Renvoyer la liste complète des sessions de formation associées à ce collaborateur
        return sessions;
    }

}

