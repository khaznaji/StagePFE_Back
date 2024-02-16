package com.example.backend.Security.password;

import com.example.backend.Entity.User;

public interface PasswordResetTokenService {
    PasswordResetToken createToken(User user);
    PasswordResetToken findByToken(String token);
    void deleteToken(PasswordResetToken token);
}