package com.example.backend.Service;

import com.example.backend.Configuration.MailConfig;
import com.example.backend.Entity.*;
import com.example.backend.Repository.CandidatureRepository;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.ManagerServiceRepository;
import com.example.backend.Repository.PosteRepository;
import com.example.backend.exception.CandidatureNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class CandidatureService {

    private final CandidatureRepository candidatureRepository;
    @Autowired
    private  CollaborateurRepository collaborateurRepository;
    @Autowired
    PosteRepository posteRepository;
    @Autowired
    ManagerServiceRepository managerServiceRepository;
    @Autowired
    MailConfig emailService ;

    @Autowired
    public CandidatureService(CandidatureRepository candidatureRepository) {
        this.candidatureRepository = candidatureRepository;
    }
    public Candidature getCandidature(Long candidatureId) {
        return candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new CandidatureNotFoundException("Candidature not found with id: " + candidatureId));
    }
    public Candidature modifierEtat(Long collaborateurId, Long posteId, EtatPostulation nouvelEtat) {
        Candidature candidature = candidatureRepository.findByCollaborateurIdAndPosteId(collaborateurId, posteId);
        if (candidature != null) {
            candidature.setEtat(nouvelEtat);
            return candidatureRepository.save(candidature);
        }
        // Gérer le cas où la candidature n'est pas trouvée
        throw new CandidatureNotFoundException("Candidature not found for collaborateurId: " + collaborateurId + " and posteId: " + posteId);
    }
    public List<Candidature> getCandidaturesByPost(Long postId) {
        return candidatureRepository.findByPosteId(postId);
    }



public void updateCandidaturesToEnAttente(List<Long> candidatureIds) {
      for (Long candidatureId : candidatureIds) {
          Candidature candidature = candidatureRepository.findById(candidatureId)
                  .orElseThrow(() -> new RuntimeException("Candidature not found with id: " + candidatureId));

          candidature.setEtat(EtatPostulation.EN_ATTENTE_ENTRETIEN_RH);
          candidatureRepository.save(candidature);
      }
  }


 /* @Transactional
  public void accepterCandidatures(List<Long> candidatureIds) {
      for (Long candidatureId : candidatureIds) {
          Candidature candidature = candidatureRepository.findById(candidatureId)
                  .orElseThrow(() -> new IllegalArgumentException("Candidature non trouvée avec l'ID : " + candidatureId));

          candidature.setEtat(EtatPostulation.ACCEPTEE);

          Poste poste = candidature.getPoste();
          if (poste != null) {
              Collaborateur collaborateur = candidature.getCollaborateur();
              if (collaborateur != null) {
                  collaborateur.setPoste(poste.getTitre());
                  collaborateur.setManagerService(poste.getManagerService());
                  collaborateurRepository.save(collaborateur);

                  User collaborateurUser = collaborateur.getCollaborateur();
                  if (collaborateurUser != null) {
                      emailService.sendAcceptanceEmail(collaborateurUser.getEmail(), poste.getTitre());
                  } else {
                      throw new IllegalArgumentException("Collaborateur's user is null for candidature ID: " + candidatureId);
                  }
              } else {
                  throw new IllegalArgumentException("Collaborateur is null for candidature ID: " + candidatureId);
              }
          } else {
              throw new IllegalArgumentException("Poste is null for candidature ID: " + candidatureId);
          }
          candidatureRepository.save(candidature);
      }
  }*/
 @Transactional
 public void accepterCandidatures(List<Long> candidatureIds) {
     for (Long candidatureId : candidatureIds) {
         Candidature candidature = candidatureRepository.findById(candidatureId)
                 .orElseThrow(() -> new CandidatureNotFoundException("Candidature not found with id: " + candidatureId));

         candidature.setEtat(EtatPostulation.ACCEPTEE);

         Poste poste = candidature.getPoste();
         if (poste != null) {
             Collaborateur collaborateur = candidature.getCollaborateur();
             if (collaborateur != null) {
                 collaborateur.setPoste(poste.getTitre());
                 collaborateur.setManagerService(poste.getManagerService());
                 collaborateurRepository.save(collaborateur);
                 User collaborateurUser = collaborateur.getCollaborateur();
                 if (collaborateurUser != null) {
                     emailService.sendAcceptanceEmail(collaborateurUser.getEmail(), poste.getTitre());
                 } else {
                     throw new IllegalArgumentException("Collaborateur's user is null for candidature ID: " + candidatureId);
                 }
             } else {
                 throw new IllegalArgumentException("Collaborateur is null for candidature ID: " + candidatureId);
             }
         } else {
             throw new IllegalArgumentException("Poste is null for candidature ID: " + candidatureId);
         }
         candidatureRepository.save(candidature);
     }
 }



}
