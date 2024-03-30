package com.example.backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Candidature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "poste_id")
    private Poste poste;

    @ManyToOne
    @JoinColumn(name = "collaborateur_id")
    @JsonIgnore
    private Collaborateur collaborateur;

    @Enumerated(EnumType.STRING)
    private EtatPostulation etat;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateCandidature;
    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;
    private int score; // Ajout de l'attribut score

}
