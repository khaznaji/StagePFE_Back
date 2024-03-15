package com.example.backend.Repository;

import com.example.backend.Entity.Collaborateur;
import com.example.backend.Entity.ManagerService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ManagerServiceRepository extends JpaRepository<ManagerService,Long> {
    @Query("SELECT c FROM ManagerService c WHERE c.manager.id = :manager_id")
    Optional<ManagerService> findByManagerManagerId(@Param("manager_id") Long manager_id);
}
