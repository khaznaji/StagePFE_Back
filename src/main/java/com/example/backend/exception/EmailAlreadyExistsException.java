package com.example.backend.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException() {
        super("Cet e-mail est déjà utilisé. Veuillez choisir un autre e-mail.");
    }
}
