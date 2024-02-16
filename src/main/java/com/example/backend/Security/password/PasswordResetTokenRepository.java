package com.example.backend.Security.password;

import com.example.backend.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository  extends JpaRepository<PasswordResetToken,Long> {
    PasswordResetToken findByToken(String token);

}
