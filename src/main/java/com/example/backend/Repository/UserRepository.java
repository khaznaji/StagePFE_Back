package com.example.backend.Repository;

import com.example.backend.Entity.ManagerService;
import com.example.backend.Entity.Role;
import com.example.backend.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
    User findByPrenom(String prenom);
    List<User> findByRole(Role role);
    List<User> findByManagerService(ManagerService managerService);


    boolean existsByEmail(String email);
    boolean existsByMatricule(String email);


    User findUserByEmail(String email);

}
