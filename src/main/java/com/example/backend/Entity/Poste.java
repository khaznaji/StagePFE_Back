package com.example.backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private boolean archive;
    private boolean encours;

    @ManyToMany
    @JoinTable(
            name = "poste_competence",
            joinColumns = @JoinColumn(name = "poste_id"),
            inverseJoinColumns = @JoinColumn(name = "competence_id"))
    private List<Competence> competences;
    @Enumerated(EnumType.STRING)
    private Departement departement;
    private String description;
    private int nombrePostesDisponibles;
    private LocalDate dateCreation;
    @ManyToMany
    @JoinTable(
            name = "poste_candidat", // Nom de la table d'association
            joinColumns = @JoinColumn(name = "poste_id"),
            inverseJoinColumns = @JoinColumn(name = "collaborateur_id"))
    @JsonIgnore
    private List<Collaborateur> candidats;


    @Transient
    @JsonProperty("managerNom")
    public String getManagerNom() {
        return managerService != null ? managerService.getManager().getNom() : null;
    }

    @Transient
    @JsonProperty("managerPrenom")
    public String getManagerPrenom() {
        return managerService != null ? managerService.getManager().getPrenom() : null;
    }
    @Transient
    @JsonProperty("image")
    public String getImage() {
        return managerService != null ? managerService.getManager().getImage() : null;
    }
}
