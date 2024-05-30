package com.example.backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Formateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;
    @OneToOne
    @JoinColumn(name = "user_id") // Nommez cette colonne selon votre structure de base de données
    @JsonIgnore
    private User formateur;
    private String specialite ;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateEntree;
    @OneToMany(mappedBy = "formateur")
    @JsonIgnore
    private List<Formation> formation;
    @OneToMany(mappedBy = "formateur")
    @JsonIgnore
    private List<Groups> groups;
}
