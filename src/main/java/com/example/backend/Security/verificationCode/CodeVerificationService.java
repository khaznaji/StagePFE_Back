package com.example.backend.Security.verificationCode;

import com.example.backend.Entity.User;

import java.util.UUID;

public interface CodeVerificationService {
    CodeVerification findByToken(String token) ;
    CodeVerification createToken(User user) ;
    void deleteToken(CodeVerification token);
     CodeVerification regenerateToken(String oldToken);}
