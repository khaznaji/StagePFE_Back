package com.example.backend.Service;

import com.example.backend.Entity.Collaborateur;
import com.example.backend.Entity.Formateur;
import com.example.backend.Entity.Formation;
import com.example.backend.Entity.Groups;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.FormateurRepository;
import com.example.backend.Repository.FormationRepository;
import com.example.backend.Repository.GroupsRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GroupeService {

    private  GroupsRespository groupsRepository;
    private  FormateurRepository formateurRepository;
    private  FormationRepository formationRepository;
    private CollaborateurRepository collaborateurRepository;


    @Autowired
    public GroupeService(GroupsRespository groupsRepository, FormateurRepository formateurRepository, FormationRepository formationRepository) {
        this.groupsRepository = groupsRepository;
        this.formateurRepository = formateurRepository;
        this.formationRepository = formationRepository;
    }
    public void addGroupWithFormationAndCollaborateurs(Long formationId, String nom, List<Long> collaborateursIds) {
        // Récupérer la formation depuis la base de données
        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));

        // Récupérer le formateur associé à la formation
        Formateur formateur = formation.getFormateur();

        // Créer une nouvelle instance de Groupes
        Groups groupe = new Groups();
        groupe.setFormation(formation);
        groupe.setFormateur(formateur);
        groupe.setNom(nom);

        // Récupérer la liste des collaborateurs depuis la base de données
        List<Collaborateur> collaborateurs = collaborateurRepository.findAllById(collaborateursIds);
        groupe.setCollaborateurs(collaborateurs);

        // Enregistrer le groupe dans la base de données
        groupsRepository.save(groupe);
    }

  }
