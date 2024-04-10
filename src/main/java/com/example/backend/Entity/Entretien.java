package com.example.backend.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;

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
}
