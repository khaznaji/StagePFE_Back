package com.example.backend.Security.verificationCode;

import com.example.backend.Security.password.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CodeVerificationRepository extends JpaRepository<CodeVerification,Long> {
    CodeVerification findByToken(String token);
    @Modifying
    @Query("DELETE FROM CodeVerification c WHERE c.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}