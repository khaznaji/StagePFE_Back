package com.example.backend.services;

import com.example.backend.Entity.Formateur;
import com.example.backend.Repository.FormateurRepository;
import com.example.backend.Service.FormateurService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class FormateurServiceImplMock {

    @Mock
    private FormateurRepository formateurRepository;

    @InjectMocks
    private FormateurService formateurService;

    private Formateur formateur;

    @BeforeEach
    public void setUp() {
        formateur = new Formateur();
        formateur.setId(1L); // Assuming Formateur has an ID field
        // Set other properties of formateur as needed
    }

    @Test
    public void testFindById() {
        // Given
        when(formateurRepository.findById(1L)).thenReturn(Optional.of(formateur));

        // When
        Formateur foundFormateur = formateurService.findById(1L);

        // Then
        assertNotNull(foundFormateur);
        assertEquals(formateur.getId(), foundFormateur.getId());
        verify(formateurRepository, times(1)).findById(1L);
    }
}