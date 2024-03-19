package com.example.backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "collaborateur_id")
    @JsonIgnore
    private Collaborateur collaborateur;

    @ManyToOne
    @JoinColumn(name = "competence_id")
    @JsonIgnore
    private Competence competence;

    private int evaluation; // Vous pouvez définir le type de données en fonction de votre système d'évaluation (par exemple, une évaluation sur 5 ou sur 10)

    // Ajoutez d'autres propriétés si nécessaire

}
