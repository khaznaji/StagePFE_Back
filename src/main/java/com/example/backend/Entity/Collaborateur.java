package com.example.backend.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Collaborateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private ManagerService managerService;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User collaborateur;
    @Enumerated(EnumType.STRING)
    private Departement department;
    private String poste ;
    private String bio ;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateEntree;
    @ManyToMany
    @JoinTable(
            name = "collab_competence",
            joinColumns = @JoinColumn(name = "collab_id"),
            inverseJoinColumns = @JoinColumn(name = "competence_id")
    )
    private List<Competence> competences;
    private String image ;

}
