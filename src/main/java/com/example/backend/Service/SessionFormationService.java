package com.example.backend.Service;

import com.example.backend.Entity.*;
import com.example.backend.Repository.FormationRepository;
import com.example.backend.Repository.GroupsRespository;
import com.example.backend.Repository.SessionFormationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.Session;
import javax.persistence.EntityNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionFormationService {

    @Autowired
    private SessionFormationRepository sessionFormationRepository;
    @Autowired
    private GroupsRespository groupsRepository;
    @Autowired
    private FormationRepository formationRepository;


    public SessionFormation addSessionFormation(Long formationId , Long groupId, String dateDebut, String dateFin) {
        Groups group = groupsRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + groupId));
        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + formationId));

        SessionFormation sessionFormation = new SessionFormation();
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
}

