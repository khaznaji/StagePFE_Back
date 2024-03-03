package com.example.backend.Service;

import com.example.backend.Entity.Departement;
import com.example.backend.Repository.DepartementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DepartementService implements IDepartementService{
    @Autowired
    private DepartementRepository departementRepository;

    @Override
    public Departement addDepartement(Departement departement) {
        return departementRepository.save(departement);
    }

    @Override
    public Departement getDepartementById(Long id) {
        return departementRepository.findById(id).orElse(null);
    }

    @Override
    public List<Departement> getAllDepartements() {
        return departementRepository.findAll();
    }

    @Override
    public Departement updateDepartement(Long id, Departement newDepartement) {
        Optional<Departement> existingDepartement = departementRepository.findById(id);
        if (existingDepartement.isPresent()) {
            Departement departement = existingDepartement.get();
            departement.setNom(newDepartement.getNom());


            return departementRepository.save(departement);
        }
        return null;
    }

    @Override
    public void deleteDepartement(Long id) {
        departementRepository.deleteById(id);
    }
}
