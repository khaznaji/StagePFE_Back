package com.example.backend.Service;

import com.example.backend.Entity.Departement;

import java.util.List;

public interface IDepartementService {
    Departement addDepartement(Departement departement);

    Departement getDepartementById(Long id);

    List<Departement> getAllDepartements();

    Departement updateDepartement(Long id, Departement newDepartement);

    void deleteDepartement(Long id);
}
