package com.example.backend.exception;

public class MatriculeAlreadyExistsException extends RuntimeException {
    public MatriculeAlreadyExistsException() {
        super("Ce matricule est déjà utilisé. Veuillez choisir un autre matricule.");
    }
}
