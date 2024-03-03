package com.example.backend.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


public enum Domaine {
    Informatique_et_Technologie, Business_et_Gestion, Finance_et_comptabilité, Ressources_humaines,
    Marketing_et_Communication, Opérations_et_logistique, Consultation_et_conseil
}

