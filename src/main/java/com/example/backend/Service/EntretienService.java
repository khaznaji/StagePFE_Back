package com.example.backend.Service;

import com.example.backend.Configuration.MailConfig;
import com.example.backend.Entity.*;

import com.example.backend.Repository.*;
import com.example.backend.Security.services.UserDetailsImpl;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import org.apache.catalina.Manager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
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
    @Autowired
    private MailConfig emailService;

    public void updateEntretien(Long id , String dateEntretien, String heureDebut, String heureFin) {
        // Vérifiez d'abord si l'entretien existe
        Entretien entretien = entretienRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entretien not found"));
       Candidature ancienneCandidature = entretien.getCandidature();

        // Mettez à jour les champs de l'entretien avec les nouvelles valeurs
        entretien.setCandidature(ancienneCandidature);
        entretien.setDateEntretien(dateEntretien);
        entretien.setHeureDebut(heureDebut);
        entretien.setHeureFin(heureFin);
        entretien.setTypeEntretien(TypeEntretien.Technique);

        // Enregistrez les modifications dans la base de données
        entretienRepository.save(entretien);

        // Envoyer des emails
        String collaborateurEmail = ancienneCandidature.getCollaborateur().getCollaborateur().getEmail();
        String managerEmail = ancienneCandidature.getPoste().getManagerService().getManager().getEmail();
        String managerName = ancienneCandidature.getPoste().getManagerService().getManager().getNom();

        String subject = "Modification de l'entretien";
        String contentCollaborateur = String.format("Cher collaborateur,<br><br>L'entretien prévu le %s de %s à %s a été modifié. <br>Le manager de service est %s.<br><br>Cordialement.",
                dateEntretien, heureDebut, heureFin, managerName);
        String contentManager = String.format("Cher manager,<br><br>L'entretien pour le poste de %s a été modifié. <br>Le collaborateur a été informé.<br><br>Cordialement.",
                ancienneCandidature.getPoste().getTitre());


            emailService.sendEmail(collaborateurEmail, subject, contentCollaborateur);
            emailService.sendEmail(managerEmail, subject, contentManager);

    }
    public String generateRandomRoomId() {
        // Génère un identifiant UUID aléatoire
        return UUID.randomUUID().toString();
    }


    public void deleteEntretien(Long id) {
        // Vérifiez d'abord si l'entretien existe
        Entretien entretien = entretienRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entretien not found"));

        // Mettez à jour le champ typeEntretien à "En attente"
        entretien.setEtatEntretien(EtatEntretien.En_Attente);

        // Sauvegardez les modifications de l'entretien avant de le supprimer
        entretienRepository.save(entretien);

        // Supprimez l'entretien
        entretienRepository.deleteById(id);
    }


    public List<Entretien> getEntretiensByCollaborateurId(Long collaborateurId) {
        return entretienRepository.findByCandidature_Collaborateur_Id(collaborateurId);
    }
    public List<Entretien> getEntretiensByManagerId(Long collaborateurId) {
        return entretienRepository.findByCandidature_Collaborateur_Id(collaborateurId);
    }
    public void noterEntretien(Long id, int note) {
        Entretien entretien = entretienRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entretien non trouvé avec l'ID : " + id));
        entretien.setNote(note);
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
        // Retrieve the current authenticated manager
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long managerServiceId = userDetails.getId();
        ManagerService managerService = managerServiceRepository.findByManagerManagerId(managerServiceId)
                .orElseThrow(() -> new EntityNotFoundException("ManagerService non trouvé avec l'ID : " + managerServiceId));

        // Create a new Entretien
        Entretien entretien = new Entretien();

        // Set other attributes of the Entretien
        entretien.setDateEntretien(dateEntretien);
        entretien.setHeureDebut(heureDebut);
        entretien.setHeureFin(heureFin);
        entretien.setEtatEntretien(EtatEntretien.En_Attente);
        entretien.setTypeEntretien(TypeEntretien.Annuel);

        // Find the collaborateur by ID and link it to the Entretien
        Collaborateur collaborateur = collaborateurRepository.findById(collaborateurId)
                .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouvé avec l'ID : " + collaborateurId));
        entretien.setCollaborateurs(collaborateur);
        String roomId = generateRandomRoomId();
        entretien.setRoomId(roomId);

        // Set the manager responsible for the Entretien
        entretien.setManagerService(managerService);

        // Save the Entretien in the database
        entretienRepository.save(entretien);

        // Send email notifications
        String collaborateurName = collaborateur.getCollaborateur().getNom();
        String collaborateurEmail = collaborateur.getCollaborateur().getEmail();
        String managerName = managerService.getManager().getNom();
        String managerEmail = managerService.getManager().getEmail();

        String subject = "Entretien Annuel";

        String contentCollaborateur = String.format(
                "Cher collaborateur %s,<br><br>Un nouvel entretien annuel est prévu le %s de %s à %s. <br>Le manager de service est %s.<br><br>Cordialement.",
                collaborateurName, dateEntretien, heureDebut, heureFin, managerName);

        String contentManager = String.format(
                "Cher manager,<br><br>Un nouvel entretien annuel a été programmé pour le collaborateur %s.<br><br>Cordialement.",
                collaborateurName);

        emailService.sendEmail(collaborateurEmail, subject, contentCollaborateur);
        emailService.sendEmail(managerEmail, subject, contentManager);
    }

    public void updateEntretienAnnuel(Long entretienId, String dateEntretien, String heureDebut, String heureFin) {
        // Récupérer l'utilisateur connecté (manager)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long managerServiceId = userDetails.getId();
        ManagerService managerService = managerServiceRepository.findByManagerManagerId(managerServiceId)
                .orElseThrow(() -> new EntityNotFoundException("ManagerService non trouvé avec l'ID : " + managerServiceId));

        // Vérifier si l'entretien existe
        Entretien existingEntretien = entretienRepository.findById(entretienId)
                .orElseThrow(() -> new EntityNotFoundException("Entretien non trouvé avec l'ID : " + entretienId));
        Collaborateur ancienneCandidature = existingEntretien.getCollaborateurs();
        existingEntretien.setCollaborateurs(ancienneCandidature);

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

        // Envoyer des emails
        String collaborateurEmail = existingEntretien.getCollaborateurs().getCollaborateur().getEmail();
        String managerEmail = managerService.getManager().getEmail();
        String managerName = managerService.getManager().getNom();
        String collaborateurName = existingEntretien.getCollaborateurs().getCollaborateur().getNom();

        String subject = "Modification de l'entretien annuel";
        String contentCollaborateur = String.format(
                "Cher collaborateur %s,<br><br>L'entretien annuel prévu le %s de %s à %s a été modifié. <br>Le manager est %s.<br><br>Cordialement.",
                collaborateurName, dateEntretien, heureDebut, heureFin, managerName);
        String contentManager = String.format(
                "Cher manager,<br><br>L'entretien annuel pour le collaborateur %s a été modifié. <br>Le collaborateur a été informé.<br><br>Cordialement.",
                collaborateurName);

        emailService.sendEmail(collaborateurEmail, subject, contentCollaborateur);
        emailService.sendEmail(managerEmail, subject, contentManager);
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
