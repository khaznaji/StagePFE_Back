    package com.example.backend.Entity;
    import com.fasterxml.jackson.annotation.JsonIgnore;
    import lombok.*;
    import javax.persistence.*;
    import java.util.List;

    @Entity
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class Groups {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "formateur_id")
        @JsonIgnore
        private Formateur formateur;

        @ManyToMany
        @JoinTable(
                name = "group_collaborator",
                joinColumns = @JoinColumn(name = "group_id"),
                inverseJoinColumns = @JoinColumn(name = "collaborator_id"))
        @JsonIgnore
        private List<Collaborateur> collaborateurs;

        private String nom ;
        @ManyToOne
        @JoinColumn(name = "formation_id")
        private Formation formation;
        private boolean certificatesGenerated;
        @Enumerated(EnumType.STRING)
        private Etat etat;

    }
