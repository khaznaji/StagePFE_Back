package com.example.backend.Repository;

import com.example.backend.Entity.Candidature;
import com.example.backend.Entity.Collaborateur;
import com.example.backend.Entity.EtatPostulation;
import com.example.backend.Entity.Poste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface CandidatureRepository extends JpaRepository<Candidature,Long> {
    boolean existsByCollaborateurAndPoste(Collaborateur collaborateur, Poste poste);
    List<Candidature> findByPoste(Poste poste);
    Long countByPosteAndEtat(Poste poste, EtatPostulation etat);
    List<Candidature> findByPoste_Id(Long posteId);
    List<Candidature> findByPosteId(Long posteId);

  /*  @Query("SELECT c.dateEntretien FROM Candidature c WHERE c.poste.id = :posteId AND c.dateEntretien IS NOT NULL")
    List<LocalDateTime> findCandidatureDatesByPosteId(Long posteId);
    @Query("SELECT c.dateEntretien,c.dateFinEntretien, c.collaborateur.collaborateur.nom, c.collaborateur.collaborateur.prenom FROM Candidature c WHERE c.poste.id = :posteId AND c.dateEntretien IS NOT NULL")
    List<Object[]> findEntretienDatesAndCollaborateurInfoByPosteId(Long posteId);
*/
    Long countByPosteAndEtatIsNot(Poste poste, EtatPostulation etat);

    Long countByPoste(Poste poste);
    Candidature findByCollaborateurIdAndPosteId(Long collaborateurId, Long posteId);

}
