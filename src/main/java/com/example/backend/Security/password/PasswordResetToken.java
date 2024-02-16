package com.example.backend.Security.password;

import com.example.backend.Entity.User;
import lombok.Getter;

import javax.persistence.*;

import javax.persistence.Entity;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

// PasswordResetToken.java
@Entity
@Getter
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Column(nullable = false)
    private Date expiryDate;

    // Getters and setters

    // Constructeur par défaut
    public PasswordResetToken() {}

    // Constructeur avec utilisateur et durée d'expiration
    public PasswordResetToken(String token, User user, int expirationInMinutes) {
        this.token = token;
        this.user = user;
        this.expiryDate = calculateExpiryDate(expirationInMinutes);
    }

    private Date calculateExpiryDate(int expirationInMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MINUTE, expirationInMinutes);
        return new Date(cal.getTime().getTime());
    }

    public boolean isExpired() {
        return new Date().after(this.expiryDate);
    }
}
