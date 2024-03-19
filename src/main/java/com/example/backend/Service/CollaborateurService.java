package com.example.backend.Service;

import com.example.backend.Entity.Collaborateur;
import com.example.backend.Entity.Competence;
import com.example.backend.Entity.Evaluation;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.CompetenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service

public class CollaborateurService implements ICollaborateurService {

    @Autowired
    private CollaborateurRepository collaborateurRepository;
    @Autowired
    private CompetenceRepository competenceRepository;
@Override
    public Collaborateur getCollaborateurDetailsById(Long collaborateurId) {
        return collaborateurRepository.findByIdWithAssociations(collaborateurId)
                .orElseThrow(() -> new RuntimeException("Collaborator not found with ID: " + collaborateurId));
    }
    public Map<Competence, List<Evaluation>> getEvaluationsForCompetences(List<Competence> competences) {
        // Utiliser le stream API pour récupérer les évaluations pour chaque compétence
        return competences.stream().collect(Collectors.toMap(
                competence -> competence, // Clé : la compétence elle-même
                competence -> competenceRepository.findEvaluationsByCompetence(competence) // Valeur : la liste des évaluations pour cette compétence
        ));
    }
    public Collaborateur getCollaborateurWithEvaluations(Long collaborateurId) {
        // Ouvrir une session Hibernate (la transaction doit déjà être active grâce à @Transactional)
        // Cela garantit que la session Hibernate est disponible pour charger les évaluations
        // avant de les retourner
        Collaborateur collaborateur = collaborateurRepository.findById(collaborateurId).orElse(null);

        // Assurez-vous que le collaborateur et les évaluations sont chargés avant de retourner
        if (collaborateur != null) {
            collaborateur.getEvaluations().size(); // Chargement effectif des évaluations
        }

        return collaborateur;
    }
}
