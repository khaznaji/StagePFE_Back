package com.example.backend.Controller;

import com.example.backend.Configuration.MailConfig;
import com.example.backend.Entity.*;
import com.example.backend.Repository.CandidatureRepository;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.ManagerServiceRepository;
import com.example.backend.Repository.PosteRepository;
import com.example.backend.Security.services.UserDetailsImpl;
import com.example.backend.Service.PosteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.backend.Entity.EtatPostulation.EN_ATTENTE;

@Controller
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/Poste")
public class PosteController {
    @Autowired
    private PosteRepository posteRepository;
    @Autowired
    private CollaborateurRepository collaborateurRepository;
    @Autowired
    private CandidatureRepository candidatureRepository;
    @Autowired
    private MailConfig emailService;
    @Autowired
    private ManagerServiceRepository managerServiceRepository;
    @PostMapping("/create")
    public ResponseEntity<?> createPoste(
            @RequestParam("titre") String titre,
            @RequestParam("description") String description,
            @RequestParam("nombrePostesDisponibles") int nombrePostesDisponibles,
            @RequestParam("competences") List<Competence> competences

    ){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long managerServiceId = userDetails.getId();
        ManagerService managerService = managerServiceRepository.findByManagerManagerId(managerServiceId)
                .orElseThrow(() -> new EntityNotFoundException("ManagerService non trouvé avec l'ID : " + managerServiceId));
        Poste poste = new Poste();
        poste.setManagerService(managerService);
        poste.setDateCreation(LocalDate.now()); // Date de création du poste
        poste.setDepartement(managerService.getDepartment()); // Utilisez le département du manager de service
        poste.setNombrePostesDisponibles(nombrePostesDisponibles);
        poste.setTitre(titre);
        poste.setDescription(description);
        poste.setApprouveParManagerRH(false);
        poste.setCompetences(competences);
        System.out.println(poste);
        posteRepository.save(poste);
        return new ResponseEntity<>(poste, HttpStatus.CREATED);
    }
    @GetMapping("/getAll")
    public ResponseEntity<List<Poste>> getAllPostes() {
        List<Poste> allPostes = posteRepository.findAll();

        System.out.println("All Postes:");
        for (Poste poste : allPostes) {
            System.out.println(poste);
        }

        return new ResponseEntity<>(allPostes, HttpStatus.OK);
    }
    @GetMapping("/getPosteById/{postId}")
    public ResponseEntity<Poste> getPosteById(@PathVariable Long postId) {
        Optional<Poste> optionalPoste = posteRepository.findById(postId);

        if (optionalPoste.isPresent()) {
            Poste poste = optionalPoste.get();
            System.out.println("Poste found by ID: " + poste);
            return new ResponseEntity<>(poste, HttpStatus.OK);
        } else {
            System.out.println("Poste not found with ID: " + postId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @PutMapping("/updateApproval/{postId}")
    public ResponseEntity<?> updateApproval(@PathVariable Long postId) {
        Optional<Poste> optionalPoste = posteRepository.findById(postId);

        if (optionalPoste.isPresent()) {
            Poste poste = optionalPoste.get();
            poste.setApprouveParManagerRH(true);
            poste.setEncours(false);

            poste.setArchive(false); // Update the field to true
            posteRepository.save(poste); // Save the updated entity
            String managerEmail = poste.getManagerService().getManager().getEmail(); // Assuming ManagerService has an email field
            String subject = "Notification d'Approbation de Poste";
            String text = "Cher(e) " + poste.getManagerService().getManager().getNom() + ",\n\n" +
                    "Nous avons le plaisir de vous informer que votre demande de poste a été approuvée par le Manager RH.\n\n" +
                    "Veuillez trouver ci-dessous les détails de votre poste :\n\n" +
                    "Titre du poste : " + poste.getTitre() + "\n" +
                    "Description : " + poste.getDescription() + "\n\n" +
                    "Nombre de postes disponibles : " + poste.getNombrePostesDisponibles() + "\n\n" +
                    "Cordialement,\n" +
                    "L'équipe 4You";
            emailService.sendEmail(managerEmail, subject, text);

            return new ResponseEntity<>(poste, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Poste not found with ID: " + postId, HttpStatus.NOT_FOUND);
        }
    }
    @PostMapping("/postuler/{postId}")
    public ResponseEntity<Map<String, String>> postulerAuPoste(@PathVariable Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId(); // ID de l'utilisateur

        // Utilisez la nouvelle méthode pour trouver le Collaborateur par l'ID de l'utilisateur
        Collaborateur collaborateur = collaborateurRepository.findByCollaborateurUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouvé avec l'ID : " + userId));
        Poste poste = posteRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Poste non trouvé avec l'ID : " + postId));

        // Vérifiez si le collaborateur a déjà postulé à ce poste
        if (candidatureRepository.existsByCollaborateurAndPoste(collaborateur, poste)) {
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("error", "Vous avez déjà postulé à ce poste");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
        }

        // Vérifiez si le collaborateur a au moins 3 compétences qui correspondent aux compétences requises pour le poste
        List<Competence> competencesCollaborateur = collaborateur.getCompetences();
        List<Competence> competencesPoste = poste.getCompetences();
        long competencesMatch = competencesCollaborateur.stream()
                .filter(competencesPoste::contains)
                .count();

        if (competencesMatch <= 1) {
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("error", "Vous devez avoir au moins une compétence qui correspondent aux compétences requises pour ce poste");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
        }

        // Créez une nouvelle instance de Candidature
        Candidature candidature = new Candidature();
        candidature.setCollaborateur(collaborateur);
        candidature.setPoste(poste);
        candidature.setEtat(EN_ATTENTE); // Définissez l'état initial de la candidature
        candidature.setDateCandidature(LocalDate.now()); // Définissez la date de candidature

        // Sauvegardez la nouvelle candidature dans la base de données
        candidatureRepository.save(candidature);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "Vous avez postulé avec succès à ce poste");
        return ResponseEntity.ok(responseBody);
    }


    @PutMapping("/updateRefus/{postId}")
    public ResponseEntity<?> updateRefus(@PathVariable Long postId) {
        Optional<Poste> optionalPoste = posteRepository.findById(postId);

        if (optionalPoste.isPresent()) {
            Poste poste = optionalPoste.get();
            poste.setApprouveParManagerRH(false);
            poste.setArchive(true);
            poste.setEncours(false);
            posteRepository.save(poste); // Save the updated entity
            String managerEmail = poste.getManagerService().getManager().getEmail(); // Assuming ManagerService has an email field
            String subject = "Notification de Refus de Poste";
            String text = "Cher(e) " + poste.getManagerService().getManager().getNom() + ",\n\n" +
                    "Nous sommes désolés de vous informer que votre demande de poste a été refusée par le Manager RH.\n\n" +
                    "Veuillez trouver ci-dessous les détails de votre demande :\n\n" +
                    "Titre du poste : " + poste.getTitre() + "\n" +
                    "Description : " + poste.getDescription() + "\n\n" +
                    "Nous vous remercions de votre intérêt et vous encourageons à soumettre à nouveau votre candidature si vous pensez que vous pourriez être un bon ajout à notre équipe.\n\n" +
                    "Cordialement,\n" +
                    "L'équipe 4You";
            emailService.sendEmail(managerEmail, subject, text);

            return new ResponseEntity<>(poste, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Poste not found with ID: " + postId, HttpStatus.NOT_FOUND);
        }
    }
    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<?> deletePoste(@PathVariable Long postId) {
        Optional<Poste> optionalPoste = posteRepository.findById(postId);

        if (optionalPoste.isPresent()) {
            Poste poste = optionalPoste.get();
            posteRepository.delete(poste);

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "Poste supprime");
            return ResponseEntity.ok(responseBody);
        } else {
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("error", "Erreur le poste n est pas supprime");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);        }
    }

   @DeleteMapping("/deletePosteByManagerService/{postId}")
   public ResponseEntity<?> deletePosteByManagerService(@PathVariable Long postId) {
       Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
       UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
       Long managerServiceId = userDetails.getId();
       Optional<Poste> optionalPoste = posteRepository.findByIdAndEncours(postId, true);
       if (optionalPoste.isPresent()) {
           Poste poste = optionalPoste.get();
           if (poste.getManagerService().getManager().getId().equals(managerServiceId)) {
               posteRepository.delete(poste);
               Map<String, String> responseBody = new HashMap<>();
               responseBody.put("message", "Poste supprimé");
               return ResponseEntity.ok(responseBody);
           } else {
               Map<String, String> responseBody = new HashMap<>();
               responseBody.put("error", "Vous n'avez pas la permission de supprimer ce poste");
               return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseBody);
           }
       } else {
           Map<String, String> responseBody = new HashMap<>();
           responseBody.put("error", "Le poste n'existe pas ou ne peut pas être supprimé");
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
       }
   }
    @PutMapping("/edit/{postId}")
    public ResponseEntity<?> editPoste(@PathVariable Long postId,
                                       @RequestParam(required = false) String titre,
                                       @RequestParam(required = false) String description,
                                       @RequestParam(required = false) Integer nombrePostesDisponibles,
                                       @RequestParam(required = false) List<Competence> competences) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long managerServiceId = userDetails.getId(); // ID du ManagerService connecté
        Optional<Poste> optionalPoste = posteRepository.findById(postId);
        if (optionalPoste.isPresent()) {
            Poste poste = optionalPoste.get();
            // Vérifiez si le ManagerService connecté est le propriétaire du poste
            if (poste.getManagerService().getManager().getId().equals(managerServiceId)) {
                // Mise à jour des détails du poste
                if (titre != null) {
                    poste.setTitre(titre);
                }
                if (description != null) {
                    poste.setDescription(description);
                }
                if (nombrePostesDisponibles != null) {
                    poste.setNombrePostesDisponibles(nombrePostesDisponibles);
                }
                if (competences != null) {
                    poste.setCompetences(competences);
                }
                posteRepository.save(poste); // Sauvegardez le poste mis à jour
                Map<String, String> responseBody = new HashMap<>();
                responseBody.put("message", "Poste mis à jour avec succès" + titre);
                return ResponseEntity.ok(responseBody);
            } else {
                Map<String, String> responseBody = new HashMap<>();
                responseBody.put("error", "Vous n'avez pas la permission de modifier ce poste");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseBody);
            }
        } else {
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("error", "Le poste n'existe pas");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBody);
        }
    }

    @GetMapping("/getApprovedAndNotAppliedPostes")
   public ResponseEntity<List<Poste>> getApprovedAndNotAppliedPostes() {
       Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
       UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
       Long userId = userDetails.getId(); // ID de l'utilisateur

       // Recherche du collaborateur correspondant à l'utilisateur
       Collaborateur collaborateur = collaborateurRepository.findByCollaborateurUserId(userId)
               .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouvé avec l'ID de l'utilisateur : " + userId));

       // Récupérer tous les postes approuvés
       List<Poste> approvedPostes = posteRepository.findByApprouveParManagerRHTrue();

       // Récupérer les postes auxquels le collaborateur a postulé
       List<Candidature> postulesParCollaborateur = collaborateur.getCandidatures();

       // Filtrer les postes approuvés pour exclure ceux auxquels le collaborateur a déjà postulé
       approvedPostes = approvedPostes.stream()
               .filter(poste -> postulesParCollaborateur.stream()
                       .noneMatch(candidature -> candidature.getPoste().getId().equals(poste.getId())))
               .collect(Collectors.toList());

       System.out.println("Postes approuvés non encore postulés par le collaborateur:");
       for (Poste poste : approvedPostes) {
           System.out.println(poste);
       }

       return new ResponseEntity<>(approvedPostes, HttpStatus.OK);
   }

    @GetMapping("/postulations")
    public ResponseEntity<List<Map<String, Object>>> getPostulations() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId(); // ID de l'utilisateur
        // Recherche du collaborateur correspondant à l'utilisateur
        Collaborateur collaborateur = collaborateurRepository.findByCollaborateurUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouvé avec l'ID de l'utilisateur : " + userId));
        // Récupérer la liste des candidatures du collaborateur
        List<Candidature> candidatures = collaborateur.getCandidatures();
        // Extraire les postes et les états de postulation auxquels le collaborateur a postulé à partir des candidatures
        List<Map<String, Object>> postulations = candidatures.stream()
                .map(candidature -> {
                    Map<String, Object> postulationDetails = new HashMap<>();
                    postulationDetails.put("poste", candidature.getPoste());
                    postulationDetails.put("etatPostulation", candidature.getEtat());
                    return postulationDetails;
                })
                .collect(Collectors.toList());
        System.out.println("Postulations du collaborateur:");
        for (Map<String, Object> postulation : postulations) {
            System.out.println(postulation.get("poste") + " - " + postulation.get("etatPostulation"));
        }
        return new ResponseEntity<>(postulations, HttpStatus.OK);
    }

    @GetMapping("/AllPostesByManagerService")
    public ResponseEntity<List<Poste>> getAllPostesByManagerService() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ManagerService managerService = userDetails.getUser().getManagerService();
        if (managerService == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }
        List<Poste> postes = posteRepository.findAllByManagerService(managerService);
        if (postes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }
        return ResponseEntity.ok(postes);
    }



}
