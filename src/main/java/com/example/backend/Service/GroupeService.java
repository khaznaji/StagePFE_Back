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
    public Groups createGroupe(Groups groupe) {
        return groupsRepository.save(groupe);
    }

    // Read
    public List<Groups> getAllGroupes() {
        return groupsRepository.findAll();
    }

    public Optional<Groups> getGroupeById(Long id) {
        return groupsRepository.findById(id);
    }

    // Update
    public Groups updateGroupe(Groups groupe) {
        return groupsRepository.save(groupe);
    }

    // Delete
    public void deleteGroupe(Long id) {
        groupsRepository.deleteById(id);
    }


  }
