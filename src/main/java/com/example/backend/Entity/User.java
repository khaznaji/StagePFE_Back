package com.example.backend.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User {
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
    private String image ;
    private String password ;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH-ss-mm")
    private LocalDateTime date = LocalDateTime.now();
    @Value("#{false}")
    private boolean isActivated;


}
