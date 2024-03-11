package com.example.backend.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Poste {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;
    private String titre;
    @ManyToOne
    @JoinColumn(name = "manager_service_id")
    private ManagerService managerService;
    private boolean approuveParManagerRH;
    @ManyToMany
    @JoinTable(
            name = "poste_competence",
            joinColumns = @JoinColumn(name = "poste_id"),
            inverseJoinColumns = @JoinColumn(name = "competence_id"))
    private List<Competence> competences;
    private Departement departement;
    private String description;
    private int nombrePostesDisponibles;
    private LocalDate dateCreation;
}
