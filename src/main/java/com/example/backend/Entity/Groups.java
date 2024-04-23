    package com.example.backend.Entity;
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
        private Formateur formateur;

        @ManyToMany
        @JoinTable(
                name = "group_collaborator",
                joinColumns = @JoinColumn(name = "group_id"),
                inverseJoinColumns = @JoinColumn(name = "collaborator_id"))
        private List<Collaborateur> collaborateurs;

        private String nom ;
        @ManyToOne
        @JoinColumn(name = "formation_id")
        private Formation formation;
        private boolean certificatesGenerated;

    }
