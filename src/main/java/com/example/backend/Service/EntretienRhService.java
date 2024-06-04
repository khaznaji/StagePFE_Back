package com.example.backend.Service;

import com.example.backend.Configuration.MailConfig;
import com.example.backend.Entity.*;
import com.example.backend.Repository.CandidatureRepository;
import com.example.backend.Repository.EntretienRhRepository;
import com.example.backend.Repository.PosteRepository;
import com.example.backend.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service

public class EntretienRhService {
 @Autowired
    PosteRepository posteRepository ;
    @Autowired
    CandidatureRepository candidatureRepository ;
    @Autowired
    UserRepository userRepository ;
    @Autowired
    EntretienRhRepository entretienRhRepository ;

    public void createEntretienForPosteAndCandidatureAndUser(Long postId, Long candidatureId, String dateEntretien, String heureDebut, String heureFin, Long userId) {
        Poste poste = posteRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Poste not found with id: " + postId));

        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new IllegalArgumentException("Candidature not found with id: " + candidatureId));
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'ID : " + userId));

        EntretienRh entretien = new EntretienRh();
        entretien.setCandidature(candidature);
        entretien.setDateEntretien(dateEntretien); // Setting dynamic date
        entretien.setHeureDebut(heureDebut); // Setting dynamic start time
        entretien.setHeureFin(heureFin); // Setting dynamic end time
        entretien.setPoste(poste);
        String roomId = generateRandomRoomId();
        entretien.setRoomId(roomId);
        entretien.setEtatEntretien(EtatEntretien.En_Attente);
        entretien.setUser(user);
        entretienRhRepository.save(entretien); // Sauvegarder l'entretien dans la base de données
    }
    public Optional<EntretienRh> getEntretienById(Long id) {
        return entretienRhRepository.findById(id);
    }
@Autowired
private MailConfig emailService ;
    public void updateEntretien(Long id, String dateEntretien, String heureDebut, String heureFin, Long userId) {
        // Vérifiez d'abord si l'entretien existe
        EntretienRh entretien = entretienRhRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entretien not found"));

        // Vérifier si d'autres champs nécessitent une mise à jour
        entretien.setDateEntretien(dateEntretien);
        entretien.setHeureDebut(heureDebut);
        entretien.setHeureFin(heureFin);

        // Récupérer l'ancienne candidature associée à l'entretien
        Candidature ancienneCandidature = entretien.getCandidature();

        // Mettez à jour l'utilisateur si nécessaire
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'ID : " + userId));
        entretien.setUser(user);

        // Réaffecter l'ancienne candidature à l'entretien
        entretien.setCandidature(ancienneCandidature);

        // Enregistrez les modifications dans la base de données
        entretienRhRepository.save(entretien);

        // Envoi d'e-mails pour informer les utilisateurs concernés
        informerUtilisateurs(entretien);
    }



    private void informerUtilisateurs(EntretienRh entretien) {
        // Envoyer un e-mail au manager RH
        String subjectManager = "Modification de l'entretien RH";
        String contentManager = "Cher Manager RH, l'entretien RH a été modifié avec succès.";

        emailService.sendEmail(entretien.getUser().getEmail(), subjectManager, contentManager);

        String titrePoste = entretien.getCandidature().getPoste().getTitre();

        // Envoyer un e-mail au collaborateur associé à la candidature
        String subjectCollaborateur = "Modification de l'entretien RH pour le poste : " + titrePoste;
        String contentCollaborateur = "Cher Collaborateur, l'entretien RH prévu pour le poste '" + titrePoste + "' a été modifié. Veuillez vérifier les détails.";

        emailService.sendEmail(entretien.getCandidature().getCollaborateur().getCollaborateur().getEmail(), subjectCollaborateur, contentCollaborateur);
    }

    public String generateRandomRoomId() {
        // Génère un identifiant UUID aléatoire
        return UUID.randomUUID().toString();
    }
    public List<EntretienRh> getAllEntretiens() {
        return entretienRhRepository.findAll();
    }
    public List<EntretienRh> getEntretiensByPosteId(Long posteId) {
        return entretienRhRepository.findByPosteId(posteId);
    }

    public List<EntretienRh> getEntretiensByCollaborateurId(Long collaborateurId) {
        return entretienRhRepository.findByCandidature_Collaborateur_Id(collaborateurId);
    }
    public void noterEntretien(Long id, int salaire ) {
        EntretienRh entretien = entretienRhRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entretien non trouvé avec l'ID : " + id));
        entretien.setSalaire(salaire);
        entretien.setEtatEntretien(EtatEntretien.Termine);
        entretienRhRepository.save(entretien);
    }
    public List<EntretienRh> getEntretiensByPoste(Long postId) {
        return entretienRhRepository.findByPosteId(postId);
    }
    public Optional<EntretienRh> getEntretienByCandidatureId(Long candidatureId) {
        return entretienRhRepository.findByCandidatureId(candidatureId);
    }
    public void deleteEntretien(Long id) {
        entretienRhRepository.deleteById(id);
    }
    public List<EntretienRh> getEntretiensByManagerRhId(Long managerRhId) {
        return entretienRhRepository.findByUser_Id(managerRhId);
    }


}
