package com.example.backend.Repository;

import com.example.backend.Entity.Certificat;
import com.example.backend.Entity.Collaborateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CertificatRepository extends JpaRepository<Certificat,Long> {
    long countByCollaborateur_Groups_Id(Long groupId);
    List<Certificat> findByCollaborateur(Collaborateur group);


}
