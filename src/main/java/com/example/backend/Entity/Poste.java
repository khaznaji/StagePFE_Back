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
import java.util.LinkedHashSet;
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
    @Enumerated(EnumType.STRING)
    private EtatPoste poste;
    @OneToMany(mappedBy = "poste")
    @JsonIgnore
    private List<Candidature> candidatures;

    @OneToMany(mappedBy = "poste",cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Quiz> quizzes;
    @OneToMany(mappedBy = "poste", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Entretien> entretiens;

    @Transient
    @JsonProperty("managerNom")
    public String getManagerNom() {
        if (managerService != null && managerService.getManager() != null) {
            return managerService.getManager().getNom();
        }
        return null; // or return a default value if appropriate
    }

    @Transient
    @JsonProperty("managerPrenom")
    public String getManagerPrenom() {
        if (managerService != null && managerService.getManager() != null) {
            return managerService.getManager().getPrenom();
        }
        return null; // or return a default value if appropriate
    }

    @Transient
    @JsonProperty("image")
    public String getImage() {
        if (managerService != null && managerService.getManager() != null) {
            return managerService.getManager().getImage();
        }
        return null; // or return a default image path if appropriate
    }
}
