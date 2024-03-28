package com.example.backend.Controller;

import com.example.backend.Configuration.MailConfig;
import com.example.backend.Entity.*;
import com.example.backend.Repository.CandidatureRepository;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.ManagerServiceRepository;
import com.example.backend.Repository.PosteRepository;
import com.example.backend.Security.services.UserDetailsImpl;
import com.example.backend.Service.CandidatureService;
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
    private CandidatureService candidatureService;
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
        poste.setPoste(EtatPoste.En_cours);
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
           poste.setPoste(EtatPoste.Accepte);// Update the field to true
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
                                       @RequestParam(required = false) Integer nombrePostesDisponibles) {
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

        // Récupérer les postes auxquels le collaborateur a postulé
        List<Candidature> postulesParCollaborateur = collaborateur.getCandidatures();

        // Récupérer tous les postes approuvés
        List<Poste> approvedPostes = posteRepository.findByPoste(EtatPoste.Publie);

        // Filtrer les postes pour ne garder que ceux auxquels le collaborateur n'a pas postulé
        List<Poste> postesNonPostules = approvedPostes.stream()
                .filter(poste -> postulesParCollaborateur.stream()
                        .noneMatch(candidature -> candidature.getPoste().equals(poste)))
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
            poste.setPoste(EtatPoste.Publie); // Mettez à jour l'état du poste à Publie
            posteRepository.save(poste); // Sauvegardez l'entité mise à jour

            // Votre code pour envoyer un e-mail ou effectuer d'autres actions après l'approbation du poste

            return new ResponseEntity<>(poste, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Poste not found with ID: " + postId, HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/postepublie")
    public ResponseEntity<List<Poste>> getPostePublie() {
        List<Poste> demandesEnCours = posteRepository.findByPoste(EtatPoste.Publie);
        return new ResponseEntity<>(demandesEnCours, HttpStatus.OK);
    }











  /***** Candidature Controller********/
    @PutMapping("/updateState/{candidatureId}")
    public ResponseEntity<?> updateCandidatureState(@PathVariable Long candidatureId, @RequestParam String newState) {
        Optional<Candidature> optionalCandidature = candidatureRepository.findById(candidatureId);
        if (optionalCandidature.isPresent()) {
            Candidature candidature = optionalCandidature.get();
            // Mettre à jour l'état de la candidature
            candidature.setEtat(EtatPostulation.valueOf(newState));
            candidatureRepository.save(candidature); // Sauvegarder la candidature mise à jour
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
            candidatInfo.put("nom", user.getNom());
            candidatInfo.put("image", user.getImage());

            candidatInfo.put("prenom", user.getPrenom());
            candidatInfo.put("email", user.getEmail());
            candidatInfo.put("collaborateur_id", collaborateur.getId().toString()); // ID du collaborateur
            candidatInfo.put("poste_id", candidature.getPoste().getId().toString()); // ID du poste

            candidatsInfo.add(candidatInfo);
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

    @GetMapping("/countCollaborateursEnAttente/{postId}")
    public ResponseEntity<Long> countCollaborateursEnAttente(@PathVariable Long postId) {
        Optional<Poste> optionalPoste = posteRepository.findById(postId);
        if (optionalPoste.isPresent()) {
            Poste poste = optionalPoste.get();
            Long count = candidatureRepository.countByPosteAndEtat(poste, EtatPostulation.EN_ATTENTE);
            return ResponseEntity.ok(count);
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


}
