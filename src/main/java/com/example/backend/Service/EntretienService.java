package com.example.backend.Service;

import com.example.backend.Entity.Candidature;
import com.example.backend.Entity.Entretien;

import com.example.backend.Entity.EtatEntretien;
import com.example.backend.Entity.Poste;
import com.example.backend.Repository.CandidatureRepository;
import com.example.backend.Repository.EntretienRepository;
import com.example.backend.Repository.PosteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EntretienService {
    @Autowired
    private EntretienRepository entretienRepository;

    @Autowired
    private CandidatureRepository candidatureRepository;
    @Autowired
    private PosteRepository posteRepository;

    public List<Entretien> getAllEntretiens() {
        return entretienRepository.findAll();
    }

    public Optional<Entretien> getEntretienById(Long id) {
        return entretienRepository.findById(id);
    }
    public List<Entretien> getEntretiensByPosteId(Long posteId) {
        return entretienRepository.findByPosteId(posteId);
    }

    public Entretien createEntretien(Entretien entretien) {
        return entretienRepository.save(entretien);
    }

    public Entretien createEntretienForPosteAndCandidature(Long posteId, Long candidatureId, String dateEntretien, String heureDebut, String heureFin) {
        Poste poste = posteRepository.findById(posteId)
                .orElseThrow(() -> new IllegalArgumentException("Poste not found with id: " + posteId));

        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new IllegalArgumentException("Candidature not found with id: " + candidatureId));

        Entretien entretien = new Entretien();
        entretien.setCandidature(candidature);
        entretien.setDateEntretien(dateEntretien); // Setting dynamic date
        entretien.setHeureDebut(heureDebut); // Setting dynamic start time
        entretien.setHeureFin(heureFin); // Setting dynamic end time
        entretien.setPoste(poste);
        String roomId = generateRandomRoomId();
        entretien.setRoomId(roomId);
        entretien.setEtatEntretien(EtatEntretien.En_Attente);

        return entretienRepository.save(entretien);
    }
    public void updateEntretien(Long id, Long candidatureId, String dateEntretien, String heureDebut, String heureFin) {
        // Vérifiez d'abord si l'entretien existe
        Entretien entretien = entretienRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entretien not found"));
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new IllegalArgumentException("Candidature not found with id: " + candidatureId));

        // Mettez à jour les champs de l'entretien avec les nouvelles valeurs
        entretien.setCandidature(candidature);
        entretien.setDateEntretien(dateEntretien);
        entretien.setHeureDebut(heureDebut);
        entretien.setHeureFin(heureFin);

        // Enregistrez les modifications dans la base de données
        entretienRepository.save(entretien);
    }

    public String generateRandomRoomId() {
        // Génère un identifiant UUID aléatoire
        return UUID.randomUUID().toString();
    }


    public void deleteEntretien(Long id) {
        entretienRepository.deleteById(id);
    }

    public List<Entretien> getEntretiensByCollaborateurId(Long collaborateurId) {
        return entretienRepository.findByCandidature_Collaborateur_Id(collaborateurId);
    }
    public List<Entretien> getEntretiensByManagerId(Long collaborateurId) {
        return entretienRepository.findByCandidature_Collaborateur_Id(collaborateurId);
    }
    public void noterEntretien(Long id, int note, String commentaire) {
        Entretien entretien = entretienRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entretien non trouvé avec l'ID : " + id));
        entretien.setNote(note);
        entretien.setCommentaire(commentaire);
        entretien.setEtatEntretien(EtatEntretien.Termine);
        entretienRepository.save(entretien);
    }

    public List<Entretien> getEntretiensByPoste(Long postId) {
        return entretienRepository.findByPosteId(postId);
    }
    public Optional<Entretien> getEntretienByCandidatureId(Long candidatureId) {
        return entretienRepository.findByCandidatureId(candidatureId);
    }

}
