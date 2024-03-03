package com.example.backend.Entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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