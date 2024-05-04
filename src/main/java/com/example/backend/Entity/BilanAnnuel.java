package com.example.backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BilanAnnuel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "manager_id")
    @JsonIgnore
    private ManagerService managerService;
    @ManyToOne
    @JoinColumn(name = "collaborateur_id")
    @JsonIgnore
    private Collaborateur collaborateur;
    private String objectifAtteints ;
    private String objectifsFuturs;
    private String projetsAccomplis;
    private String challenges;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateBilan;
    @Enumerated(EnumType.STRING)
    private Etat etatBilan;

}
