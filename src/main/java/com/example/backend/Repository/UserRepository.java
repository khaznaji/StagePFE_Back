package com.example.backend.Repository;

import com.example.backend.Entity.Role;
import com.example.backend.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);

    boolean existsByEmail(String email);
    boolean existsByMatricule(String email);


    User findUserByEmail(String email);

}
