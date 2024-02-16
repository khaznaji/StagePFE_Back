package com.example.backend.Security.password;

import com.example.backend.Entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

// PasswordResetTokenServiceImpl.java
@Service
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Override
    public PasswordResetToken createToken(User user) {
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user, 15); // Durée d'expiration en minutes (modifiable)
        return passwordResetTokenRepository.save(resetToken);
    }

    @Override
    public PasswordResetToken findByToken(String token) {
        return passwordResetTokenRepository.findByToken(token);
    }

    @Override
    public void deleteToken(PasswordResetToken token) {
        passwordResetTokenRepository.delete(token);
    }
}
