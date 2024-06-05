package com.example.backend.Service;

import com.example.backend.Entity.BilanAnnuel;
import com.example.backend.Entity.Collaborateur;
import com.example.backend.Entity.Etat;
import com.example.backend.Entity.ManagerService;
import com.example.backend.Repository.BilanAnnuelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;

@Service
public class BilanAnnuelService {
    @Autowired
    private BilanAnnuelRepository bilanAnnuelRepository;
    public void envoyerBilanAnnuel(ManagerService managerService) {
        List<Collaborateur> collaborateurs = managerService.getCollaborateurs();

        // Imprimer la liste des Collaborateur avant de les traiter
        System.out.println("Liste des Collaborateur auxquels le BilanAnnuel sera envoyé :");
        for (Collaborateur collaborateur : collaborateurs) {
            // Supposons que Collaborateur a une méthode getNom() pour obtenir le nom du collaborateur
            System.out.println(collaborateur.getCollaborateur().getNom());
        }

        for (Collaborateur collaborateur : collaborateurs) {
            BilanAnnuel bilanAnnuel = new BilanAnnuel();
            bilanAnnuel.setManagerService(managerService);
            bilanAnnuel.setCollaborateur(collaborateur);
            bilanAnnuel.setDateBilan(LocalDate.now());
            bilanAnnuel.setEtatBilan(Etat.En_Attente);

            bilanAnnuelRepository.save(bilanAnnuel);
        }

        // Imprimer un message de confirmation après avoir envoyé le BilanAnnuel
        System.out.println("Bilan annuel envoyé avec succès à tous les Collaborateur.");
    }

    public BilanAnnuel mettreAJourBilanAnnuel(Long bilanAnnuelId, BilanAnnuel bilanAnnuelMiseAJour) {
        BilanAnnuel bilanAnnuelExistant = bilanAnnuelRepository.findById(bilanAnnuelId)
                .orElseThrow(() -> new EntityNotFoundException("BilanAnnuel non trouvé avec l'ID : " + bilanAnnuelId));

        // Mettre à jour les champs spécifiques ici. Par exemple :
        bilanAnnuelExistant.setObjectifAtteints(bilanAnnuelMiseAJour.getObjectifAtteints());
        bilanAnnuelExistant.setObjectifsFuturs(bilanAnnuelMiseAJour.getObjectifsFuturs());
        bilanAnnuelExistant.setProjetsAccomplis(bilanAnnuelMiseAJour.getProjetsAccomplis());
        bilanAnnuelExistant.setChallenges(bilanAnnuelMiseAJour.getChallenges());
        bilanAnnuelExistant.setEtatBilan(Etat.Sauvegarde);

        return bilanAnnuelRepository.save(bilanAnnuelExistant);
    }
    public BilanAnnuel mettreAJourBilanAnnuelEtEnvoye(Long bilanAnnuelId, BilanAnnuel bilanAnnuelMiseAJour) {
        BilanAnnuel bilanAnnuelExistant = bilanAnnuelRepository.findById(bilanAnnuelId)
                .orElseThrow(() -> new EntityNotFoundException("BilanAnnuel non trouvé avec l'ID : " + bilanAnnuelId));

        // Mettre à jour les champs spécifiques ici. Par exemple :
        bilanAnnuelExistant.setObjectifAtteints(bilanAnnuelMiseAJour.getObjectifAtteints());
        bilanAnnuelExistant.setObjectifsFuturs(bilanAnnuelMiseAJour.getObjectifsFuturs());
        bilanAnnuelExistant.setProjetsAccomplis(bilanAnnuelMiseAJour.getProjetsAccomplis());
        bilanAnnuelExistant.setChallenges(bilanAnnuelMiseAJour.getChallenges());
        bilanAnnuelExistant.setEtatBilan(Etat.Envoye);

        return bilanAnnuelRepository.save(bilanAnnuelExistant);
    }
    public List<BilanAnnuel> getBilansAnnuelByCollaborateur(Collaborateur collaborateur) {
        return bilanAnnuelRepository.findByCollaborateur(collaborateur);
    }
    public BilanAnnuel getBilanById(Long bilanAnnuelId) {
        return bilanAnnuelRepository.findById(bilanAnnuelId)
                .orElse(null);
    }
    public List<BilanAnnuel> getBilansByCollaborateurId(Long collaborateurId) {
        return bilanAnnuelRepository.findByCollaborateurId(collaborateurId);
    }


}
