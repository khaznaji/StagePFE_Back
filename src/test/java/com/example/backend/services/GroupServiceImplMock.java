package com.example.backend.services;
import com.example.backend.Entity.Groups;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.FormateurRepository;
import com.example.backend.Repository.FormationRepository;
import com.example.backend.Repository.GroupsRespository;
import com.example.backend.Service.GroupeService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@Slf4j
public class GroupServiceImplMock {


    @Mock
    private GroupsRespository groupsRepository;

    @Mock
    private FormateurRepository formateurRepository;

    @Mock
    private FormationRepository formationRepository;

    @Mock
    private CollaborateurRepository collaborateurRepository;

    @InjectMocks
    private GroupeService groupeService;

    private Groups groupe;

    @BeforeEach
    public void setup() {
        groupe = new Groups();
        groupe.setId(1L);
        groupe.setNom("Test Groupe");
    }

    @Test
    public void testCreateGroupe() {
        // Arrange
        when(groupsRepository.save(groupe)).thenReturn(groupe);

        // Act
        Groups result = groupeService.createGroupe(groupe);

        // Assert
        assertEquals(groupe, result);
    }

    @Test
    public void testGetAllGroupes() {
        // Arrange
        List<Groups> groupsList = new ArrayList<>();
        groupsList.add(groupe);
        when(groupsRepository.findAll()).thenReturn(groupsList);

        // Act
        List<Groups> result = groupeService.getAllGroupes();

        // Assert
        assertEquals(1, result.size());
        assertEquals(groupe, result.get(0));
    }
    @Test
    public void testGetGroupeById() {
        // Arrange
        when(groupsRepository.findById(1L)).thenReturn(Optional.of(groupe));

        // Act
        Optional<Groups> result = groupeService.getGroupeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(groupe, result.get());
    }

    @Test
    public void testUpdateGroupe() {
        // Arrange
        when(groupsRepository.save(groupe)).thenReturn(groupe);

        // Act
        Groups result = groupeService.updateGroupe(groupe);

        // Assert
        assertEquals(groupe, result);
    }

    @Test
    public void testDeleteGroupe() {
        // Arrange
        doNothing().when(groupsRepository).deleteById(1L);

        // Act
        groupeService.deleteGroupe(1L);

        // Assert
        // No assertion needed as delete operation does not return any value
        verify(groupsRepository, times(1)).deleteById(1L);
    }
}