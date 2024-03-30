package com.example.backend.exception;

public class CandidatureNotFoundException extends RuntimeException {
    public CandidatureNotFoundException(String message) {
        super(message);
    }
}
