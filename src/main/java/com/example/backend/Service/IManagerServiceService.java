package com.example.backend.Service;

import com.example.backend.Entity.ManagerService;

import java.util.List;
import java.util.Optional;

public interface IManagerServiceService {
    List<ManagerService> getAllManagerServices();

    Optional<ManagerService> getManagerServiceById(Long id);

    ManagerService createManagerService(ManagerService managerService);

    ManagerService updateManagerService(Long id, ManagerService updatedManagerService);

    void deleteManagerService(Long id);
}
