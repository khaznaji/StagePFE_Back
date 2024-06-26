package com.example.backend.Controller;
import com.example.backend.Entity.Collaborateur;
import com.example.backend.Entity.Competence;
import com.example.backend.Entity.Evaluation;
import com.example.backend.Entity.User;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.CompetenceRepository;
import com.example.backend.Repository.EvaluationRepository;
import com.example.backend.Security.services.UserDetailsImpl;
import com.example.backend.Service.CollaborateurService;
import com.example.backend.Service.CompetenceService;
import com.example.backend.Service.EvaluationService;
import com.example.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/Evaluation")
public class EvaluationController {
    @Autowired

    private CollaborateurRepository collaborateurRepository;

    @Autowired
    private CollaborateurService collaborateurService;
    @Autowired
    private EvaluationRepository evaluationRepository; // Assurez-vous d'avoir ce service injecté

    @Autowired
    private CompetenceRepository competenceRepository;
    @PostMapping("/evaluation")
    public ResponseEntity<?> addEvaluation(@RequestParam("competenceId") Long competenceId,
                                           @RequestParam("evaluationValue") int evaluationValue) {
        // Récupérer l'utilisateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();
        Collaborateur collaborateur = user.getCollaborateur();

        // Rechercher la compétence correspondante à partir de son ID
        Optional<Competence> competenceOptional = competenceRepository.findById(competenceId);
        if (!competenceOptional.isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Compétence non trouvée pour l'ID: " + competenceId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        Competence competence = competenceOptional.get();

        // Vérifier si une évaluation existe déjà pour cette compétence et ce collaborateur
        Optional<Evaluation> existingEvaluationOptional = evaluationRepository.findByCollaborateurAndCompetence(collaborateur, competence);
        if (existingEvaluationOptional.isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Vous avez déjà évalué cette compétence");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Créer une nouvelle évaluation
        Evaluation evaluation = new Evaluation();
        evaluation.setCollaborateur(collaborateur);
        evaluation.setCompetence(competence);
        evaluation.setEvaluation(evaluationValue);

        // Enregistrer l'évaluation dans la base de données
        evaluationRepository.save(evaluation);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Évaluation ajoutée avec succès pour la compétence: " + competence.getNom());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PostMapping("/evaluation/{collaborateurId}")
    public ResponseEntity<?> addEvaluation(@PathVariable Long collaborateurId,
                                           @RequestParam("competenceId") Long competenceId,
                                           @RequestParam("evaluationValue") int evaluationValue) {
        // Rechercher le collaborateur correspondant à partir de son ID
        Optional<Collaborateur> collaborateurOptional = collaborateurRepository.findById(collaborateurId);
        if (!collaborateurOptional.isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Collaborateur non trouvé pour l'ID: " + collaborateurId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        Collaborateur collaborateur = collaborateurOptional.get();

        // Rechercher la compétence correspondante à partir de son ID
        Optional<Competence> competenceOptional = competenceRepository.findById(competenceId);
        if (!competenceOptional.isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Compétence non trouvée pour l'ID: " + competenceId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        Competence competence = competenceOptional.get();

        // Vérifier si une évaluation existe déjà pour cette compétence et ce collaborateur
        Optional<Evaluation> existingEvaluationOptional = evaluationRepository.findByCollaborateurAndCompetence(collaborateur, competence);
        if (existingEvaluationOptional.isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Vous avez déjà évalué cette compétence");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Créer une nouvelle évaluation
        Evaluation evaluation = new Evaluation();
        evaluation.setCollaborateur(collaborateur);
        evaluation.setCompetence(competence);
        evaluation.setEvaluation(evaluationValue);

        // Enregistrer l'évaluation dans la base de données
        evaluationRepository.save(evaluation);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Évaluation ajoutée avec succès pour la compétence: " + competence.getNom());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PutMapping("/evaluation/{evaluationId}/{collaborateurId}")
    public ResponseEntity<?> updateEvaluationByIdAndCollaborateurId(
            @PathVariable("evaluationId") Long evaluationId,
            @PathVariable("collaborateurId") Long collaborateurId,
            @RequestParam(value = "evaluation") int evaluation) {





        // Recherche de l'évaluation par son identifiant
        Optional<Evaluation> existingEvaluationOptional = evaluationRepository.findById(evaluationId);

        if (!existingEvaluationOptional.isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Aucune évaluation trouvée pour cet identifiant");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Evaluation existingEvaluation = existingEvaluationOptional.get();

        // Assurez-vous que l'évaluation appartient bien au collaborateur spécifié dans le chemin de l'URL
        if (!existingEvaluation.getCollaborateur().getId().equals(collaborateurId)) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "L'évaluation spécifiée n'appartient pas au collaborateur spécifié");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        existingEvaluation.setEvaluation(evaluation);
        evaluationRepository.save(existingEvaluation);
        Collaborateur collaborateur = existingEvaluation.getCollaborateur();
        collaborateur.setVerified(true); // Mettre le statut à true
        collaborateurRepository.save(collaborateur);
        Map<String, String> response = new HashMap<>();
        response.put("message", "L'évaluation a été mise à jour avec succès");
        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/evaluation/{evaluationId}")
    public ResponseEntity<?> updateEvaluationById(@PathVariable("evaluationId") Long evaluationId,
                                                  @RequestParam(value = "evaluation") int evaluation)
    {
        // Récupérer l'utilisateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();
        Collaborateur collaborateur = user.getCollaborateur();

        // Recherche de l'évaluation par son identifiant
        Optional<Evaluation> existingEvaluationOptional = evaluationRepository.findById(evaluationId);

        if (!existingEvaluationOptional.isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Aucune évaluation trouvée pour cet identifiant");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        Evaluation existingEvaluation = existingEvaluationOptional.get();
        if (!existingEvaluation.getCollaborateur().getId().equals(collaborateur.getId())) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Vous n'êtes pas autorisé à modifier cette évaluation");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        existingEvaluation.setEvaluation(evaluation);
        evaluationRepository.save(existingEvaluation);
        Map<String, String> response = new HashMap<>();
        response.put("message", "L'évaluation a été mise à jour avec succès");
        return ResponseEntity.ok().body(response);  }
    @DeleteMapping("/evaluation/{evaluationId}/{collaborateurId}")
    public ResponseEntity<?> deleteEvaluationByIdAndCollaborateurId(@PathVariable("evaluationId") Long evaluationId,
                                                                    @PathVariable("collaborateurId") Long collaborateurId) {

        // Recherche de l'évaluation par son identifiant
        Optional<Evaluation> existingEvaluationOptional = evaluationRepository.findById(evaluationId);

        if (!existingEvaluationOptional.isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Aucune évaluation trouvée pour cet identifiant");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Evaluation existingEvaluation = existingEvaluationOptional.get();

        // Vérifier si l'évaluation appartient au collaborateur spécifié dans le chemin de l'URL
        if (!existingEvaluation.getCollaborateur().getId().equals(collaborateurId)) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "L'évaluation spécifiée n'appartient pas au collaborateur spécifié");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        evaluationRepository.delete(existingEvaluation);

        Map<String, String> response = new HashMap<>();
        response.put("message", "L'évaluation a été supprimée avec succès");
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/evaluation/{evaluationId}")
    public ResponseEntity<?> deleteEvaluationById(@PathVariable("evaluationId") Long evaluationId) {
        // Récupérer l'utilisateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();
        Collaborateur collaborateur = user.getCollaborateur();
        Optional<Evaluation> existingEvaluationOptional = evaluationRepository.findById(evaluationId);
        if (!existingEvaluationOptional.isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Aucune évaluation trouvée pour cet identifiant");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        Evaluation existingEvaluation = existingEvaluationOptional.get();
        if (!existingEvaluation.getCollaborateur().getId().equals(collaborateur.getId())) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Vous n'êtes pas autorisé à supprimer cette évaluation");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        evaluationRepository.delete(existingEvaluation);
        Map<String, String> response = new HashMap<>();
        response.put("message", "L'évaluation a été supprimée avec succès");
        return ResponseEntity.ok().body(response);
    }
    @GetMapping("/evaluations/{collaborateurId}")
    public ResponseEntity<?> getAllEvaluationsForCollaborateur(@PathVariable Long collaborateurId) {
        // Rechercher le collaborateur correspondant à partir de son ID
        Optional<Collaborateur> collaborateurOptional = collaborateurRepository.findById(collaborateurId);
        if (!collaborateurOptional.isPresent()) {
            return new ResponseEntity<>("Collaborateur non trouvé pour cet ID", HttpStatus.NOT_FOUND);
        }
        Collaborateur collaborateur = collaborateurOptional.get();

        // Rechercher toutes les évaluations pour ce collaborateur
        List<Evaluation> evaluations = evaluationRepository.findByCollaborateur(collaborateur);

        if (evaluations.isEmpty()) {
            return new ResponseEntity<>("Aucune évaluation trouvée pour ce collaborateur", HttpStatus.NOT_FOUND);
        }

        // Récupérer le nom de la compétence pour chaque évaluation
        evaluations.forEach(evaluation -> {
            evaluation.setCompetenceName(evaluation.getCompetence().getNom());
        });

        return new ResponseEntity<>(evaluations, HttpStatus.OK);
    }

   @GetMapping("/evaluations")
    public ResponseEntity<?> getAllEvaluationsForCurrentUser() {
        // Récupérer l'utilisateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();
        Collaborateur collaborateur = user.getCollaborateur();

        // Rechercher toutes les évaluations pour ce collaborateur
        List<Evaluation> evaluations = evaluationRepository.findByCollaborateur(collaborateur);

        if (evaluations.isEmpty()) {
            return new ResponseEntity<>("Aucune évaluation trouvée pour cet utilisateur", HttpStatus.NOT_FOUND);
        }

        // Récupérer le nom de la compétence pour chaque évaluation
        evaluations.forEach(evaluation -> {
            evaluation.setCompetenceName(evaluation.getCompetence().getNom());
        });

        return new ResponseEntity<>(evaluations, HttpStatus.OK);
    }
    @PutMapping("/ajoutCompetenceCollaborateur")
    public ResponseEntity<Map<String, String>> updateCompetenceCollaborateur(
            @RequestParam(value = "competences", required = false) List<Competence> competences,
            @RequestParam(value = "evaluations", required = false) List<Integer> evaluations
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();

        Map<String, String> response = new HashMap<>();

        // Update the competences if provided
        if (competences != null) {
            Collaborateur collaborateur = collaborateurService.getCollaborateurWithEvaluations(user.getCollaborateur().getId()); // Appel de la méthode pour récupérer le collaborateur avec les évaluations chargées
            List<Competence> existingCompetences = collaborateur.getCompetences();

            // Add evaluations for each competence
            if (evaluations != null && evaluations.size() == competences.size()) {
                for (int i = 0; i < competences.size(); i++) {
                    Competence competence = competences.get(i);
                    int evaluationScore = evaluations.get(i);

                    // Check if the competence already exists for the collaborator
                    boolean competenceExists = existingCompetences.stream()
                            .anyMatch(existingCompetence -> existingCompetence.getId().equals(competence.getId()));

                    if (!competenceExists) {
                        // Add the competence to the collaborator
                        existingCompetences.add(competence);
                        collaborateur.getCompetences().add(competence); // Ajout de la compétence au collaborateur

                        // Add the evaluation for the new competence
                        Evaluation evaluation = new Evaluation();
                        evaluation.setCollaborateur(collaborateur);
                        evaluation.setCompetence(competence);
                        evaluation.setEvaluation(evaluationScore);

                        // Save both the new competence and evaluation
                        collaborateurRepository.save(collaborateur);
                        evaluationRepository.save(evaluation);

                        // Add success message for adding new competence and evaluation
                        response.put("success", "La compétence " + competence.getNom() + " a été ajoutée avec succès avec une évaluation de " + evaluationScore);
                    } else {
                        // If the competence exists, find and update the existing evaluation
                        Evaluation existingEvaluation = collaborateur.getEvaluations().stream()
                                .filter(e -> e.getCompetence().getId().equals(competence.getId()))
                                .findFirst()
                                .orElse(null);

                        if (existingEvaluation != null) {
                            existingEvaluation.setEvaluation(evaluationScore);
                            evaluationRepository.save(existingEvaluation);
                            response.put("evaluationUpdated", "L'évaluation de la compétence " + competence.getNom() + " a été mise à jour avec succès");
                        } else {
                            // Add the evaluation for the existing competence
                            Evaluation newEvaluation = new Evaluation();
                            newEvaluation.setCollaborateur(collaborateur);
                            newEvaluation.setCompetence(competence);
                            newEvaluation.setEvaluation(evaluationScore);

                            // Save the new evaluation
                            evaluationRepository.save(newEvaluation);

                            response.put("evaluationAdded", "L'évaluation de la compétence " + competence.getNom() + " a été ajoutée avec succès");
                        }
                    }
                }
            }

            collaborateurRepository.save(collaborateur);
            response.put("competences", "Competences updated successfully");
        }

        // Handle PDF file upload
        // Handle PDF file upload

        // Return response
        return ResponseEntity.ok(response);
    }
}
