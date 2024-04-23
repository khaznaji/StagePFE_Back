package com.example.backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class    Formation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;
    private String title ;
    private String description ;
    private String image ;
    private int chapitre ;
    private int duree ;
    private boolean disponibilite ;
    @Enumerated(EnumType.STRING)
    private Departement department;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @OneToOne()
    @JoinColumn(name = "formateur_id")
    @JsonIgnore
    private Formateur formateur;
    @Transient // Indique à JPA de ne pas mapper ce champ à la base de données
    private String formateurName;
    @Transient // Indique à JPA de ne pas mapper ce champ à la base de données
    private String formateurImage;
    @OneToMany(mappedBy = "formation")
    @JsonIgnore
    private List<ParticipationFormation> participations;


}
