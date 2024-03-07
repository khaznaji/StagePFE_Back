package com.example.backend.Service;

import com.example.backend.Entity.Collaborateur;
import com.example.backend.Repository.CollaborateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

public class CollaborateurService implements ICollaborateurService {

    @Autowired
    private CollaborateurRepository collaborateurRepository;
@Override
    public Collaborateur getCollaborateurDetailsById(Long collaborateurId) {
        return collaborateurRepository.findByIdWithAssociations(collaborateurId)
                .orElseThrow(() -> new RuntimeException("Collaborator not found with ID: " + collaborateurId));
    }
}
