package com.example.backend.Configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

@Service
public class MailConfig {

    @Autowired
    private JavaMailSender javaMailSender;

    public boolean sendActivationEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            javaMailSender.send(message);

            // Retournez true si l'e-mail a été envoyé avec succès
            return true;
        } catch (Exception e) {
            // Gérez l'exception (enregistrez-la, journalisez-la, etc.) si l'e-mail n'a pas pu être envoyé
            return false;
        }
    }
    public void sendPasswordResetEmail(String to, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Reset");
        message.setText("Click the link to reset your password: " + resetLink);

        javaMailSender.send(message);
    }

}

