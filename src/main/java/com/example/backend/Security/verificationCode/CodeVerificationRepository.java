package com.example.backend.Security.verificationCode;

import com.example.backend.Security.password.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeVerificationRepository extends JpaRepository<CodeVerification,Long> {
    CodeVerification findByToken(String token);

}