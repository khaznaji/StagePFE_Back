package com.example.backend.Repository;

import com.example.backend.Entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormationRepository extends JpaRepository<Formation, Long> {
    List<Formation> findAllByFormateur(Formateur formateur);
    List<Formation> findByDepartment(Departement department);

}
