package com.example.backend.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationFormation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "collaborateur_id")
    private Collaborateur collaborateur;

    @ManyToOne
    @JoinColumn(name = "formation_id")
    private Formation formation;

    @Enumerated(EnumType.STRING)
    private EtatParticipation etat;

    // Autres champs et méthodes
}
