package com.example.backend.Configuration;

import com.example.backend.Entity.Candidature;
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
import java.time.format.DateTimeFormatter;

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
    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
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
    /* public void sendEmailToManagerService(Candidature candidature) {
        String managerName = candidature.getPoste().getManagerService().getManager().getNom();
        String candidateName = candidature.getCollaborateur().getCollaborateur().getNom() + " " +
                candidature.getCollaborateur().getCollaborateur().getPrenom();
        String postTitle = candidature.getPoste().getTitre();
        String interviewDateTime = candidature.getDateEntretien().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        String subject = "Programmation d'un entretien pour le poste " + postTitle;
        String body = "Cher/chère " + managerName + ",\n\n" +
                "J'espère que ce message vous trouve bien.\n\n" +
                "Nous avons le plaisir de vous informer que nous avons planifié un entretien pour le poste de " + postTitle + " avec un candidat de grande valeur, " + candidateName + ".\n\n" +
                "Détails de l'entretien :\n" +
                "- Date et heure : " + interviewDateTime + "\n" +
                "- Durée : [Durée estimée]\n\n" +
                "Veuillez noter cette date dans votre agenda. Si vous avez des empêchements ou des questions concernant cet entretien, n'hésitez pas à me contacter directement.\n\n" +
                "Dans l'attente de cet entretien prometteur, je vous remercie pour votre coopération.\n\n" +
                "Cordialement,\n" +
                "[L'équipe de recrutement 4YOU]";

        sendEmail(candidature.getPoste().getManagerService().getManager().getEmail(), subject, body);
        System.out.println("E-mail envoyé au manager de service à : " + candidature.getPoste().getManagerService().getManager().getEmail());
    }



    public void sendEmailToCollaborateur(Candidature candidature) {
        String subject = "Confirmation d'entretien en ligne pour le poste " + candidature.getPoste().getTitre();
        String body = "Cher/Chère " + candidature.getCollaborateur().getCollaborateur().getPrenom() + ",\n\n" +
                "Nous sommes ravis de vous informer que vous avez été sélectionné(e) pour un entretien en ligne pour le poste de " + candidature.getPoste().getTitre() + ".\n\n" +
                "Date et heure de l'entretien : " + candidature.getDateEntretien().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + ".\n\n" +
                "L'entretien se déroulera en ligne via notre plateforme de recrutement. Vous pourrez accéder à toutes les informations relatives à cet entretien dans la section 'Mes entretiens' de notre plateforme.\n\n" +
                "Nous vous recommandons de vous connecter quelques minutes avant l'heure prévue de l'entretien pour effectuer les éventuels réglages techniques nécessaires.\n\n" +
                "Si vous avez des questions ou besoin d'assistance, n'hésitez pas à nous contacter.\n\n" +
                "Cordialement,\nL'équipe de recrutement";


        sendEmail(candidature.getCollaborateur().getCollaborateur().getEmail(), subject, body);
    } */
}

