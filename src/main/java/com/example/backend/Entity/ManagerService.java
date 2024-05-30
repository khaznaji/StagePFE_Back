    package com.example.backend.Entity;

    import com.fasterxml.jackson.annotation.JsonIgnore;
    import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
        @OneToOne()
        @JoinColumn(name = "manager_id")
        @JsonIgnore
        private User manager;
        @Enumerated(EnumType.STRING)
        private Departement department;
        private String poste ;
        private String bio ;
        private String dateEntree;

        @ManyToMany
        @JoinTable(
                name = "manager_competence",
                joinColumns = @JoinColumn(name = "manager_id"),
                inverseJoinColumns = @JoinColumn(name = "competence_id")
        )
@JsonIgnore
        private List<Competence> competences;

        @OneToMany(mappedBy = "managerService")
        @JsonIgnore
        private List<Collaborateur> collaborateurs;
        @OneToMany(mappedBy = "managerService")
        @JsonIgnore
        private List<Poste> postes;
        @OneToMany(mappedBy = "managerService")
        private List<BilanAnnuel> bilansAnnuels;
        @OneToMany(mappedBy = "managerService")
        @JsonIgnore

        private List<Entretien> entretiens;
        @Transient
        public long getNombreCollaborateurs() {
            return collaborateurs != null ? collaborateurs.size() : 0;
        }

        @Transient
        public long getNombrePostesPublies() {
            return postes != null ? postes.stream().filter(poste -> poste.getPoste() == EtatPoste.Publie).count() : 0;
        }

        @Transient
        public long getNombreDemandesFormation() {
            return collaborateurs != null ? collaborateurs.stream()
                    .mapToLong(collaborateur -> collaborateur.getParticipations() != null ? collaborateur.getParticipations().size() : 0)
                    .sum() : 0;
        }

        @Transient
        public long getNombrePostesApprouves() {
            return postes != null ? postes.stream().filter(poste -> poste.getPoste() == EtatPoste.Accepte).count() : 0;
        }
    }