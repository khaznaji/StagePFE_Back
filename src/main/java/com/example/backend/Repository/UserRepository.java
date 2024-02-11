package com.example.backend.Repository;

import com.example.backend.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
    @Query(value = "SELECT * FROM user WHERE email = ?1 AND is_activated = 1", nativeQuery = true)
    Integer findUserByIsActived(String username);
    boolean existsByEmail(String email);

}
