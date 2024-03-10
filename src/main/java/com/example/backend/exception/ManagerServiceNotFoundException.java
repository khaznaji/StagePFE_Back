package com.example.backend.exception;

public class ManagerServiceNotFoundException extends RuntimeException {
    public ManagerServiceNotFoundException(String message) {
        super(message);
    }
}
