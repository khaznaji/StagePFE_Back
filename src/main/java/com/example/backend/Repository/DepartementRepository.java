package com.example.backend.Repository;

import com.example.backend.Entity.Departement;
import com.example.backend.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartementRepository  extends JpaRepository<Departement,Long> {
}
