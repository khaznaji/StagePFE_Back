package com.example.backend.exception;

public class InvalidVerificationCodeException extends Exception {
    public InvalidVerificationCodeException(String message) {
        super(message);
    }
}