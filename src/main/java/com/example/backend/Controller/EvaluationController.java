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
            return new ResponseEntity<>("Compétence non trouvée pour l'ID: " + competenceId, HttpStatus.NOT_FOUND);
        }
        Competence competence = competenceOptional.get();

        // Vérifier si une évaluation existe déjà pour cette compétence et ce collaborateur
        Optional<Evaluation> existingEvaluationOptional = evaluationRepository.findByCollaborateurAndCompetence(collaborateur, competence);
        if (existingEvaluationOptional.isPresent()) {
            return new ResponseEntity<>("Vous avez déjà évalué cette compétence", HttpStatus.BAD_REQUEST);
        }

        // Créer une nouvelle évaluation
        Evaluation evaluation = new Evaluation();
        evaluation.setCollaborateur(collaborateur);
        evaluation.setCompetence(competence);
        evaluation.setEvaluation(evaluationValue);

        // Enregistrer l'évaluation dans la base de données
        evaluationRepository.save(evaluation);

        return new ResponseEntity<>("Évaluation ajoutée avec succès pour la compétence: " + competence.getNom(), HttpStatus.CREATED);
    }

    @PutMapping("/evaluation/{evaluationId}")
    public ResponseEntity<?> updateEvaluationById(@PathVariable("evaluationId") Long evaluationId,
                                                  @RequestParam(value = "evaluation") int evaluation) {
        // Récupérer l'utilisateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();
        Collaborateur collaborateur = user.getCollaborateur();

        // Recherche de l'évaluation par son identifiant
        Optional<Evaluation> existingEvaluationOptional = evaluationRepository.findById(evaluationId);

        if (!existingEvaluationOptional.isPresent()) {
            return new ResponseEntity<>("Aucune évaluation trouvée pour cet identifiant", HttpStatus.NOT_FOUND);
        }

        // Vérifier si l'utilisateur connecté est autorisé à modifier cette évaluation
        // Vérifier si l'utilisateur connecté est autorisé à modifier cette évaluation
        Evaluation existingEvaluation = existingEvaluationOptional.get();
        if (!existingEvaluation.getCollaborateur().getId().equals(collaborateur.getId())) {
            return new ResponseEntity<>("Vous n'êtes pas autorisé à modifier cette évaluation", HttpStatus.FORBIDDEN);
        }


        // Mettre à jour l'évaluation
        existingEvaluation.setEvaluation(evaluation);
        evaluationRepository.save(existingEvaluation);

        return new ResponseEntity<>("L'évaluation a été mise à jour avec succès", HttpStatus.OK);
    }

    @DeleteMapping("/evaluation/{evaluationId}")
    public ResponseEntity<?> deleteEvaluationById(@PathVariable("evaluationId") Long evaluationId) {
        // Récupérer l'utilisateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();
        Collaborateur collaborateur = user.getCollaborateur();

        // Recherche de l'évaluation par son identifiant
        Optional<Evaluation> existingEvaluationOptional = evaluationRepository.findById(evaluationId);

        if (!existingEvaluationOptional.isPresent()) {
            return new ResponseEntity<>("Aucune évaluation trouvée pour cet identifiant", HttpStatus.NOT_FOUND);
        }

        // Vérifier si l'utilisateur connecté est autorisé à supprimer cette évaluation
        Evaluation existingEvaluation = existingEvaluationOptional.get();
        if (!existingEvaluation.getCollaborateur().getId().equals(collaborateur.getId())) {
            return new ResponseEntity<>("Vous n'êtes pas autorisé à supprimer cette évaluation", HttpStatus.FORBIDDEN);
        }

        // Supprimer l'évaluation
        evaluationRepository.delete(existingEvaluation);

        return new ResponseEntity<>("L'évaluation a été supprimée avec succès", HttpStatus.OK);
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
