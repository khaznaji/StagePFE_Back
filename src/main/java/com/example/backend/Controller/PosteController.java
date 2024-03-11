package com.example.backend.Controller;

import com.example.backend.Entity.Competence;
import com.example.backend.Entity.ManagerService;
import com.example.backend.Entity.Poste;
import com.example.backend.Repository.ManagerServiceRepository;
import com.example.backend.Repository.PosteRepository;
import com.example.backend.Security.services.UserDetailsImpl;
import com.example.backend.Service.PosteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;

@Controller
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/Poste")
public class PosteController {
    @Autowired
    private PosteRepository posteRepository;

    private PosteService posteService;
    @Autowired
    private ManagerServiceRepository managerServiceRepository;
    @PostMapping("/create")
    public ResponseEntity<?> createPoste(
            @RequestParam("titre") String titre,
            @RequestParam("description") String description,
            @RequestParam("nombrePostesDisponibles") int nombrePostesDisponibles,
            @RequestParam("competences") List<Competence> competences


    ){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long managerServiceId = userDetails.getId();
        ManagerService managerService = managerServiceRepository.findById(managerServiceId)
                .orElseThrow(() -> new EntityNotFoundException("ManagerService non trouvé avec l'ID : " + managerServiceId));
        Poste poste = new Poste();
        poste.setManagerService(managerService);
        poste.setDateCreation(LocalDate.now()); // Date de création du poste
        poste.setDepartement(managerService.getDepartment()); // Utilisez le département du manager de service
        poste.setNombrePostesDisponibles(nombrePostesDisponibles);
        poste.setTitre(titre);
        poste.setDescription(description);
        poste.setApprouveParManagerRH(false);
        poste.setCompetences(competences);
        System.out.println(poste);
        posteRepository.save(poste);
        return new ResponseEntity<>(poste, HttpStatus.CREATED);
    }
}
