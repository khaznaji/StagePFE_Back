package com.example.backend.Security.verificationCode;

import com.example.backend.Entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class CodeVerificationServiceImpl  implements CodeVerificationService{
    @Autowired
    private CodeVerificationRepository codeVerification;

    @Override
    public CodeVerification createToken(User user) {
        String token = UUID.randomUUID().toString();
        String activationCode = generateActivationCode();
        CodeVerification otpToken = new CodeVerification(token, user , activationCode , 15);
        return codeVerification.save(otpToken);
    }
    @Override
    public CodeVerification regenerateToken(String oldToken) {
        CodeVerification existingToken = codeVerification.findByToken(oldToken.toString());

        if (existingToken != null) {
            // Vérifier si le token est expiré
            if (existingToken.isExpired()) {
                // Régénérer le token
                String newToken = UUID.randomUUID().toString();
                String activationCode = generateActivationCode();

                // Mettre à jour le token existant
                existingToken.setToken(newToken);
                existingToken.setActivationCode(activationCode);
                existingToken.setExpiryDate(existingToken.calculateExpiryDate(15));

                codeVerification.save(existingToken);

                return existingToken;
            } else {
                // Le token n'est pas expiré, renvoyer l'ancien token
                return existingToken;
            }
        }

        return null; // Retourner null si le token n'est pas trouvé
    }

    private boolean isTokenExpired(CodeVerification token) {
        Date expirationDate = token.getExpiryDate();
        return expirationDate != null && expirationDate.before(new Date());
    }


    @Override
    public CodeVerification findByToken(String token) {
        return codeVerification.findByToken(token);
    }

    @Override
    public void deleteToken(CodeVerification token) {
        codeVerification.delete(token);
    }

    private String generateActivationCode() {
        // Générer un nombre aléatoire entre 1000 et 9999
        int randomCode = (int) (Math.random() * 9000) + 1000;
        return String.valueOf(randomCode);
    }

}
