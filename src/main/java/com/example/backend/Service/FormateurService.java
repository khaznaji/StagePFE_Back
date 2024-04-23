package com.example.backend.Service;

import com.example.backend.Entity.Formateur;
import com.example.backend.Repository.FormateurRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class FormateurService {
    @Autowired
    private FormateurRepository formateurRepository;
    public Formateur findById(Long id) {
        return formateurRepository.findById(id).orElse(null);
    }
}
