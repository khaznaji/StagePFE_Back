package com.example.backend.services;


import com.example.backend.Entity.Poste;
import com.example.backend.Repository.PosteRepository;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.CandidatureRepository;
import com.example.backend.Service.IPosteService;
import com.example.backend.Service.PosteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PosteServiceImplMock {

    @Mock
    private PosteRepository posteRepository;

    @Mock
    private CollaborateurRepository collaborateurRepository;

    @Mock
    private CandidatureRepository candidatureRepository;

    @InjectMocks
    private PosteService posteService;

    private Poste poste;

    @BeforeEach
    public void setUp() {
        poste = new Poste();
        // Initialize poste with necessary fields
    }

    @Test
    public void testCreatePoste() {
        // Given
        when(posteRepository.save(any(Poste.class))).thenReturn(poste);

        // When
        Poste savedPoste = posteService.createPoste(poste);

        // Then
        assertNotNull(savedPoste);
        verify(posteRepository, times(1)).save(poste);
    }

    @Test
    public void testGetPosteByIdFound() {
        // Given
        Long postId = 1L;
        when(posteRepository.findById(postId)).thenReturn(Optional.of(poste));

        // When
        Poste foundPoste = posteService.getPosteById(postId);

        // Then
        assertNotNull(foundPoste);
        assertEquals(poste, foundPoste);
        verify(posteRepository, times(1)).findById(postId);
    }

    @Test
    public void testGetPosteByIdNotFound() {
        // Given
        Long postId = 1L;
        when(posteRepository.findById(postId)).thenReturn(Optional.empty());

        // When & Then
        assertNull(posteService.getPosteById(postId));
        verify(posteRepository, times(1)).findById(postId);
    }
}
