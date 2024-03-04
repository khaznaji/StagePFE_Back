package com.example.backend.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.*;
import java.util.List;


public enum Departement {
        Ressources_Humaines , Financier,  Informatique, Marketing , Production  ,
    Recherche_et_du_Développement ,Juridique , Opérations

}
