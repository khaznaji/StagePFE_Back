package com.example.backend.Entity;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

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
public class ManagerService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;
    @Enumerated(EnumType.STRING)
    private Departement department;
    private String poste ;
    private String bio ;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateEntree;
    private String image ;

    @ManyToMany
    @JoinTable(
            name = "manager_competence",
            joinColumns = @JoinColumn(name = "manager_id"),
            inverseJoinColumns = @JoinColumn(name = "competence_id")
    )
    private List<Competence> competences;

    @OneToMany(mappedBy = "managerService")
    private List<Collaborateur> collaborateurs;
}