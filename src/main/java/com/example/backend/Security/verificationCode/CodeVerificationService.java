package com.example.backend.Security.verificationCode;

import com.example.backend.Entity.User;

public interface CodeVerificationService {
    CodeVerification findByToken(String token) ;
    CodeVerification createToken(User user) ;
    void deleteToken(CodeVerification token);
}
