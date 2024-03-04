package com.example.backend.Entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManagerService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;
}