package com.example.backend.Controller;

import com.example.backend.Configuration.MailConfig;
import com.example.backend.Entity.*;
import com.example.backend.Repository.*;
import com.example.backend.Security.services.UserDetailsImpl;
import com.example.backend.Service.CandidatureService;
import com.example.backend.Service.EntretienService;
import com.example.backend.Service.ResumeMatcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityNotFoundException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private CandidatureService candidatureService;
    @Autowired
    private EntretienService entretienService;
    @Autowired
    private ManagerServiceRepository managerServiceRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private  NotificationRepository notificationRepository;
    @PostMapping("/create")
    public ResponseEntity<?> createPoste(
            @RequestParam("titre") String titre,
            @RequestParam("description") String description,
            @RequestParam("typeContrat") TypeContrat typeContrat,
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
        poste.setTypeContrat(typeContrat);
        poste.setTitre(titre);
        poste.setDescription(description);
        poste.setPoste(EtatPoste.En_cours);
        poste.setCompetences(competences);
        System.out.println(poste);
        posteRepository.save(poste);

        List<User> rhManagers = userRepository.findByRole(Role.ManagerRh);
        if (rhManagers.isEmpty()) {
            throw new EntityNotFoundException("RH Managers non trouvés");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationDateTime = now.plusDays(3);

        for (User rhManager : rhManagers) {
            Notification notification = new Notification();
            notification.setMessage("A créer un nouveau poste : " + titre);
            notification.setDateTime(now);
            notification.setExpirationDateTime(expirationDateTime);
            notification.setReceiver(rhManager);
            notification.setNotifType(NotifType.Poste);
            notification.setSender(userDetails.getUser()); // Assuming UserDetailsImpl has a method getUser() that returns the associated User entity
            notificationRepository.save(notification);
        }

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
           poste.setPoste(EtatPoste.Accepte);// Update the field to true
            posteRepository.save(poste); // Save the updated entity
            String managerEmail = poste.getManagerService().getManager().getEmail(); // Assuming ManagerService has an email field
            String subject = "Notification d'Approbation de Poste";
            String text = "Cher(e) " + poste.getManagerService().getManager().getNom() + ",\n\n" +
                    "Nous avons le plaisir de vous informer que votre demande de poste a été approuvée par le Manager RH.\n\n" +
                    "Veuillez trouver ci-dessous les détails de votre poste :\n\n" +
                    "Titre du poste : " + poste.getTitre() + "\n" +
                    "Description : " + poste.getDescription() + "\n\n" +
                    "Type de Contrat: " + poste.getTypeContrat() + "\n\n" +
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

    Collaborateur collaborateur = collaborateurRepository.findByCollaborateurUserId(userId)
            .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouvé avec l'ID : " + userId));

    if (!collaborateur.isVerified()) {
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("error", "Vous devez être vérifié pour postuler à un poste");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
    }

    Poste poste = posteRepository.findById(postId)
            .orElseThrow(() -> new EntityNotFoundException("Poste non trouvé avec l'ID : " + postId));

    if (candidatureRepository.existsByCollaborateurAndPoste(collaborateur, poste)) {
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("error", "Vous avez déjà postulé à ce poste");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }

    try {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:5000/match";
        Map<String, Long> requestBody = new HashMap<>();
        requestBody.put("collaborateur_id", collaborateur.getId());
        requestBody.put("job_id", postId);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("similarity_scores")) {
                List<Double> similarityScores = (List<Double>) responseBody.get("similarity_scores");

                // Enregistrer les scores de similarité dans la base de données
                for (Double score : similarityScores) {
                    int matchPercentageInt = (int) (score * 100); // Convertir en pourcentage

                    Candidature candidature = new Candidature();
                    candidature.setCollaborateur(collaborateur);
                    candidature.setPoste(poste);
                    candidature.setEtat(EN_ATTENTE);
                    candidature.setDateCandidature(LocalDate.now());
                    candidature.setMatchPercentage(matchPercentageInt); // Enregistrer le pourcentage de correspondance

                    candidatureRepository.save(candidature);
                }

                Map<String, String> successResponse = new HashMap<>();
                successResponse.put("message", "Vous avez postulé avec succès à ce poste");
                return ResponseEntity.ok(successResponse);
            } else {
                // Gérer le cas où la réponse ne contient pas la clé "similarity_scores"
            }
        } else {
            // Gérer le cas où la réponse n'est pas OK
        }
    } catch (RestClientException e) {
        // Gérer l'erreur d'appel à l'API Flask
    }


    // Gérer les autres erreurs si nécessaire
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
}
    @PutMapping("/updateRefus/{postId}")
    public ResponseEntity<?> updateRefus(@PathVariable Long postId) {
        Optional<Poste> optionalPoste = posteRepository.findById(postId);
        if (optionalPoste.isPresent()) {
            Poste poste = optionalPoste.get();
           poste.setPoste(EtatPoste.Rejete);
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
       Optional<Poste> optionalPoste = posteRepository.findById(postId);
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
                                       @RequestParam(required = false) TypeContrat typeContrat) {
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
                if (typeContrat != null) {
                    poste.setTypeContrat(typeContrat);
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
    @PutMapping("/editPosteComptence/{postId}")
    public ResponseEntity<?> editPosteComptence(@PathVariable Long postId, @RequestParam(required = false) List<Competence> competences) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long managerServiceId = userDetails.getId(); // ID du ManagerService connecté
        Optional<Poste> optionalPoste = posteRepository.findById(postId);
        if (optionalPoste.isPresent()) {
            Poste poste = optionalPoste.get();
            // Vérifiez si le ManagerService connecté est le propriétaire du poste
            if (poste.getManagerService().getManager().getId().equals(managerServiceId)) {
                // Mise à jour des détails du post
                if (competences != null) {
                    poste.setCompetences(competences);
                }
                posteRepository.save(poste); // Sauvegardez le poste mis à jour
                Map<String, String> responseBody = new HashMap<>();
                responseBody.put("message", "Competence mis à jour avec succès" );
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

        // Récupérer les IDs des postes auxquels le collaborateur a postulé
        Set<Long> appliedPosteIds = collaborateur.getCandidatures().stream()
                .map(candidature -> candidature.getPoste().getId())
                .collect(Collectors.toSet());

        // Récupérer tous les postes approuvés
        List<Poste> approvedPostes = posteRepository.findByPoste(EtatPoste.Publie);

        // Filtrer les postes pour ne garder que ceux auxquels le collaborateur n'a pas postulé
        List<Poste> postesNonPostules = approvedPostes.stream()
                .filter(poste -> !appliedPosteIds.contains(poste.getId()))
                .collect(Collectors.toList());

        return new ResponseEntity<>(postesNonPostules, HttpStatus.OK);
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
    @PutMapping("/updateEtatQuizz/{candidatureId}")
    public ResponseEntity <Map<String, String>> updateEtatQuizz(@PathVariable Long candidatureId) {
        Optional<Candidature> optionalCandidature = candidatureRepository.findById(candidatureId);
        if (optionalCandidature.isPresent()) {
            Candidature candidature = optionalCandidature.get();
            candidature.setEtatQuizz(EtatQuizz.Termine); // Mettre à jour l'état du quiz à "Termine"
            candidatureRepository.save(candidature);
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "L'état du quiz a été mis à jour avec succès pour la candidature avec l'ID  : " );
            return ResponseEntity.ok(responseBody);// Sauvegarder la candidature mise à jour
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/testsForConnectedCollaborator")
    public ResponseEntity<?> getTestsForConnectedCollaborator() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId(); // ID de l'utilisateur

        // Recherche du collaborateur correspondant à l'utilisateur
        Collaborateur collaborateur = collaborateurRepository.findByCollaborateurUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouvé avec l'ID de l'utilisateur : " + userId));

        // Récupérer la liste des candidatures du collaborateur
        List<Candidature> candidatures = collaborateur.getCandidatures();

        if (!candidatures.isEmpty()) {
            List<Quiz> tests = new ArrayList<>();

            for (Candidature candidature : candidatures) {
                if (candidature.getEtatQuizz() == EtatQuizz.En_Attente) {
                    Quiz test = candidature.getQuiz();
                    if (test != null) {
                        test.setCandidatureId(candidature.getId());
                        test.getCandidatures().add(candidature);
                        tests.add(test);
                    }
                }
            }

            return ResponseEntity.ok(tests);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aucune candidature trouvée pour le collaborateur");
        }
    }


    @GetMapping("/AllPostesApprouveByManagerService")
    public ResponseEntity<List<Poste>> AllPostesApprouveByManagerService() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ManagerService managerService = userDetails.getUser().getManagerService();

        if (managerService == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }

        List<Poste> postes = posteRepository.findAllByManagerService(managerService);
        List<Poste> filteredPostes = postes.stream()
                .filter(poste -> poste.getPoste() == EtatPoste.Accepte)
                .collect(Collectors.toList());

        if (filteredPostes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }

        return ResponseEntity.ok(filteredPostes);
    }



    @GetMapping("/PostesEnCoursRefuseByManagerService")
    public ResponseEntity<List<Poste>> PostesEnCoursRefuseByManagerService() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ManagerService managerService = userDetails.getUser().getManagerService();

        if (managerService == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }

        List<Poste> postes = posteRepository.findAllByManagerService(managerService);
        List<Poste> filteredPostes = postes.stream()
                .filter(poste -> poste.getPoste() == EtatPoste.En_cours || poste.getPoste() == EtatPoste.Rejete)
                .collect(Collectors.toList());

        if (filteredPostes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }

        return ResponseEntity.ok(filteredPostes);
    }

    @DeleteMapping("/deleteCompetence/{postId}/{competenceId}")
    public ResponseEntity<?> deleteCompetenceFromPoste(@PathVariable Long postId, @PathVariable Long competenceId) {
        Optional<Poste> optionalPoste = posteRepository.findById(postId);

        if (optionalPoste.isPresent()) {
            Poste poste = optionalPoste.get();

            // Supprimer la compétence spécifique de la liste des compétences du poste
            List<Competence> competences = poste.getCompetences();
            competences.removeIf(competence -> competence.getId().equals(competenceId));
            poste.setCompetences(competences);

            posteRepository.save(poste); // Enregistrer les modifications

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "Compétence supprimée avec succès du poste avec l'ID : " + postId);
            return ResponseEntity.ok(responseBody);
        } else {
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("error", "Le poste n'existe pas avec l'ID : " + postId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBody);
        }
    }
    @GetMapping("/getCandidatsByPosteId/{postId}")
    public ResponseEntity<List<Map<String, String>>> getCandidatsByPosteId(@PathVariable Long postId) {
        Optional<Poste> optionalPoste = posteRepository.findById(postId);

        if (optionalPoste.isPresent()) {
            Poste poste = optionalPoste.get();

            // Récupérer toutes les candidatures associées à ce poste
            List<Candidature> candidatures = candidatureRepository.findByPoste(poste);

            // Extraire les informations des collaborateurs (nom, prénom, email) de chaque candidature
            List<Map<String, String>> candidatsInfo = new ArrayList<>();
            for (Candidature candidature : candidatures) {
                Collaborateur collaborateur = candidature.getCollaborateur();
                User user = collaborateur.getCollaborateur();

                Map<String, String> candidatInfo = new HashMap<>();
                candidatInfo.put("id", collaborateur.getId().toString()); // Ajouter l'ID du candidat
                candidatInfo.put("etat", candidature.getEtat().toString());
                candidatInfo.put("match", Integer.toString(candidature.getMatchPercentage()));

                candidatInfo.put("nom", user.getNom());
                candidatInfo.put("prenom", user.getPrenom());
                candidatInfo.put("email", user.getEmail());

                candidatsInfo.add(candidatInfo);
            }

            return new ResponseEntity<>(candidatsInfo, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/getCandidatsByPosteIdEnAttenteEntretien/{postId}")
    public ResponseEntity<List<Map<String, String>>> getCandidatsByPosteIdEnAttenteEntretien(@PathVariable Long postId) {
        Optional<Poste> optionalPoste = posteRepository.findById(postId);

        if (optionalPoste.isPresent()) {
            Poste poste = optionalPoste.get();

            // Récupérer toutes les candidatures associées à ce poste
            List<Candidature> candidatures = candidatureRepository.findByPoste(poste);

            // Extraire les informations des collaborateurs (nom, prénom, email) de chaque candidature
            List<Map<String, String>> candidatsInfo = new ArrayList<>();
            for (Candidature candidature : candidatures) {
                if (candidature.getEtat().equals(EtatPostulation.EN_ATTENTE_ENTRETIEN)) { // Filtrer par l'état de la candidature
                    Collaborateur collaborateur = candidature.getCollaborateur();
                    User user = collaborateur.getCollaborateur();

                    Map<String, String> candidatInfo = new HashMap<>();
                    candidatInfo.put("id", candidature.getId().toString()); // Ajouter l'ID du candidat
                    candidatInfo.put("etat", candidature.getEtat().toString());

                    candidatInfo.put("nom", user.getNom());
                    candidatInfo.put("prenom", user.getPrenom());
                    candidatInfo.put("email", user.getEmail());

                    candidatsInfo.add(candidatInfo);
                }
            }

            return new ResponseEntity<>(candidatsInfo, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/getCandidatsByPosteIdEnAttenteEntretienRh/{postId}")
    public ResponseEntity<List<Map<String, String>>> getCandidatsByPosteIdEnAttenteEntretienRh(@PathVariable Long postId) {
        Optional<Poste> optionalPoste = posteRepository.findById(postId);

        if (optionalPoste.isPresent()) {
            Poste poste = optionalPoste.get();

            // Récupérer toutes les candidatures associées à ce poste
            List<Candidature> candidatures = candidatureRepository.findByPoste(poste);

            // Extraire les informations des collaborateurs (nom, prénom, email) de chaque candidature
            List<Map<String, String>> candidatsInfo = new ArrayList<>();
            for (Candidature candidature : candidatures) {
                if (candidature.getEtat().equals(EtatPostulation.EN_ATTENTE_ENTRETIEN_RH)) { // Filtrer par l'état de la candidature
                    Collaborateur collaborateur = candidature.getCollaborateur();
                    User user = collaborateur.getCollaborateur();

                    Map<String, String> candidatInfo = new HashMap<>();
                    candidatInfo.put("id", candidature.getId().toString()); // Ajouter l'ID du candidat
                    candidatInfo.put("etat", candidature.getEtat().toString());

                    candidatInfo.put("nom", user.getNom());
                    candidatInfo.put("prenom", user.getPrenom());
                    candidatInfo.put("email", user.getEmail());

                    candidatsInfo.add(candidatInfo);
                }
            }

            return new ResponseEntity<>(candidatsInfo, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/demandesEnCours")
    public ResponseEntity<List<Poste>> getDemandesEnCours() {
        List<Poste> demandesEnCours = posteRepository.findByPoste(EtatPoste.En_cours);
        return new ResponseEntity<>(demandesEnCours, HttpStatus.OK);
    }
    @PutMapping("/publieposte/{postId}")
    public ResponseEntity<?> PubliePoste(@PathVariable Long postId) {
        Optional<Poste> optionalPoste = posteRepository.findById(postId);

        if (optionalPoste.isPresent()) {
            Poste poste = optionalPoste.get();

            // Vérifiez si des tests associés à ce poste sont actifs
            boolean testsActifs = poste.getQuizzes().stream().anyMatch(Quiz::isActive);

            // Si aucun test n'est actif, publiez le poste
            if (testsActifs) {
                poste.setPoste(EtatPoste.Publie); // Mettez à jour l'état du poste à Publie
                posteRepository.save(poste); // Sauvegardez l'entité mise à jour

                // Votre code pour envoyer un e-mail ou effectuer d'autres actions après l'approbation du poste

                return new ResponseEntity<>(poste, HttpStatus.OK);
            } else {
                // Sinon, renvoyez un message d'erreur
                return new ResponseEntity<>("Impossible de publier le poste car il y a un ou plusieurs test qui ne contiennent pas de questions.", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("Poste not found with ID: " + postId, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/archivePoste/{postId}")
    public ResponseEntity<?> ArchivePoste(@PathVariable Long postId) {
        Optional<Poste> optionalPoste = posteRepository.findById(postId);

        if (optionalPoste.isPresent()) {
            Poste poste = optionalPoste.get();
            poste.setPoste(EtatPoste.Archive); // Mettez à jour l'état du poste à Publie
            posteRepository.save(poste); // Sauvegardez l'entité mise à jour

            // Votre code pour envoyer un e-mail ou effectuer d'autres actions après l'approbation du poste

            return new ResponseEntity<>(poste, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Poste not found with ID: " + postId, HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/getPostePublieCoteManagerRh")
    public ResponseEntity<List<Poste>> getPostePublieCoteManagerRh() {

        List<Poste> demandesEnCours = posteRepository.findByPoste(EtatPoste.Publie);
        return new ResponseEntity<>(demandesEnCours, HttpStatus.OK);
    }
    @GetMapping("/postepublie")
    public ResponseEntity<List<Poste>> getPostePublie() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ManagerService managerService = userDetails.getUser().getManagerService();

        if (managerService == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }

        List<Poste> postes = posteRepository.findAllByManagerService(managerService);
        List<Poste> filteredPostes = postes.stream()
                .filter(poste -> poste.getPoste() == EtatPoste.Publie)
                .collect(Collectors.toList());

        if (filteredPostes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }

        return ResponseEntity.ok(filteredPostes);
    }

    @GetMapping("/posteArchive")
    public ResponseEntity<List<Poste>> getPosteArchive() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ManagerService managerService = userDetails.getUser().getManagerService();

        if (managerService == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }

        List<Poste> postes = posteRepository.findAllByManagerService(managerService);
        List<Poste> filteredPostes = postes.stream()
                .filter(poste -> poste.getPoste() == EtatPoste.Archive)
                .collect(Collectors.toList());

        if (filteredPostes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }

        return ResponseEntity.ok(filteredPostes);
    }










  /***** Candidature Controller********/

  @PutMapping("/updateState/{candidatureId}")
  public ResponseEntity<?> updateCandidatureState(@PathVariable Long candidatureId, @RequestParam String newState) {
      Optional<Candidature> optionalCandidature = candidatureRepository.findById(candidatureId);
      if (optionalCandidature.isPresent()) {
          Candidature candidature = optionalCandidature.get();
          if (newState.equals("Preselection")) {
              Poste poste = candidature.getPoste();
              if (poste != null) {
                  List<Quiz> quizzes = poste.getQuizzes();
                  if (!quizzes.isEmpty()) {
                      Random random = new Random();
                      int randomIndex = random.nextInt(quizzes.size());
                      Quiz randomQuiz = quizzes.get(randomIndex);
                      candidature.setQuiz(randomQuiz);
                      candidature.setEtatQuizz(EtatQuizz.En_Attente); // Mettre à jour l'état du quiz à "En_Attente"
                  }
              }
          }
          candidature.setEtat(EtatPostulation.valueOf(newState));
          candidatureRepository.save(candidature); // Sauvegarder la candidature mise à jour
          return ResponseEntity.ok(candidature);
      } else {
          return ResponseEntity.notFound().build();
      }
  }

    @PutMapping("/updateStateRefus/{candidatureId}")
    public ResponseEntity<?> updateCandidatureStateRefus(@PathVariable Long candidatureId) {
        Optional<Candidature> optionalCandidature = candidatureRepository.findById(candidatureId);
        if (optionalCandidature.isPresent()) {
            Candidature candidature = optionalCandidature.get();
            candidature.setEtat(EtatPostulation.REFUSEE);
            candidatureRepository.save(candidature); // Sauvegarder la candidature mise à jour
            return ResponseEntity.ok(candidature);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @PutMapping("/updateStateEntretien/{candidatureId}")
    public ResponseEntity<?> updateCandidatureStateEntretien(@PathVariable Long candidatureId) {
        Optional<Candidature> optionalCandidature = candidatureRepository.findById(candidatureId);
        if (optionalCandidature.isPresent()) {
            Candidature candidature = optionalCandidature.get();
            candidature.setEtat(EtatPostulation.EN_ATTENTE_ENTRETIEN);
            candidatureRepository.save(candidature);
            return ResponseEntity.ok(candidature);
        } else {
            return ResponseEntity.notFound().build();
        }
    }



    @GetMapping("/AllCandidature/{postId}")
    public ResponseEntity<List<Map<String, String>>> getCandidaturesByPost(@PathVariable Long postId) {
        List<Candidature> candidatures = candidatureService.getCandidaturesByPost(postId);
        List<Map<String, String>> candidatsInfo = new ArrayList<>();

        for (Candidature candidature : candidatures) {
            Collaborateur collaborateur = candidature.getCollaborateur();
            User user = collaborateur.getCollaborateur();

            Map<String, String> candidatInfo = new HashMap<>();
            candidatInfo.put("candidature_id", candidature.getId().toString()); // ID de la candidature
            candidatInfo.put("etat", candidature.getEtat().toString());
            candidatInfo.put("match", Integer.toString(candidature.getMatchPercentage()));


            candidatInfo.put("nom", user.getNom());
            candidatInfo.put("image", user.getImage());
            candidatInfo.put("score", String.valueOf(candidature.getScore())); // Convert int to String

            candidatInfo.put("prenom", user.getPrenom());
            candidatInfo.put("email", user.getEmail());
            candidatInfo.put("collaborateur_id", collaborateur.getId().toString()); // ID du collaborateur
            candidatInfo.put("poste_id", candidature.getPoste().getId().toString()); // ID du poste

            candidatsInfo.add(candidatInfo);
        }

        return new ResponseEntity<>(candidatsInfo, HttpStatus.OK);
    }
    @GetMapping("/CandidaturesSpecifiques/{postId}")
    public ResponseEntity<List<Map<String, String>>> getCandidaturesSpecifiques(@PathVariable Long postId) {
        List<Candidature> candidatures = candidatureService.getCandidaturesByPost(postId);
        List<Map<String, String>> candidatsInfo = new ArrayList<>();

        for (Candidature candidature : candidatures) {
            EtatQuizz etatQuizz = candidature.getEtatQuizz();

            if (etatQuizz == EtatQuizz.Termine) {
                Collaborateur collaborateur = candidature.getCollaborateur();
                User user = collaborateur.getCollaborateur();
                Map<String, String> candidatInfo = new HashMap<>();
                candidatInfo.put("candidature_id", candidature.getId().toString()); // ID de la candidature
                candidatInfo.put("etat", candidature.getEtat().toString());
                candidatInfo.put("nom", user.getNom());
                candidatInfo.put("image", user.getImage());
                candidatInfo.put("score", String.valueOf(candidature.getScore())); // Convert int to String
                candidatInfo.put("prenom", user.getPrenom());
                candidatInfo.put("email", user.getEmail());
                candidatInfo.put("collaborateur_id", collaborateur.getId().toString()); // ID du collaborateur
                candidatInfo.put("poste_id", candidature.getPoste().getId().toString()); // ID du poste

                candidatsInfo.add(candidatInfo);
            }
        }

        return new ResponseEntity<>(candidatsInfo, HttpStatus.OK);
    }

    @PostMapping("/updateEtatToEnAttente")
    public ResponseEntity<?> updateCandidaturesEtatToEnAttente(@RequestBody List<Long> candidatureIds) {
        candidatureService.updateCandidaturesToEnAttente(candidatureIds);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/CandidatsVersEntretienRh/{postId}")
    public ResponseEntity<List<Map<String, String>>> CandidatsVersEntretienRh(@PathVariable Long postId) {
        List<Candidature> candidatures = candidatureService.getCandidaturesByPost(postId);
        List<Map<String, String>> candidatsInfo = new ArrayList<>();

        for (Candidature candidature : candidatures) {
            EtatPostulation etat = candidature.getEtat();

            // Vérifier si l'état de la candidature est "Entretien"
            if (etat == EtatPostulation.Entretien) {
                Collaborateur collaborateur = candidature.getCollaborateur();
                User user = collaborateur.getCollaborateur();

                // Récupérer les informations d'entretien associées à la candidature
                Optional<Entretien> entretienOptional = entretienService.getEntretienByCandidatureId(candidature.getId());

                // Vérifier si un entretien est associé à la candidature
                if (entretienOptional.isPresent()) {
                    Entretien entretien = entretienOptional.get();

                    // Créer une nouvelle carte d'informations de candidat
                    Map<String, String> candidatInfo = new HashMap<>();
                    candidatInfo.put("candidature_id", candidature.getId().toString()); // ID de la candidature
                    candidatInfo.put("etat", etat.toString());
                    candidatInfo.put("nom", user.getNom());
                    candidatInfo.put("image", user.getImage());
                    candidatInfo.put("score", String.valueOf(candidature.getScore())); // Convert int to String
                    candidatInfo.put("prenom", user.getPrenom());
                    candidatInfo.put("email", user.getEmail());
                    candidatInfo.put("collaborateur_id", collaborateur.getId().toString()); // ID du collaborateur
                    candidatInfo.put("poste_id", candidature.getPoste().getId().toString()); // ID du poste
                    candidatInfo.put("note", String.valueOf(entretien.getNote())); // Convertir int en String

                    // Ajouter les informations du candidat à la liste
                    candidatsInfo.add(candidatInfo);
                }
            }
        }

        return new ResponseEntity<>(candidatsInfo, HttpStatus.OK);
    }

    @GetMapping("/CandidatsEntretienRh/{postId}")
    public ResponseEntity<List<Map<String, String>>> CandidatsEntretienRh(@PathVariable Long postId) {
        List<Candidature> candidatures = candidatureService.getCandidaturesByPost(postId);
        List<Map<String, String>> candidatsInfo = new ArrayList<>();
        for (Candidature candidature : candidatures) {
            EtatPostulation etat = candidature.getEtat();
            if (etat == EtatPostulation.Entretien_Rh) {
                Collaborateur collaborateur = candidature.getCollaborateur();
                User user = collaborateur.getCollaborateur();
                // Récupérer les informations d'entretien associées à la candidature
                Optional<Entretien> entretienOptional = entretienService.getEntretienByCandidatureId(candidature.getId());
                if (entretienOptional.isPresent()) {
                    Entretien entretien = entretienOptional.get();
                    Map<String, String> candidatInfo = new HashMap<>();
                    candidatInfo.put("candidature_id", candidature.getId().toString()); // ID de la candidature
                    candidatInfo.put("etat", etat.toString());
                    candidatInfo.put("nom", user.getNom());
                    candidatInfo.put("image", user.getImage());
                    candidatInfo.put("score", String.valueOf(candidature.getScore())); // Convert int to String
                    candidatInfo.put("prenom", user.getPrenom());
                    candidatInfo.put("email", user.getEmail());
                    candidatInfo.put("collaborateur_id", collaborateur.getId().toString()); // ID du collaborateur
                    candidatInfo.put("poste_id", candidature.getPoste().getId().toString()); // ID du poste
                    candidatInfo.put("note", String.valueOf(entretien.getNote())); // Convertir int en String
                    candidatsInfo.add(candidatInfo);
                }
            }
        }
        return new ResponseEntity<>(candidatsInfo, HttpStatus.OK);
    }

    @PutMapping("/modifierEtat/{collaborateurId}/{posteId}/{newState}")
    public Candidature modifierEtat(
            @PathVariable Long collaborateurId,
            @PathVariable Long posteId,
            @PathVariable EtatPostulation newState) {
        return candidatureService.modifierEtat(collaborateurId, posteId, newState);
    }
    @GetMapping("/collaborateur/info/{id}")
    public Map<String, Object> getCollaborateurInfoById(@PathVariable Long id) {
        // Récupérez le Collaborateur par son ID
        Collaborateur collaborateur = collaborateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collaborateur not found"));

        // Construisez le map pour contenir les informations
        Map<String, Object> collaborateurInfo = new HashMap<>();

        collaborateurInfo.put("nom", collaborateur.getCollaborateur().getNom());
        collaborateurInfo.put("prenom", collaborateur.getCollaborateur().getPrenom());
        collaborateurInfo.put("poste", collaborateur.getPoste());
        collaborateurInfo.put("dateEntree", collaborateur.getDateEntree());
        collaborateurInfo.put("departement", collaborateur.getDepartment().name()); // Assurez-vous que Departement est un enum
        collaborateurInfo.put("bio", collaborateur.getBio());
        collaborateurInfo.put("image", collaborateur.getCollaborateur().getImage());
        collaborateurInfo.put("resume", collaborateur.getResume());

        // Récupérez les évaluations des compétences
        List<Map<String, Object>> evaluations = collaborateur.getEvaluations().stream()
                .map(evaluation -> {
                    Map<String, Object> evaluationInfo = new HashMap<>();
                    evaluationInfo.put("competenceName", evaluation.getCompetence().getNom());
                    evaluationInfo.put("evaluation", evaluation.getEvaluation());
                    evaluationInfo.put("domaine", evaluation.getCompetence().getDomaine());
                    return evaluationInfo;
                })
                .collect(Collectors.toList());

        collaborateurInfo.put("evaluations", evaluations);

        return collaborateurInfo;
    }
    @Autowired
    private FormationRepository formationRepository ;
    @GetMapping("/userscertif/info/{id}")
    public Map<String, Object> getUsersCertifInfoById(@PathVariable Long id) {
        Collaborateur user = collaborateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("nom", user.getCollaborateur().getNom());
        userInfo.put("prenom", user.getCollaborateur().getPrenom());
        userInfo.put("mail", user.getCollaborateur().getEmail());
        userInfo.put("num", user.getCollaborateur().getNumtel());


            userInfo.put("isVerified", user.isVerified());
            userInfo.put("id", user.getId());

            userInfo.put("poste", user.getPoste());
            userInfo.put("dateEntree", user.getDateEntree());
            userInfo.put("departement", user.getDepartment().name());
            userInfo.put("bio", user.getBio());
            userInfo.put("image", user.getCollaborateur().getImage());
            userInfo.put("resume", user.getResume());
            // Récupérez les évaluations des compétences
            List<Map<String, Object>> evaluations = user.getEvaluations().stream()
                    .map(evaluation -> {
                        Map<String, Object> evaluationInfo = new HashMap<>();
                        evaluationInfo.put("competenceName", evaluation.getCompetence().getNom());
                        evaluationInfo.put("evaluation", evaluation.getEvaluation());
                        evaluationInfo.put("domaine", evaluation.getCompetence().getDomaine());

                        return evaluationInfo;
                    })
                    .collect(Collectors.toList());

            userInfo.put("evaluations", evaluations);
            ManagerService managerService = user.getManagerService();
            if (managerService != null) {
                userInfo.put("managerName", managerService.getManager().getNom());
                userInfo.put("managerPrenom", managerService.getManager().getPrenom());
            }
            // Ajoutez d'autres informations spécifiques aux collaborateurs si nécessaire


        return userInfo;
    }
    @GetMapping("/users/info/{id}")
    public Map<String, Object> getUsersInfoById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("nom", user.getNom());
        userInfo.put("prenom", user.getPrenom());
        userInfo.put("role", user.getRole());

        if (user.getRole() == Role.Collaborateur) {
            Collaborateur collaborateur = user.getCollaborateur();
            userInfo.put("isVerified", collaborateur.isVerified());

            userInfo.put("poste", collaborateur.getPoste());
            userInfo.put("dateEntree", collaborateur.getDateEntree());
            userInfo.put("departement", collaborateur.getDepartment().name());
            userInfo.put("bio", collaborateur.getBio());
            userInfo.put("image", collaborateur.getCollaborateur().getImage());
            userInfo.put("resume", collaborateur.getResume());
            // Récupérez les évaluations des compétences
            List<Map<String, Object>> evaluations = collaborateur.getEvaluations().stream()
                    .map(evaluation -> {
                        Map<String, Object> evaluationInfo = new HashMap<>();
                        evaluationInfo.put("competenceName", evaluation.getCompetence().getNom());
                        evaluationInfo.put("evaluation", evaluation.getEvaluation());
                        evaluationInfo.put("domaine", evaluation.getCompetence().getDomaine());

                        return evaluationInfo;
                    })
                    .collect(Collectors.toList());

            userInfo.put("evaluations", evaluations);
            ManagerService managerService = collaborateur.getManagerService();
            if (managerService != null) {
                userInfo.put("managerName", managerService.getManager().getNom());
                userInfo.put("managerPrenom", managerService.getManager().getPrenom());
            }
            // Ajoutez d'autres informations spécifiques aux collaborateurs si nécessaire
        } else if (user.getRole() == Role.ManagerService) {
            ManagerService managerService = user.getManagerService();
            userInfo.put("dateEntree", managerService.getDateEntree());
            userInfo.put("departement", managerService.getDepartment().name());
            userInfo.put("image", managerService.getManager().getImage());
            userInfo.put("poste", managerService.getPoste());

            userInfo.put("equipe", managerService.getCollaborateurs().stream()
                    .map(collaborateur -> {
                        Map<String, Object> collaborateurMap = new HashMap<>();
                        collaborateurMap.put("nom", collaborateur.getCollaborateur().getNom() + " " + collaborateur.getCollaborateur().getPrenom());
                        collaborateurMap.put("id", collaborateur.getId());
                        collaborateurMap.put("image", collaborateur.getCollaborateur().getImage());

                        return collaborateurMap;
                    })
                    .collect(Collectors.toList()));

            userInfo.put("postesCrees", managerService.getPostes().stream()
                    .filter(poste -> EtatPoste.Publie.equals(poste.getPoste())) // Filtre les postes avec l'état "publié"
                    .map(poste -> {
                        Map<String, Object> posteMap = new HashMap<>();
                        posteMap.put("titre", poste.getTitre());
                        posteMap.put("description", poste.getDescription());
                        posteMap.put("typeContrat", poste.getTypeContrat());
                        posteMap.put("nombreCandidats", poste.getCandidatures().size());

                        posteMap.put("id", poste.getId());
                        return posteMap;
                    })
                    .collect(Collectors.toList()));

            userInfo.put("nombrePostesCrees", managerService.getPostes().stream()
                    .filter(poste -> poste.getPoste() == EtatPoste.Publie) // Filtrer les postes avec un état "publié"
                    .count());

            // Ajoutez d'autres informations spécifiques aux managers de service si nécessaire
        } else if (user.getRole() == Role.Formateur) {
            Formateur formateur = user.getFormateur();
            userInfo.put("dateEntree", formateur.getDateEntree());
            userInfo.put("departement", formateur.getSpecialite());
            userInfo.put("image", formateur.getFormateur().getImage());
            userInfo.put("formationCrees", formateur.getFormation().stream()
                    .map(poste -> {
                        Map<String, Object> posteMap = new HashMap<>();
                        posteMap.put("id", poste.getId());
                        posteMap.put("titre", poste.getTitle());
                        posteMap.put("imagePoste", poste.getImage());
                        posteMap.put("chapitre", poste.getChapitre());
                        posteMap.put("duree", poste.getDuree());
                        posteMap.put("departement", poste.getDepartment());

                        return posteMap;
                    })
                    .collect(Collectors.toList()));
            userInfo.put("nombreformationCrees", formateur.getFormation().size());

            // Ajoutez d'autres informations spécifiques aux formateurs si nécessaire
        }

        return userInfo;
    }

    @GetMapping("/countCollaborateursEnAttente/{postId}")
    public ResponseEntity<Long> countCollaborateursEnAttente(@PathVariable Long postId) {
        Optional<Poste> optionalPoste = posteRepository.findById(postId);
        if (optionalPoste.isPresent()) {
            Poste poste = optionalPoste.get();
            long enAttenteCount = candidatureRepository.countByPosteAndEtat(poste, EtatPostulation.EN_ATTENTE);
            long preselectionCount = candidatureRepository.countByPosteAndEtat(poste, EtatPostulation.Preselection);
            long attenteentretienCount = candidatureRepository.countByPosteAndEtat(poste, EtatPostulation.EN_ATTENTE_ENTRETIEN);
            long entretienCount = candidatureRepository.countByPosteAndEtat(poste, EtatPostulation.Entretien);
            long entretienRhCount = candidatureRepository.countByPosteAndEtat(poste, EtatPostulation.EN_ATTENTE_ENTRETIEN_RH);
            long enAttenteRhCount = candidatureRepository.countByPosteAndEtat(poste, EtatPostulation.Entretien_Rh);

            long totalCount = enAttenteCount + preselectionCount + entretienCount + attenteentretienCount +entretienRhCount +enAttenteRhCount;
            return ResponseEntity.ok(totalCount);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/countCollaborateursAcceptees/{postId}")
    public ResponseEntity<Long> countCollaborateursAcceptees(@PathVariable Long postId) {
        Optional<Poste> optionalPoste = posteRepository.findById(postId);
        if (optionalPoste.isPresent()) {
            Poste poste = optionalPoste.get();
            Long count = candidatureRepository.countByPosteAndEtat(poste, EtatPostulation.ACCEPTEE);
            return ResponseEntity.ok(count);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/countCollaborateursRefusees/{postId}")
    public ResponseEntity<Long> countCollaborateursRefusees(@PathVariable Long postId) {
        Optional<Poste> optionalPoste = posteRepository.findById(postId);
        if (optionalPoste.isPresent()) {
            Poste poste = optionalPoste.get();
            Long count = candidatureRepository.countByPosteAndEtat(poste, EtatPostulation.REFUSEE);
            return ResponseEntity.ok(count);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/getPosteByIdSimple/{postId}")
    public ResponseEntity<Poste> getPosteByIdSimple(@PathVariable Long postId) {
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
    @PutMapping("/{candidatureId}/score")
    public ResponseEntity<?> soumettreScoreQuiz(@PathVariable Long candidatureId, @RequestParam int score) {
        Optional<Candidature> optionalCandidature = candidatureRepository.findById(candidatureId);
        if (optionalCandidature.isPresent()) {
            Candidature candidature = optionalCandidature.get();

            // Déterminer le seuil requis pour passer à l'état Entretien
            Quiz quiz = candidature.getQuiz();
            int maxMarks = Integer.parseInt(quiz.getMaxMarks());
            double passingThreshold = 0.5 * maxMarks;

            // Mettre à jour le score de la candidature
            candidature.setScore(score);

            // Vérifier si le score est supérieur ou égal au seuil pour passer à l'état Entretien
            if (score >= passingThreshold) {
                candidature.setEtat(EtatPostulation.EN_ATTENTE_ENTRETIEN);
            } else {
                candidature.setEtat(EtatPostulation.REFUSEE);
                // Envoyer un e-mail de refus au candidat
                String candidatEmail = candidature.getCollaborateur().getCollaborateur().getEmail();
                String subject = "Réponse à votre candidature pour le poste " + candidature.getPoste().getTitre();
                String body = "Cher/Chère " + candidature.getCollaborateur().getCollaborateur().getNom() + ",\n\n" +
                        "Nous tenons à vous remercier pour l'intérêt que vous avez manifesté pour le poste de " +
                        candidature.getPoste().getTitre() + " au sein de notre entreprise. Après une évaluation attentive de votre candidature, nous regrettons de vous informer que nous avons décidé de ne pas poursuivre le processus de recrutement avec votre profil.\n\n" +
                        "Nous tenons à vous assurer que cette décision ne reflète en aucun cas sur vos qualifications ou vos compétences professionnelles. Nous avons reçu un grand nombre de candidatures très compétentes, et bien que votre profil soit impressionnant, nous avons dû faire un choix difficile.\n\n" +
                        "Nous vous souhaitons le meilleur dans vos recherches futures et vous remercions encore une fois pour l'intérêt que vous avez porté à notre entreprise.\n\n" +
                        "Cordialement,\n\n" +
                        "L'équipe de recrutement de 4YOU";
                emailService.sendEmail(candidatEmail, subject, body);

                System.out.println("E-mail de refus envoyé à : " + candidatEmail);

            }

            candidatureRepository.save(candidature); // Enregistrer la candidature mise à jour avec le score et l'état
            return ResponseEntity.ok("Score enregistré avec succès pour la candidature avec l'ID : " + candidatureId);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/candidatures/accepter")
    public void accepterCandidatures(@RequestBody List<Long> candidatureIds) {
        candidatureService.accepterCandidatures(candidatureIds);
    }
    @Autowired
    private ResumeMatcherService resumeMatcherService;
    @PostMapping("/match-percentage/{collaborateurId}/{posteId}")
    public double getMatchPercentage(@PathVariable Long collaborateurId, @PathVariable Long posteId) {
        try {
            return resumeMatcherService.calculateMatchPercentage(collaborateurId, posteId);
        } catch (IOException e) {
            e.printStackTrace();
            // Gérer l'erreur
            return -1; // Ou tout autre indicateur d'erreur
        }
    }
    @GetMapping("/extract-text/{collaborateurId}")
    public String extractTextFromPDF(@PathVariable Long collaborateurId) {
        String basePath = "C:\\Users\\olkhaznaji\\Desktop\\StagePFE\\Frontend\\src\\assets\\";
        try {
            return resumeMatcherService.extractTextFromPDF(collaborateurId, basePath);
        } catch (IOException e) {
            e.printStackTrace();
            // Gérer l'erreur
            return "Erreur lors de l'extraction du texte du PDF.";
        }
    }
}
