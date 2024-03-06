package com.example.backend.Service;

import com.example.backend.Entity.Gender;
import com.example.backend.Entity.ManagerService;
import com.example.backend.Entity.Role;
import com.example.backend.Entity.User;
import com.example.backend.Repository.ManagerServiceRepository;
import com.example.backend.exception.MatriculeAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ManagerServiceService implements IManagerServiceService{
    private final ManagerServiceRepository managerServiceRepository;

    @Autowired
    public ManagerServiceService(ManagerServiceRepository managerServiceRepository) {
        this.managerServiceRepository = managerServiceRepository;
    }
@Override
    public List<ManagerService> getAllManagerServices() {
        return managerServiceRepository.findAll();
    }
    @Override

    public Optional<ManagerService> getManagerServiceById(Long id) {
        return managerServiceRepository.findById(id);
    }
    @Override

    public ManagerService createManagerService(ManagerService managerService) {
        // Logique de création spécifique, si nécessaire
        return managerServiceRepository.save(managerService);
    }

    @Override

    public ManagerService updateManagerService(Long id, ManagerService updatedManagerService) {
        // Logique de mise à jour spécifique, si nécessaire
        if (managerServiceRepository.existsById(id)) {
            updatedManagerService.setId(id);
            return managerServiceRepository.save(updatedManagerService);
        }
        return null; // Ou lancez une exception, selon la logique souhaitée
    }
    @Override

    public void deleteManagerService(Long id) {
        // Logique de suppression spécifique, si nécessaire
        managerServiceRepository.deleteById(id);
    }
}
