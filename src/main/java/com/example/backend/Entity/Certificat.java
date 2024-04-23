package com.example.backend.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class Certificat {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long idCertificat;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime date = LocalDateTime.now();
    @ManyToOne
    @JoinColumn(name = "collaborateur_id")
    private Collaborateur collaborateur;
    private String periode;
    private String month;
    private String path;
    private Long userOrGroupId;

}
