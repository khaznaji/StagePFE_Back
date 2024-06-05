package com.example.backend.Service;

import com.example.backend.Entity.*;
import com.example.backend.Repository.ManagerServiceRepository;
import com.example.backend.Security.services.UserDetailsImpl;
import com.example.backend.exception.MatriculeAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ManagerServiceService implements IManagerServiceService{
    private final ManagerServiceRepository managerServiceRepository;

    @Autowired
    public ManagerServiceService(ManagerServiceRepository managerServiceRepository) {
        this.managerServiceRepository = managerServiceRepository;
    }
@Override
    public List<ManagerService> getAllManagerServices() {
        return managerServiceRepository.findAll();
    }
    @Override

    public Optional<ManagerService> getManagerServiceById(Long id) {
        return managerServiceRepository.findById(id);
    }
    @Override

    public ManagerService createManagerService(ManagerService managerService) {
        // Logique de création spécifique, si nécessaire
        return managerServiceRepository.save(managerService);
    }

    @Override

    public ManagerService updateManagerService(Long id, ManagerService updatedManagerService) {
        // Logique de mise à jour spécifique, si nécessaire
        if (managerServiceRepository.existsById(id)) {
            updatedManagerService.setId(id);
            return managerServiceRepository.save(updatedManagerService);
        }
        return null; // Ou lancez une exception, selon la logique souhaitée
    }
    @Override

    public void deleteManagerService(Long id) {
        // Logique de suppression spécifique, si nécessaire
        managerServiceRepository.deleteById(id);
    }
    public List<List<Map<String, Object>>> getMembers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long managerServiceId = userDetails.getId();
        ManagerService managerService = managerServiceRepository.findByManagerManagerId(managerServiceId)
                .orElseThrow(() -> new RuntimeException("ManagerService non trouvé avec l'ID : " + managerServiceId));

        List<Collaborateur> candidatures = managerService.getCollaborateurs();

        List<List<Map<String, Object>>> result = new ArrayList<>();

        for (Collaborateur candidature : candidatures) {
            List<Map<String, Object>> collaborateurDetails = new ArrayList<>();

            Map<String, Object> collaborateurInfo = new HashMap<>();
            collaborateurInfo.put("id", candidature.getCollaborateur().getId());
            collaborateurInfo.put("idUser", candidature.getId());

            collaborateurInfo.put("nom", candidature.getCollaborateur().getNom());
            collaborateurInfo.put("prenom", candidature.getCollaborateur().getPrenom());
            collaborateurInfo.put("image", candidature.getCollaborateur().getImage());
            collaborateurDetails.add(collaborateurInfo);
            result.add(collaborateurDetails);
        }

        return result;
    }

    public Map<String, List<String>> getTopThreeCompetencesByCategory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long managerServiceId = userDetails.getId();
        ManagerService managerService = managerServiceRepository.findByManagerManagerId(managerServiceId)
                .orElseThrow(() -> new RuntimeException("ManagerService non trouvé avec l'ID : " + managerServiceId));

        List<Collaborateur> collaborateurs = managerService.getCollaborateurs();

        // Récupérez toutes les évaluations de compétences pour tous les collaborateurs
        List<Evaluation> evaluations = collaborateurs.stream()
                .flatMap(collaborateur -> collaborateur.getEvaluations().stream())
                .collect(Collectors.toList());

        // Regroupez les évaluations par compétence et calculez la moyenne
        Map<Competence, Double> competenceAverageMap = evaluations.stream()
                .collect(Collectors.groupingBy(
                        Evaluation::getCompetence,
                        Collectors.averagingDouble(Evaluation::getEvaluation)
                ));

        // Triez les compétences en fonction de leur moyenne d'évaluation
        List<Competence> sortedCompetences = competenceAverageMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Séparez les compétences en "hard skills" et "soft skills"
        List<Competence> hardSkills = sortedCompetences.stream()
                .filter(competence -> competence.getDomaine() == Domaine.HardSkills)
                .collect(Collectors.toList());

        List<Competence> softSkills = sortedCompetences.stream()
                .filter(competence -> competence.getDomaine() == Domaine.SoftSkills)
                .collect(Collectors.toList());

        // Sélectionnez les trois meilleures compétences de chaque catégorie
        List<String> topThreeHardSkills = hardSkills.stream()
                .limit(3)
                .map(Competence::getNom)
                .collect(Collectors.toList());

        List<String> topThreeSoftSkills = softSkills.stream()
                .limit(3)
                .map(Competence::getNom)
                .collect(Collectors.toList());

        Map<String, List<String>> topThreeCompetencesByCategory = new HashMap<>();
        topThreeCompetencesByCategory.put("Hard Skills", topThreeHardSkills);
        topThreeCompetencesByCategory.put("Soft Skills", topThreeSoftSkills);

        return topThreeCompetencesByCategory;
    }
    public List<Map<String, Object>> getPostesWithCandidatureCount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long managerServiceId = userDetails.getId();

        ManagerService managerService = managerServiceRepository.findByManagerManagerId(managerServiceId)
                .orElseThrow(() -> new RuntimeException("ManagerService non trouvé avec l'ID : " + managerServiceId));

        return managerService.getPostes()
                .stream()
                .filter(poste -> poste.getPoste() == EtatPoste.Publie)
                .map(poste -> {
                    Map<String, Object> posteMap = new HashMap<>();
                    posteMap.put("titre", poste.getTitre());
                    posteMap.put("nbr", poste.getCandidatures().size());
                    return posteMap;
                })
                .collect(Collectors.toList());
    }


}
