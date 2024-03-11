package com.example.backend.Service;

import com.example.backend.Entity.Collaborateur;
import com.example.backend.Entity.ManagerService;
import com.example.backend.Entity.Poste;
import com.example.backend.Repository.ManagerServiceRepository;
import com.example.backend.Repository.PosteRepository;
import com.example.backend.Security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;

public class PosteService implements IPosteService{

    @Autowired
    private PosteRepository posteRepository;
    @Override
    public Poste createPoste(Poste poste) {

        return posteRepository.save(poste);
    }

}
