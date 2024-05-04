package com.example.backend.Repository;

import com.example.backend.Entity.Formation;
import com.example.backend.Entity.Groups;
import com.example.backend.Entity.ManagerService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupsRespository extends JpaRepository<Groups,Long> {
    List<Groups> findByFormation(Formation formation);
    List<Groups> findByFormateur_Id(Long formateurId);


}
