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
public class Formateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;
    @OneToOne()
    @JoinColumn(name = "formateur_id")
    @JsonIgnore
    private User fomarteur;
    private String specialite ;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateEntree;
    @OneToOne()
    @JsonIgnore
    private Formation formation;
}
