package com.example.backend.Service;

import com.example.backend.Entity.Certificat;
import com.example.backend.Entity.Collaborateur;
import com.example.backend.Entity.Groups;
import com.example.backend.Repository.CertificatRepository;
import com.example.backend.Repository.GroupsRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class CertificatService {
    @Autowired
    private CertificatRepository certificatRepository;
    @Autowired
    private GroupsRespository groupsRepository;
    public boolean checkIfCertificatesGenerated(Long groupId) {
        System.out.println("Checking certificates for group: " + groupId);
        long certificateCount = certificatRepository.countByCollaborateur_Groups_Id(groupId);
        System.out.println("Certificate count for group " + groupId + ": " + certificateCount);

        return certificateCount > 0;
    }
    public List<Certificat> getCertificatByGroupId(Long groupId) {
        Groups group = groupsRepository.findById(groupId).orElse(null);
        if (group != null) {
            List<Collaborateur> users = group.getCollaborateurs();
            if (!users.isEmpty()) {
                return certificatRepository.findByCollaborateur(users.get(0)); // Assuming you want the first user's certificate
            }

        }
        return null;
    }
}
