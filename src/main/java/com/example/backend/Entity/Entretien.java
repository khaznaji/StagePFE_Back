package com.example.backend.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Entretien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(name = "candidature_id")
    private Candidature candidature;
    private String dateEntretien; // Date de l'entretien
    private String heureDebut; // Heure de début de l'entretien
    private String heureFin;
    @ManyToOne
    @JoinColumn(name = "poste_id") // Colonne dans la table Entretien qui stockera l'ID du poste
    private Poste poste;
    private String roomId; // Identifiant de la salle de réunion
    private String commentaire; // Identifiant de la salle de réunion
    private int note; // Identifiant de la salle de réunion

}
