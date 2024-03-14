package com.example.backend.Controller;

import com.example.backend.Configuration.MailConfig;
import com.example.backend.Entity.*;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.ManagerServiceRepository;
import com.example.backend.Repository.PosteRepository;
import com.example.backend.Repository.UserRepository;
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
import java.util.*;

@Controller
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/Poste")
public class PosteController {
    @Autowired
    private PosteRepository posteRepository;
    @Autowired
    private CollaborateurRepository collaborateurRepository;
    private PosteService posteService;
    @Autowired
    private MailConfig emailService;
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
    @GetMapping("/getAll")
    public ResponseEntity<List<Poste>> getAllPostes() {
        List<Poste> allPostes = posteRepository.findAll();

        System.out.println("All Postes:");
        for (Poste poste : allPostes) {
            System.out.println(poste);
        }

        return new ResponseEntity<>(allPostes, HttpStatus.OK);
    }
    @GetMapping("/getPosteById/{postId}")
    public ResponseEntity<Poste> getPosteById(@PathVariable Long postId) {
        Optional<Poste> optionalPoste = posteRepository.findById(postId);

        if (optionalPoste.isPresent()) {
            Poste poste = optionalPoste.get();
            System.out.println("Poste found by ID: " + poste);
            return new ResponseEntity<>(poste, HttpStatus.OK);
        } else {
            System.out.println("Poste not found with ID: " + postId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @PutMapping("/updateApproval/{postId}")
    public ResponseEntity<?> updateApproval(@PathVariable Long postId) {
        Optional<Poste> optionalPoste = posteRepository.findById(postId);

        if (optionalPoste.isPresent()) {
            Poste poste = optionalPoste.get();
            poste.setApprouveParManagerRH(true);
            poste.setEncours(true);

            poste.setArchive(false); // Update the field to true
            posteRepository.save(poste); // Save the updated entity
            String managerEmail = poste.getManagerService().getManager().getEmail(); // Assuming ManagerService has an email field
            String subject = "Notification d'Approbation de Poste";
            String text = "Cher(e) " + poste.getManagerService().getManager().getNom() + ",\n\n" +
                    "Nous avons le plaisir de vous informer que votre demande de poste a été approuvée par le Manager RH.\n\n" +
                    "Veuillez trouver ci-dessous les détails de votre poste :\n\n" +
                    "Titre du poste : " + poste.getTitre() + "\n" +
                    "Description : " + poste.getDescription() + "\n\n" +
                    "Nombre de postes disponibles : " + poste.getNombrePostesDisponibles() + "\n\n" +
                    "Cordialement,\n" +
                    "L'équipe 4You";
            emailService.sendEmail(managerEmail, subject, text);

            return new ResponseEntity<>(poste, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Poste not found with ID: " + postId, HttpStatus.NOT_FOUND);
        }
    }
    @PostMapping("/postuler/{postId}")
    public ResponseEntity<String> postulerAuPoste(@PathVariable Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId(); // ID de l'utilisateur

        // Use the new method to find the Collaborateur by the User's ID
        Collaborateur collaborateur = collaborateurRepository.findByCollaborateurUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouvé avec l'ID : " + userId));
        Poste poste = posteRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Poste non trouvé avec l'ID : " + postId));

        // Vérifier si le collaborateur a déjà postulé à ce poste
        if (poste.getCandidats().contains(collaborateur)) {
            return new ResponseEntity<>("Vous avez déjà postulé à ce poste.", HttpStatus.BAD_REQUEST);
        }

        // Ajouter le poste à la liste des postes auxquels le collaborateur a postulé
        collaborateur.getPostesCandidatures().add(poste);
        collaborateurRepository.save(collaborateur);

        // Ajouter le collaborateur à la liste des candidats pour ce poste
        poste.getCandidats().add(collaborateur);
        posteRepository.save(poste);

        return new ResponseEntity<>("Vous avez postulé avec succès à ce poste.", HttpStatus.OK);
    }


    @PutMapping("/updateRefus/{postId}")
    public ResponseEntity<?> updateRefus(@PathVariable Long postId) {
        Optional<Poste> optionalPoste = posteRepository.findById(postId);

        if (optionalPoste.isPresent()) {
            Poste poste = optionalPoste.get();
            poste.setApprouveParManagerRH(false);
            poste.setArchive(true);
            poste.setEncours(true);
            posteRepository.save(poste); // Save the updated entity
            String managerEmail = poste.getManagerService().getManager().getEmail(); // Assuming ManagerService has an email field
            String subject = "Notification de Refus de Poste";
            String text = "Cher(e) " + poste.getManagerService().getManager().getNom() + ",\n\n" +
                    "Nous sommes désolés de vous informer que votre demande de poste a été refusée par le Manager RH.\n\n" +
                    "Veuillez trouver ci-dessous les détails de votre demande :\n\n" +
                    "Titre du poste : " + poste.getTitre() + "\n" +
                    "Description : " + poste.getDescription() + "\n\n" +
                    "Nous vous remercions de votre intérêt et vous encourageons à soumettre à nouveau votre candidature si vous pensez que vous pourriez être un bon ajout à notre équipe.\n\n" +
                    "Cordialement,\n" +
                    "L'équipe 4You";
            emailService.sendEmail(managerEmail, subject, text);

            return new ResponseEntity<>(poste, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Poste not found with ID: " + postId, HttpStatus.NOT_FOUND);
        }
    }
    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<?> deletePoste(@PathVariable Long postId) {
        Optional<Poste> optionalPoste = posteRepository.findById(postId);

        if (optionalPoste.isPresent()) {
            Poste poste = optionalPoste.get();
            posteRepository.delete(poste);

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "Poste supprime");
            return ResponseEntity.ok(responseBody);
        } else {
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("error", "Erreur le poste n est pas supprime");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);        }
    }
    @GetMapping("/getApprovedPostes")
    public ResponseEntity<List<Poste>> getApprovedPostes() {
        List<Poste> approvedPostes = posteRepository.findByApprouveParManagerRHTrue();

        System.out.println("Approved Postes:");
        for (Poste poste : approvedPostes) {
            System.out.println(poste);
        }

        return new ResponseEntity<>(approvedPostes, HttpStatus.OK);
    }

}
