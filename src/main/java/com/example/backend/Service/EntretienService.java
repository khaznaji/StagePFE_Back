package com.example.backend.Service;

import com.example.backend.Entity.Candidature;
import com.example.backend.Entity.Entretien;

import com.example.backend.Entity.Poste;
import com.example.backend.Repository.CandidatureRepository;
import com.example.backend.Repository.EntretienRepository;
import com.example.backend.Repository.PosteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        return entretienRepository.save(entretien);
    }
    public String generateRandomRoomId() {
        // Génère un identifiant UUID aléatoire
        return UUID.randomUUID().toString();
    }
    public Entretien updateEntretien(Long id, Entretien updatedEntretien) {
        Optional<Entretien> existingEntretien = entretienRepository.findById(id);
        if (existingEntretien.isPresent()) {
            updatedEntretien.setId(id);
            return entretienRepository.save(updatedEntretien);
        }
        return null; // Gérer le cas où l'entretien n'existe pas
    }

    public void deleteEntretien(Long id) {
        entretienRepository.deleteById(id);
    }
}
