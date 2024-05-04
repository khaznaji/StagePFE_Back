package com.example.backend.Service;

import com.example.backend.Entity.*;
import com.example.backend.Repository.ManagerServiceRepository;
import com.example.backend.Security.services.UserDetailsImpl;
import com.example.backend.exception.MatriculeAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    public List<List<Map<String, Object>>> getMembers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long managerServiceId = userDetails.getId();
        ManagerService managerService = managerServiceRepository.findByManagerManagerId(managerServiceId)
                .orElseThrow(() -> new RuntimeException("ManagerService non trouvé avec l'ID : " + managerServiceId));

        List<Collaborateur> candidatures = managerService.getCollaborateurs();

        // Liste de listes de cartes pour stocker les détails de postulation de chaque collaborateur
        List<List<Map<String, Object>>> result = new ArrayList<>();

        // Parcourir chaque candidature et extraire les détails requis
        for (Collaborateur candidature : candidatures) {
            List<Map<String, Object>> collaborateurDetails = new ArrayList<>();

            Map<String, Object> collaborateurInfo = new HashMap<>();
            collaborateurInfo.put("id", candidature.getCollaborateur().getId());
            collaborateurInfo.put("idUser", candidature.getId());

            collaborateurInfo.put("nom", candidature.getCollaborateur().getNom());
            collaborateurInfo.put("prenom", candidature.getCollaborateur().getPrenom());
            collaborateurInfo.put("image", candidature.getCollaborateur().getImage());
            collaborateurDetails.add(collaborateurInfo);
            result.add(collaborateurDetails);
        }

        return result;
    }



}
