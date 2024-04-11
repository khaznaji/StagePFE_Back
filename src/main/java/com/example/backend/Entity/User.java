package com.example.backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public  class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;
    private String nom ;
    private String prenom ;
    private Integer numtel ;
    private String matricule ;
    @Enumerated(EnumType.STRING)
    private Role role;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private String email ;
    private String password ;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH-ss-mm")
    private LocalDateTime date = LocalDateTime.now();
    @Value("#{false}")
    private boolean isActivated;
    @OneToOne(mappedBy = "collaborateur", cascade = CascadeType.ALL)
    @JsonIgnore
    private Collaborateur collaborateur;    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL) // Un utilisateur peut avoir plusieurs entretiens
    @JsonIgnore
    private Set<EntretienRh> entretiens = new HashSet<>(); // Utilisez Set pour éviter les doublons

    @OneToOne(mappedBy = "manager" , cascade = CascadeType.ALL)
    @JsonIgnore
    private ManagerService managerService;
    private String image ;

    public Departement getDepartment() {
        // Ajoutez ici la logique pour récupérer le département de l'utilisateur.
        // Vous devrez peut-être ajuster cela en fonction de votre modèle de données.
        if (managerService != null) {
            return managerService.getDepartment();
        }
        return null; // ou lancez une exception, selon vos besoins.
    }


}
