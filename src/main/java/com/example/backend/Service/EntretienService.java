package com.example.backend.Service;

import com.example.backend.Entity.*;

import com.example.backend.Repository.*;
import com.example.backend.Security.services.UserDetailsImpl;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import org.apache.catalina.Manager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EntretienService {
    @Autowired
    private EntretienRepository entretienRepository;

    @Autowired
    private CandidatureRepository candidatureRepository;
    @Autowired
    private PosteRepository posteRepository;
    @Autowired
    private ManagerServiceRepository managerServiceRepository ;

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
        entretien.setTypeEntretien(TypeEntretien.Technique);

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
        entretien.setTypeEntretien(TypeEntretien.Technique);

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

    public List<Entretien> getEntretiensTechniquesByPoste(Long postId) {
        // Récupérer tous les entretiens liés au poste avec l'ID donné
        List<Entretien> entretiens = entretienRepository.findByPosteId(postId);

        // Filtrer les entretiens pour ne conserver que ceux de type "Technique"
        List<Entretien> entretiensTechniques = entretiens.stream()
                .filter(entretien -> entretien.getTypeEntretien() == TypeEntretien.Technique)
                .collect(Collectors.toList());

        return entretiensTechniques;
    }
    public Optional<Entretien> getEntretienByCandidatureId(Long candidatureId) {
        return entretienRepository.findByCandidatureId(candidatureId);
    }
    @Autowired
    private CollaborateurRepository collaborateurRepository ;
    public void ajoutEntretienAnnuel(Long collaborateurId, String dateEntretien, String heureDebut, String heureFin) {
        // Récupérer l'utilisateur connecté (manager)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long managerServiceId = userDetails.getId();
        ManagerService managerService = managerServiceRepository.findByManagerManagerId(managerServiceId)
                .orElseThrow(() -> new EntityNotFoundException("ManagerService non trouvé avec l'ID : " + managerServiceId));

        // Créer un nouvel entretien
        Entretien entretien = new Entretien();

        // Set autres attributs de l'entretien
        entretien.setDateEntretien(dateEntretien);
        entretien.setHeureDebut(heureDebut);
        entretien.setHeureFin(heureFin);
        entretien.setEtatEntretien(EtatEntretien.En_Attente);

        entretien.setTypeEntretien(TypeEntretien.Annuel );

        // Trouver le collaborateur par son ID et le lier à l'entretien
        Collaborateur collaborateur = collaborateurRepository.findById(collaborateurId).orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouvé avec l'ID : " + collaborateurId));
        entretien.setCollaborateurs(collaborateur);
        String roomId = generateRandomRoomId();
        entretien.setRoomId(roomId);
        // Set le manager responsable de l'entretien
        entretien.setManagerService(managerService);

        // Enregistrer l'entretien dans la base de données
        entretienRepository.save(entretien);
    }
    public void updateEntretienAnnuel(Long entretienId, String dateEntretien, String heureDebut, String heureFin) {
        // Récupérer l'utilisateur connecté (manager)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long managerServiceId = userDetails.getId();
        ManagerService managerService = managerServiceRepository.findByManagerManagerId(managerServiceId)
                .orElseThrow(() -> new EntityNotFoundException("ManagerService non trouvé avec l'ID : " + managerServiceId));

        // Vérifier si l'entretien existe
        Entretien existingEntretien = entretienRepository.findById(entretienId).orElseThrow(() -> new EntityNotFoundException("Entretien non trouvé avec l'ID : " + entretienId));



        // Mettre à jour les attributs de l'entretien
        existingEntretien.setDateEntretien(dateEntretien);
        existingEntretien.setHeureDebut(heureDebut);
        existingEntretien.setHeureFin(heureFin);
        existingEntretien.setTypeEntretien(TypeEntretien.Annuel);
        existingEntretien.setEtatEntretien(EtatEntretien.En_Attente);
        existingEntretien.setManagerService(managerService);
        String roomId = generateRandomRoomId();
        existingEntretien.setRoomId(roomId);
        // Enregistrer les modifications dans la base de données
        entretienRepository.save(existingEntretien);
    }
    public List<Map<String, Object>> getEntretiensAnnuelDuManagerConnecte() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ManagerService managerService = userDetails.getUser().getManagerService();

        Long managerId = managerService.getId();

        List<Entretien> entretiensDuManager = entretienRepository.findByManagerServiceId(managerId);

        List<Map<String, Object>> entretiensAvecCollaborateurs = new ArrayList<>();

        for (Entretien entretien : entretiensDuManager) {
            if (entretien.getTypeEntretien() == TypeEntretien.Annuel) {
                Map<String, Object> entretienAvecCollaborateur = new HashMap<>();
                entretienAvecCollaborateur.put("entretien", entretien);


                    Collaborateur collaborateur = entretien.getCollaborateurs();
                    if (collaborateur != null) {
                        entretienAvecCollaborateur.put("nomCollaborateur", collaborateur.getCollaborateur().getNom());
                        entretienAvecCollaborateur.put("prenomCollaborateur", collaborateur.getCollaborateur().getPrenom());
                        entretienAvecCollaborateur.put("collaborateurId", collaborateur.getCollaborateur().getId());

                }
                entretiensAvecCollaborateurs.add(entretienAvecCollaborateur);
            }
        }

        return entretiensAvecCollaborateurs;
    }
    public List<Map<String, Object>> getEntretiensAnnuelDuCollaborateurConnecte() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Collaborateur collaborateur = userDetails.getUser().getCollaborateur();

        Long collaborateurId = collaborateur.getId();

        List<Entretien> entretiensDuCollaborateur = entretienRepository.findByCollaborateurs_Id(collaborateurId);

        List<Map<String, Object>> entretiensAvecCollaborateurs = new ArrayList<>();

        for (Entretien entretien : entretiensDuCollaborateur) {
            if (entretien.getTypeEntretien() == TypeEntretien.Annuel) {
                Map<String, Object> entretienAvecCollaborateur = new HashMap<>();
                entretienAvecCollaborateur.put("entretien", entretien);

                // Ajoutez les informations du collaborateur

                // Récupérez le manager associé à l'entretien
                ManagerService managerService = entretien.getManagerService();
                if (managerService != null) {
                    User manager = managerService.getManager();
                    if (manager != null) {
                        entretienAvecCollaborateur.put("nomManager", manager.getNom());
                        entretienAvecCollaborateur.put("prenomManager", manager.getPrenom());
                    }
                }

                entretiensAvecCollaborateurs.add(entretienAvecCollaborateur);
            }
        }

        return entretiensAvecCollaborateurs;
    }
    public TypeEntretien getTypeEntretien(Long id) {
        Optional<Entretien> optionalEntretien = getEntretienById(id);
        Entretien entretien = optionalEntretien.orElseThrow(() -> new IllegalArgumentException("Entretien non trouvé"));

        // Logique pour déterminer le type d'entretien en fonction de l'entretien récupéré
        if (entretien.getTypeEntretien() == TypeEntretien.Annuel) {
            return TypeEntretien.Annuel;
        } else {
            return TypeEntretien.Technique;
        }
    }



}
