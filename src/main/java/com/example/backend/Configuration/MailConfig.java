package com.example.backend.Configuration;

import com.example.backend.Entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

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
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

        try {
            // Configure the email
            helper.setTo(to);
            helper.setSubject("Réinitialisation de mot de passe - 4You");

            // You can customize the email body here
            String emailContent = "<p>Bonjour,</p>"
                    + "<p>Vous avez demandé la réinitialisation de votre mot de passe.</p>"
                    + "<p>Cliquez sur le lien ci-dessous pour procéder à la réinitialisation :</p>"
                    + "<a href=\"" + resetLink + "\">Réinitialiser le mot de passe</a>"
                    + "<p>Si vous n'avez pas demandé cette réinitialisation, veuillez ignorer cet email.</p>"
                    + "<p>Cordialement,<br/>Equipe 4You</p>";

            helper.setText(emailContent, true);

            // Send the email
            javaMailSender.send(message);
        } catch (MessagingException e) {
            // Handle exception if needed
            e.printStackTrace();
        }
    }

    public void sendVerificationCodeByEmail(String email, String resetLink, String verificationCode) {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

        try {
            helper.setTo(email);
            helper.setSubject("Code de vérification d'inscription - 4You");
            String htmlContent = "<p>Bonjour,</p>"
                    + "<p>Merci de vous être inscrit! Veuillez cliquer sur le lien ci-dessous pour activer votre compte :</p>"
                    + "<p><a href='" + resetLink + "'>" + resetLink + "</a></p>"
                    + "<p>Votre code de vérification est : <strong>" + verificationCode + "</strong></p>"
                    + "<p>Cordialement,</p>"
                    + "<p>Equipe 4You</p>";

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            // Gérer l'exception selon les besoins
            e.printStackTrace();
        }
    }
    public void sendWelcomeEmail(String email, String username , String resetLink, String verificationCode) {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

        try {
            helper.setTo(email);
            helper.setSubject("Bienvenue - 4You");
            String htmlContent = "<p>Chère/Cher " + username + ",</p>"
                    + "<p>Nous sommes ravis de vous informer que votre compte 4YOU a été créé avec succès!</p>"
                    + "<p>Email: " + email + "</p>"
                    + "<p>Mot de passe: SopraHR2024</p>"
                    + "<p>Merci de vous être inscrit! Veuillez cliquer sur le lien ci-dessous pour activer votre compte :</p>"
                    + "<p><a href='" + resetLink + "'>" + resetLink + "</a></p>"
                    + "<p>Votre code de vérification est : <strong>" + verificationCode + "</strong></p>"
                    + "<p>Vous pouvez vous connecter à votre compte en utilisant ce lien: <a href='http://localhost:4200/signin'>http://localhost:4200/signin</a></p>"
                    + "<p>Nous vous recommandons de changer votre mot de passe dès que possible pour des raisons de sécurité.</p>"
                    + "<p>Merci de faire partie de la communauté 4YOU.</p>"
                    + "<p>Cordialement,</p>"
                    + "<p>L'équipe 4YOU</p>";

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            // Gérer l'exception selon les besoins
            e.printStackTrace();
        }
    }

}

