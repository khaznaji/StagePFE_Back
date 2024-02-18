package com.example.backend.Security.verificationCode;
import com.example.backend.Entity.User;
import lombok.Getter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

@Entity
@Getter
public class CodeVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private String activationCode;
    @Column(nullable = false)
    private Date expiryDate;
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;
    public CodeVerification(String token, User user, String activationCode ,int expirationInMinutes) {
        this.token = token;
        this.user = user;
        this.activationCode = activationCode;

        this.expiryDate = calculateExpiryDate(expirationInMinutes);
    }

    public CodeVerification() {

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
