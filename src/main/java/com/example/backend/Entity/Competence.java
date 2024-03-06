package com.example.backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Competence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    @Enumerated(EnumType.STRING)
    private Domaine domaine;

    @ManyToMany(mappedBy = "competences")
    @JsonIgnore
    private List<ManagerService> managers ;

    @ManyToMany(mappedBy = "competences")
    @JsonIgnore
    private List<Collaborateur> collaborateurs ;

}
