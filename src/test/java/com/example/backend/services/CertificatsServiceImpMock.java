    package com.example.backend.services;
    import com.example.backend.Entity.Certificat;
    import com.example.backend.Entity.Collaborateur;
    import com.example.backend.Entity.Groups;
    import com.example.backend.Repository.CertificatRepository;
    import com.example.backend.Repository.GroupsRespository;
    import com.example.backend.Service.CertificatService;
    import lombok.extern.slf4j.Slf4j;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    import org.junit.jupiter.api.extension.ExtendWith;
    import org.mockito.InjectMocks;
    import org.mockito.Mock;
    import org.mockito.junit.jupiter.MockitoExtension;

    import java.util.ArrayList;
    import java.util.List;
    import java.util.Optional;

    import static com.jayway.jsonpath.internal.path.PathCompiler.fail;
    import static org.junit.jupiter.api.Assertions.assertEquals;
    import static org.junit.jupiter.api.Assertions.assertNotNull;
    import static org.mockito.ArgumentMatchers.any;
    import static org.mockito.Mockito.when;

    @ExtendWith(MockitoExtension.class)
    @Slf4j
    public class CertificatsServiceImpMock {
        @Mock
        private CertificatRepository certificatRepository;

        @Mock
        private GroupsRespository groupsRepository;

        @InjectMocks
        private CertificatService certificatService;

        private Groups group;
        private Collaborateur collaborateur;
        private Certificat certificat;

        @BeforeEach
        public void setup() {
            group = new Groups();
            group.setId(1L);

            collaborateur = new Collaborateur();
            collaborateur.setId(1L);

            certificat = new Certificat();
            certificat.setIdCertificat(1L);
            certificat.setCollaborateur(collaborateur);
        }

        @Test
        public void testCheckIfCertificatesGenerated() {
            // Arrange
            when(certificatRepository.countByCollaborateur_Groups_Id(1L)).thenReturn(1L);

            // Act
            boolean result = certificatService.checkIfCertificatesGenerated(1L);

            // Assert
            assertEquals(true, result);
        }
        @Test
        public void testGetCertificatByGroupId() {
            // Création d'un groupe simulé
            Groups group = new Groups();
            group.setId(1L); // Set group id
            Collaborateur collaborateur = new Collaborateur(); // Création d'un collaborateur simulé
            List<Collaborateur> users = new ArrayList<>();
            users.add(collaborateur);
            group.setCollaborateurs(users);

            // Définition du comportement simulé du repository pour retourner le groupe simulé lorsqu'on recherche par ID
            when(groupsRepository.findById(1L)).thenReturn(java.util.Optional.of(group));

            // Création de certificats simulés
            Certificat certificat1 = new Certificat();
            Certificat certificat2 = new Certificat();
            List<Certificat> certificats = new ArrayList<>();
            certificats.add(certificat1);
            certificats.add(certificat2);

            // Définition du comportement simulé du repository pour retourner les certificats simulés lorsqu'on recherche par collaborateur
            when(certificatRepository.findByCollaborateur(collaborateur)).thenReturn(certificats);

            // Appel de la méthode à tester
            List<Certificat> result = certificatService.getCertificatByGroupId(1L);

            // Vérification du résultat
            assertNotNull(result);
            assertEquals(2, result.size());
        }

    }
