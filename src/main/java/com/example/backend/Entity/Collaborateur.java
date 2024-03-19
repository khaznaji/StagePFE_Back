package com.example.backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Collaborateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;
    @ManyToOne()
    @JoinColumn(name = "manager_id")
    @JsonIgnore
    private ManagerService managerService;
    @OneToOne()
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User collaborateur;
    @Enumerated(EnumType.STRING)
    private Departement department;
    private String poste ;
    private String bio ;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateEntree;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "collab_competence",
            joinColumns = @JoinColumn(name = "collab_id"),
            inverseJoinColumns = @JoinColumn(name = "competence_id")
    )
    @JsonIgnore
    private List<Competence> competences;
    @OneToMany(mappedBy = "collaborateur")
    @JsonIgnore
    private List<Candidature> candidatures;
    @OneToMany(mappedBy = "collaborateur", fetch = FetchType.LAZY)
    @JsonIgnore

    private List<Evaluation> evaluations;

    private String resume ;

}
