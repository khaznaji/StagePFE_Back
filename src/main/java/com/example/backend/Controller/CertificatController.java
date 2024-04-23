package com.example.backend.Controller;


import com.example.backend.Entity.Certificat;
import com.example.backend.Entity.Collaborateur;
import com.example.backend.Entity.Formation;
import com.example.backend.Entity.Groups;
import com.example.backend.Repository.CertificatRepository;
import com.example.backend.Repository.CollaborateurRepository;
import com.example.backend.Repository.GroupsRespository;
import com.example.backend.Service.CertificatService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/certif")

public class CertificatController {
    @Autowired
    protected GroupsRespository groupsRepository;
    @Autowired
    private CollaborateurRepository userRepository;
    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private CertificatRepository certificatRepository;
    @Autowired
    private CertificatService certificateService;
    @PostMapping("/Generer/{idgroupe}")
    public void genererCertificatForGroup(@PathVariable Long idgroupe, @RequestParam String month, @RequestParam String periode) throws RuntimeException, IOException, DocumentException {
        try {
            Groups group = groupsRepository.findById(idgroupe).orElseThrow(() -> new RuntimeException("Groupe introuvable"));

            Formation formation = group.getFormation();
            String nom_formation = formation.getTitle();

            List<Collaborateur> users = group.getCollaborateurs();

            if (users.isEmpty()) {
                throw new RuntimeException("Aucun utilisateur n'est associé au groupe.");
            }

            if (group.isCertificatesGenerated()) {
                throw new RuntimeException("Certificates already generated for this group.");
            }

            for (Collaborateur user : users) {
                // Vérifier si l'utilisateur a déjà un certificat
                if (user.getCertificats().isEmpty()) {
                    String fullName = user.getCollaborateur().getNom() + " " + user.getCollaborateur().getPrenom() ;

                    String relativePath = "Certifications/" + nom_formation + " " + month + "/" + "_" + user.getId() + "_" + user.getCollaborateur().getNom() + "_" + user.getCollaborateur().getPrenom() + ".pdf";
                    String pdfname = "C:\\Users\\olkhaznaji\\Desktop\\StagePFE\\Frontend\\src\\assets\\" + relativePath;
                    File f = new File("C:\\Users\\olkhaznaji\\Desktop\\StagePFE\\Frontend\\src\\assets\\Certifications\\" + nom_formation + " " + month);
                    if (f.mkdir()) {
                        System.out.println("Directory has been created successfully");
                    } else {
                        System.out.println("Directory cannot be created");
                    }
                    Document document = new Document();
                    document.setPageSize(PageSize.A4.rotate());

                    Certificat certificat = new Certificat();
                    certificat.setDate(LocalDateTime.now());
                    certificat.setPeriode(periode);
                    certificat.setMonth(month);
                    group.setCertificatesGenerated(true);

                    certificat.setCollaborateur(user); // Set the relationship between Certificat and User

                    try {
                        FileOutputStream fo = new FileOutputStream(new File(pdfname));
                        PdfWriter writer = PdfWriter.getInstance(document, fo);

                        document.open();

                        PdfContentByte canvas = writer.getDirectContentUnder();

                        Image image = Image.getInstance("src/main/resources/certif2.jpg");
                        image.scaleAbsolute(PageSize.A4.rotate());
                        image.setAbsolutePosition(0, 0);
                        canvas.addImage(image);

                        float pos = (document.getPageSize().getWidth() / 2) - (fullName.length() * 18 / 2);
                        FixText(fullName, "savoyeplain.ttf", "Savoye", pos, 240, writer, 60);

                        certificate_footer(writer, fullName, periode, nom_formation, month);

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                        String formattedDate = certificat.getDate().format(formatter);
                        FixText(formattedDate, "poppins.regular.ttf", "Poppins", 280, 100, writer, 13);

                        String str = "http://localhost:4200/student/profile/" + user.getId(); // Utiliser l'ID de l'utilisateur pour le lien QR
                        BarcodeQRCode my_code = new BarcodeQRCode(str, 100, 100, null);
                        Image qr_image = my_code.getImage();
                        qr_image.setAbsolutePosition(70, 60);
                        document.add(qr_image);

                        document.close();
                        writer.close();
                        fo.close();
                        System.out.println("Done");
                        String certificateLink = "http://localhost:4200/student/profile/" + user.getId();
                      sendEmailWithAttachment(user.getCollaborateur().getEmail(), pdfname, fullName, certificateLink);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // Set the relationship between Certificat and User
                    certificat.setPath(relativePath);
                    certificat.setUserOrGroupId(group.getId()); // ou certificat.setUserOrGroupId(group.getId());

                    certificatRepository.save(certificat);
                    System.out.print(nom_formation);
                } else {
                    System.out.println("Certificat already exists for user: " + user.getCollaborateur().getNom() + " " + user.getCollaborateur().getPrenom());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    /***********user*********/
 public boolean areAllUsersCertified(Groups group) {
        List<Collaborateur> usersInGroup = group.getCollaborateurs();
        int certifiedUserCount = 1;

        for (Collaborateur user : usersInGroup) {
            if (!user.getCertificats().isEmpty()) {
                certifiedUserCount++;
            }
        }

        System.out.println(certifiedUserCount + "/" + usersInGroup.size() + " " + (certifiedUserCount == usersInGroup.size()));

        return certifiedUserCount == usersInGroup.size();
    }





   private void sendEmailWithAttachment(String toEmail, String attachmentPath, String fullName, String certificateLink) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        try {
            helper.setTo(toEmail);
            helper.setSubject("Your Certificate");

            // Set the email body
            String emailBody = "Hello " + fullName + ",\n\n" +
                    "Congratulations! Your certificate has been generated.\n" +
                    "You can download your certificate using the link below:\n\n" +
                    certificateLink + "\n\n" +
                    "Best regards,\n" +
                    "Your Certificate Team";

            helper.setText(emailBody);

            // Attach the certificate PDF
            FileSystemResource file = new FileSystemResource(new File(attachmentPath));
            helper.addAttachment("Certificate.pdf", file);

            javaMailSender.send(message);
            System.out.println("Email sent with certificate to: " + toEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
   }


    private static void FixText (String text, String fontfile, String fontname,float x, int y, PdfWriter writer,
                                 int size){
        PdfContentByte cb = writer.getDirectContent();


        FontFactory.register("C:\\Users\\olkhaznaji\\Desktop\\StagePFE\\Backend\\src\\main\\resources\\fonts\\" + fontfile);
        Font textFont = FontFactory.getFont(fontname, BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED, 10);
        BaseFont bf = textFont.getBaseFont();

        cb.saveState();
        cb.beginText();
        cb.moveText(x, y);
        cb.setFontAndSize(bf, size);
        cb.showText(text);
        cb.endText();
        cb.restoreState();
    }
    private static void certificate_footer(PdfWriter writer, String name, String periode, String formation, String month ) {

        PdfContentByte cb = writer.getDirectContent();


        FontFactory.register("C:\\Users\\olkhaznaji\\Desktop\\StagePFE\\Backend\\src\\main\\resources\\fonts\\Poppins-Thin.ttf");
        Font textFont = FontFactory.getFont("Poppins Thin", BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED, 10);
        BaseFont bf = textFont.getBaseFont();
        FontFactory.register("C:\\Users\\olkhaznaji\\Desktop\\StagePFE\\Backend\\src\\main\\resources\\fonts\\poppins.regular.ttf");
        Font textFont2 = FontFactory.getFont("Poppins", BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED, 10);
        BaseFont bf2 = textFont2.getBaseFont();

        cb.saveState();
        cb.beginText();
        cb.moveText(180, 185);
        cb.setFontAndSize(bf, 14);
        String begin = "This is to certify that";
        cb.showText(begin);
        cb.endText();

        cb.beginText();
        cb.setFontAndSize(bf2, 14);
        float pos_name = 180+cb.getEffectiveStringWidth(begin, false);
        cb.moveText(pos_name, 185);
        cb.showText(name);
        cb.endText();

        cb.beginText();
        cb.setFontAndSize(bf, 14);
        float pos_text2 = pos_name+cb.getEffectiveStringWidth(name, false);
        cb.moveText(pos_text2 + 10, 185);
        String next ="successfully completed ";
        cb.showText(next);
        cb.endText();


        cb.beginText();
        cb.setFontAndSize(bf2, 14);
        float pos_mot2 = pos_text2 +cb.getEffectiveStringWidth(next, false);
        cb.moveText(pos_mot2 + 7, 185);
        cb.showText(periode);
        cb.endText();

        cb.beginText();
        cb.moveText(180,165);
        cb.setFontAndSize(bf, 14);
        String of = "of";
        cb.showText(of);
        cb.endText();

        cb.beginText();
        cb.setFontAndSize(bf2, 14);
        float pos_formation = 180+cb.getEffectiveStringWidth(of, false);
        cb.moveText(pos_formation + 7, 165);
        cb.showText(formation);
        cb.endText();

        cb.beginText();
        cb.setFontAndSize(bf, 14);
        float pos_text3 = pos_formation+cb.getEffectiveStringWidth(formation, false);
        cb.moveText(pos_text3 + 20, 165);
        String next2 = "training and coaching on";
        cb.showText(next2);
        cb.endText();

        cb.beginText();
        cb.setFontAndSize(bf2, 14);
        float pos_month = pos_text3+cb.getEffectiveStringWidth(next2, false);
        cb.moveText(pos_month + 20, 165);
        cb.showText(month);
        cb.endText();

        cb.restoreState();
    }






    private void deleteCertificatFiles(String pathToDelete) {
        try {
            Path directoryPath = Paths.get(pathToDelete);
            Files.walk(directoryPath)
                    .sorted((p1, p2) -> -p1.compareTo(p2)) // Delete files in reverse order (deepest first)
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /***hedhi**/
    @DeleteMapping("/Supprimer/{idgroupe}")
    public ResponseEntity<String> supprimerCertificatsForGroup(@PathVariable Long idgroupe) {
        try {
            Groups group = groupsRepository.findById(idgroupe).orElseThrow(() -> new RuntimeException("Groupe introuvable"));
            Formation formation = group.getFormation();
            String nom_formation = formation.getTitle();
            List<Collaborateur> users = group.getCollaborateurs(); // Assuming "getEtudiants()" returns the list of users associated with the group
            group.setCertificatesGenerated(false);
            if (users.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            for (Collaborateur user : users) {
                List<Certificat> certificats = certificatRepository.findByCollaborateur(user); // Assuming "findByUser" is the correct method in your repository
                for (Certificat certificat : certificats) {
                    String pathToDelete = "C:\\Users\\olkhaznaji\\Desktop\\StagePFE\\Frontend\\src\\assets\\Certifications\\" + nom_formation + " " + certificat.getMonth();

                    deleteCertificatFiles(pathToDelete);
                    certificatRepository.delete(certificat);
                }
            }

            return ResponseEntity.ok("Certificats deleted successfully for the group: " + idgroupe);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting certificats.");
        }
    }
    /**** hedhi ****/
    @PutMapping("/ModifierCertificats/{idgroupe}")
    public ResponseEntity<String> modifierCertificatsForGroup(@PathVariable Long idgroupe, @RequestParam String periode, @RequestParam String month) {
        try {
            Groups group = groupsRepository.findById(idgroupe).orElseThrow(() -> new RuntimeException("Groupe introuvable"));

            List<Collaborateur> users = group.getCollaborateurs();

            if (users.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            for (Collaborateur user : users) {
                String fullName = user.getCollaborateur().getNom() + " " + user.getCollaborateur().getPrenom();
                Formation formation = group.getFormation();
                String nom_formation = formation.getTitle();

                String relativePath = "Certifications/" + nom_formation + " " + month + "/" + "_" + user.getId() + "_" + user.getCollaborateur().getNom() + "_" + user.getCollaborateur().getPrenom() + ".pdf";
                String pdfname = "C:\\Users\\olkhaznaji\\Desktop\\StagePFE\\Frontend\\src\\assets\\" + relativePath;
                File pdfFile = new File(pdfname);

                List<Certificat> certificats = certificatRepository.findByCollaborateur(user);
                for (Certificat certificat : certificats) {
                    String oldPath = certificat.getPath();
                    String newPath = oldPath.replace(certificat.getMonth().trim(), month.trim());

                    // Update periode and month in the certificate
                    certificat.setPeriode(periode);

                    // Check if the new month is different from the existing month
                    if (!month.equals(certificat.getMonth())) {
                        certificat.setMonth(month);

                        // Update certificate path if month changed
                        certificat.setPath(newPath);

                        // Delete the old folder if it exists
                        File oldFolder = new File("C:\\Users\\olkhaznaji\\Desktop\\StagePFE\\Frontend\\src\\assets\\" + oldPath).getParentFile();
                        if (oldFolder.exists() && oldFolder.isDirectory()) {
                            File[] files = oldFolder.listFiles();
                            if (files != null) {
                                for (File file : files) {
                                    file.delete();
                                }
                            }
                            oldFolder.delete();
                        }

                        // Create the new folder
                        pdfFile.getParentFile().mkdirs();
                    }

                    Document document = new Document();
                    document.setPageSize(PageSize.A4.rotate());
                    // Save the modified certificate


                    try {
                        FileOutputStream fo = new FileOutputStream(new File(pdfname));
                        PdfWriter writer = PdfWriter.getInstance(document, fo);

                        document.open();

                        PdfContentByte canvas = writer.getDirectContentUnder();

                        Image image = Image.getInstance("src/main/resources/certif2.jpg");
                        image.scaleAbsolute(PageSize.A4.rotate());
                        image.setAbsolutePosition(0, 0);
                        canvas.addImage(image);

                        float pos = (document.getPageSize().getWidth() / 2) - (fullName.length() * 18 / 2);
                        FixText(fullName, "savoyeplain.ttf", "Savoye", pos, 240, writer, 60);

                        certificate_footer(writer, fullName, periode, nom_formation, month);

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                        String formattedDate = certificat.getDate().format(formatter);
                        FixText(formattedDate, "poppins.regular.ttf", "Poppins", 280, 100, writer, 13);

                        String str = "http://localhost:4200/student/profile/" + user.getId(); // Utiliser l'ID de l'utilisateur pour le lien QR
                        BarcodeQRCode my_code = new BarcodeQRCode(str, 100, 100, null);
                        Image qr_image = my_code.getImage();
                        qr_image.setAbsolutePosition(70, 60);
                        document.add(qr_image);

                        document.close();
                        writer.close();
                        fo.close();
                        System.out.println("Done");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    certificat.setPath(relativePath);
                    Certificat savedCertificat = certificatRepository.save(certificat);
                    System.out.println("Updated certificat path: " + savedCertificat.getPath());
                }
            }

            return ResponseEntity.ok("Certificats updated successfully for the group: " + idgroupe);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating certificats.");
        }
    }

    /****** periode tetmodifa ***/
    @PutMapping("/ModifierCertificatsUpdatePeriode/{idgroupe}")
    public ResponseEntity<String> modifierCertificatsForGroupUpdatePeriode(@PathVariable Long idgroupe, @RequestParam String newPeriode, @RequestParam String newMonth) {
        try {
            Groups group = groupsRepository.findById(idgroupe).orElseThrow(() -> new RuntimeException("Groupe introuvable"));

            List<Collaborateur> users = group.getCollaborateurs();

            if (users.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            for (Collaborateur user : users) {
                String fullName = user.getCollaborateur().getNom() + " " + user.getCollaborateur().getPrenom();
                Formation formation = group.getFormation();
                String nom_formation = formation.getTitle();

                String relativePath = "Certifications/" + nom_formation + " " + newMonth + "/" +"_"+user.getId() +"_"+ user.getCollaborateur().getNom() +"_"+ user.getCollaborateur().getPrenom() + ".pdf";
                String pdfname = "C:\\Users\\olkhaznaji\\Desktop\\StagePFE\\Frontend\\src\\assets\\" + relativePath;
                File f = new File("C:\\Users\\olkhaznaji\\Desktop\\StagePFE\\Frontend\\src\\assets\\Certifications\\" + nom_formation + " " + newMonth);
                // Creates all directories if they don't exist

                if (f.mkdir()) {
                    System.out.println("Directory has been created successfully");
                } else {
                    System.out.println("Directory cannot be created");
                }
                Document document = new Document();
                document.setPageSize(PageSize.A4.rotate());
                List<Certificat> certificats = certificatRepository.findByCollaborateur(user);
                for (Certificat certificat : certificats) {
                    String oldPath = certificat.getPath();
                    String newPath = oldPath.replace(certificat.getMonth().trim(), newMonth.trim());

                    // Update periode and month in the certificate
                    certificat.setPeriode(newPeriode);
                    certificat.setMonth(newMonth);

                    // Update certificate path if month changed
                    if (!newMonth.equals(certificat.getMonth())) {
                        certificat.setPath(newPath);
                    }

                    // Save the modified certificate


                    try {
                        FileOutputStream fo = new FileOutputStream(new File(pdfname));
                        PdfWriter writer = PdfWriter.getInstance(document, fo);

                        document.open();

                        PdfContentByte canvas = writer.getDirectContentUnder();

                        Image image = Image.getInstance("src/main/resources/certif2.jpg");
                        image.scaleAbsolute(PageSize.A4.rotate());
                        image.setAbsolutePosition(0, 0);
                        canvas.addImage(image);

                        float pos = (document.getPageSize().getWidth() / 2) - (fullName.length() * 18 / 2);
                        FixText(fullName, "savoyeplain.ttf", "Savoye", pos, 240, writer, 60);

                        certificate_footer(writer, fullName, newPeriode, nom_formation, newMonth);

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                        String formattedDate = certificat.getDate().format(formatter);
                        FixText(formattedDate, "poppins.regular.ttf", "Poppins", 280, 100, writer, 13);

                        String str = "http://localhost:4200/student/profile/" + user.getId(); // Utiliser l'ID de l'utilisateur pour le lien QR
                        BarcodeQRCode my_code = new BarcodeQRCode(str, 100, 100, null);
                        Image qr_image = my_code.getImage();
                        qr_image.setAbsolutePosition(70, 60);
                        document.add(qr_image);

                        document.close();
                        writer.close();
                        fo.close();
                        System.out.println("Done");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    certificat.setPath(relativePath);
                    Certificat savedCertificat = certificatRepository.save(certificat);
                    System.out.println("Updated certificat path: " + savedCertificat.getPath());
                }
            }

            return ResponseEntity.ok("Certificats updated successfully for the group: " + idgroupe);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating certificats.");
        }
    }
    @GetMapping("/check-generated/{groupId}")
    public ResponseEntity<Boolean> checkCertificatesGenerated(@PathVariable Long groupId) {
        boolean certificatesGenerated = certificateService.checkIfCertificatesGenerated(groupId);
        System.out.println("Certificates generated for group " + groupId + ": " + certificatesGenerated);
        return ResponseEntity.ok(certificatesGenerated);

    }
    @GetMapping("/values/{idgroupe}")
    public ResponseEntity<List<Certificat>> getCertificateValuesByGroupId(@PathVariable Long idgroupe) {
        List<Certificat> certificatValues = certificateService.getCertificatByGroupId(idgroupe);
        if (!certificatValues.isEmpty()) {
            return ResponseEntity.ok(certificatValues);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/UserCertificates/{userId}")
    public ResponseEntity<List<Certificat>> getUserCertificates(
            @PathVariable Long userId
    ) {
        try {
            Collaborateur user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Certificat> userCertificates = user.getCertificats();

            return ResponseEntity.ok(userCertificates);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/UserCertificatesFormation/{userId}")
    public ResponseEntity<List<String>> getUserCertificatesFormationNames(@PathVariable Long userId) {
        try {
            Collaborateur user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Certificat> userCertificates = user.getCertificats();
            Set<String> formationNames = new HashSet<>();

            for (Certificat certificat : userCertificates) {
                String path = certificat.getPath();
                String formationName = extractFormationNameFromPath(path);
                formationNames.add(formationName);
            }

            return ResponseEntity.ok(new ArrayList<>(formationNames));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String extractFormationNameFromPath(String path) {
        int startIndex = path.indexOf("Certifications/") + "Certifications/".length();
        int endIndex = path.indexOf("/", startIndex);
        if (endIndex != -1) {
            return path.substring(startIndex, endIndex);
        }
        return "";
    }

    @GetMapping("/ShareCertificateOnLinkedIn/{certificatId}")
    public ResponseEntity<String> shareCertificateOnLinkedIn(@PathVariable Long certificatId) {
        try {
            Certificat certificat = certificatRepository.findById(certificatId)
                    .orElseThrow(() -> new RuntimeException("Certificat not found"));

            String shareUrl = "https://www.linkedin.com/sharing/share-offsite/?url="
                    + URLEncoder.encode(certificat.getPath(), "UTF-8");

            return ResponseEntity.ok(shareUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/Supprimer/{idgroupe}/user/{iduser}")
    public ResponseEntity<String> supprimerUserFromGroup(@PathVariable Long idgroupe, @PathVariable Long iduser) {
        try {
            Groups group = groupsRepository.findById(idgroupe).orElseThrow(() -> new RuntimeException("Groupe introuvable"));
            Collaborateur user = userRepository.findById(iduser).orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            List<Certificat> certificats = certificatRepository.findByCollaborateur(user);
            for (Certificat certificat : certificats) {
                String pdfPathToDelete = "C:\\Users\\olkhaznaji\\Desktop\\StagePFE\\Frontend\\src\\assets\\" + certificat.getPath();
                deleteCertificatFile(pdfPathToDelete); // Supprime le fichier PDF

                certificatRepository.delete(certificat);
            }
            group.setCertificatesGenerated(false);

            group.getCollaborateurs().remove(user);
            groupsRepository.save(group);

            return ResponseEntity.ok("User and associated certificats deleted successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting the user and certificats.");
        }
    }

    private void deleteCertificatFile(String pdfFilePath) {
        File pdfFile = new File(pdfFilePath);
        if (pdfFile.exists() && pdfFile.isFile()) {
            pdfFile.delete();
        }
    }

}
