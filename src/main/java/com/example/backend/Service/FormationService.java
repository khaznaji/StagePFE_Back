package com.example.backend.Service;

import com.example.backend.Entity.Formation;
import com.example.backend.Repository.FormationRepository;
import com.example.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;

@Service
public class FormationService {
    @Autowired
    private FormationRepository formationRepository ;
    public Formation getFormationById(Long formationId) {
        return formationRepository.findById(formationId)
                .orElseThrow(() -> new EntityNotFoundException("Formation not found with id " + formationId));
    }
}
