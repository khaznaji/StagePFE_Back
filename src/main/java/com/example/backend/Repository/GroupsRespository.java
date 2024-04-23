package com.example.backend.Repository;

import com.example.backend.Entity.Groups;
import com.example.backend.Entity.ManagerService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupsRespository extends JpaRepository<Groups,Long> {
}
