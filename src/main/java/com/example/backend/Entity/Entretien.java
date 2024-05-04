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
public class Entretien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(name = "candidature_id")
    private Candidature candidature;
    private String dateEntretien;
    private String heureDebut;
    private String heureFin;
    @ManyToOne
    @JoinColumn(name = "poste_id")
    private Poste poste;
    private String roomId;
    private String commentaire;
    private int note;
    @Enumerated(EnumType.STRING)
    private EtatEntretien etatEntretien;
    @Enumerated(EnumType.STRING)
    private TypeEntretien typeEntretien;
    @ManyToOne
    @JoinColumn(name = "manager_service_id")
    @JsonIgnore
    private ManagerService managerService;
    @ManyToOne
    @JoinColumn(name = "collaborateur_id")
    @JsonIgnore
    private Collaborateur collaborateurs;
}
