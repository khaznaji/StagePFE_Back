package com.example.backend.Service;

import com.example.backend.Entity.Candidature;
import com.example.backend.Entity.Collaborateur;
import com.example.backend.Entity.Poste;
import com.example.backend.Repository.CandidatureRepository;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.PosteRepository;
import com.example.backend.Security.services.UserDetailsImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class ResumeMatcherService {

    @Autowired
    private CollaborateurRepository collaborateurRepository;
    @Autowired
    private CandidatureRepository candidatureRepository;
    @Autowired
    private PosteService posteService;

    public double calculateMatchPercentage(Long collaborateurId, Long posteId) throws IOException {
        Collaborateur collaborateur = collaborateurRepository.findById(collaborateurId)
                .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouvé avec l'ID : " + collaborateurId));
        Poste poste = posteService.getPosteById(posteId);
        String basePath = "C:\\Users\\olkhaznaji\\Desktop\\StagePFE\\Frontend\\src\\assets\\";

        String resumeText = extractTextFromPDF(collaborateurId, basePath);

        String jobDescription = poste.getDescription();
        double similarity = calculateTextSimilarity(resumeText, jobDescription);

        // Calcul du pourcentage de correspondance
        double matchPercentage = Math.round(similarity * 10000) / 100.0; // Multipliez par 10000 pour conserver deux décimales après la virgule, puis divisez par 100.0
       int matchPercentageFloat = (int) matchPercentage;
        Candidature candidature = candidatureRepository.findByCollaborateurIdAndPosteId(collaborateurId, posteId);
        if (candidature != null) {
            candidature.setMatchPercentage(matchPercentageFloat);
            candidatureRepository.save(candidature); // Enregistrer les modifications
        } else {
             throw new EntityNotFoundException("Candidature non trouvée pour le collaborateur et le poste spécifiés.");
        }

        return matchPercentage;
    }
    private double calculateTextSimilarity(String text1, String text2) {
        int maxLength = Math.max(text1.length(), text2.length());
        int distance = new LevenshteinDistance().apply(text1, text2);
        double similarity = 1.0 - ((double) distance / maxLength);
        return similarity;
    }

    public String extractTextFromPDF(Long collaborateurId, String basePath) throws IOException {
       Collaborateur collaborateur = collaborateurRepository.findById(collaborateurId)
               .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouvé avec l'ID : " + collaborateurId));

       // Obtenez le chemin du fichier PDF à partir de la base de données
       String pdfPath = collaborateur.getResume();

       if (pdfPath == null) {
           throw new IllegalArgumentException("Le chemin du fichier PDF est null.");
       }

       // Extraire le nom de fichier du chemin
       Path path = Paths.get(pdfPath);
       String fileName = path.getFileName().toString();

       // Formater le nom de fichier
       String formattedFileName = fileName.replace('_', ' '); // Remplacer '_' par un espace

       // Construire le chemin complet
       String fullPath = basePath + pdfPath;

       // Charger le document PDF
       try (PDDocument document = PDDocument.load(new File(fullPath))) {
           // Créer un extracteur de texte PDF
           PDFTextStripper stripper = new PDFTextStripper();

           // Extraire le texte du document PDF
           String text = stripper.getText(document);

           // Retourner le texte extrait
           return text;
       }
   }



    private List<String> preprocessText(String text) {
        // Supprimer la ponctuation et les caractères spéciaux
        text = text.replaceAll("[^a-zA-Z\\s]", "");

        // Convertir en minuscules et séparer les mots par des espaces
        String[] words = text.toLowerCase().split("\\s+");

        // Créer une liste à partir du tableau de mots
        List<String> wordList = new ArrayList<>(Arrays.asList(words));

        return wordList;
    }
}
