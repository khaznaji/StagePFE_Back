package com.example.backend.Service;
import com.example.backend.Entity.User;
import com.example.backend.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Base64;
import java.io.IOException;

@Service
public class FacialRecognitionService {
@Autowired
private UserRepository userRepository ;
    public void configureUserFace(User user, MultipartFile imageFile) {
        try {
            // Convert MultipartFile to Base64 string
            String encodedImage = Base64.getEncoder().encodeToString(imageFile.getBytes());
            // Set the encoded image string to the user's face image field
            user.setFaceImage(encodedImage);
            // Save or update the user record in the database
            userRepository.save(user);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error occurred during face configuration");
        }
    }

    public String recognizeUser(User user, MultipartFile imageFile) {
        try {
            // Convert MultipartFile to Base64 string
            String capturedImage = Base64.getEncoder().encodeToString(imageFile.getBytes());
            // Compare the captured face image with the stored face image of the user
            boolean isMatch = user.getFaceImage().equals(capturedImage);
            // Return recognition result
            if (isMatch) {
                return "User recognized";
            } else {
                return "User not recognized";
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error occurred during recognition");
        }
    }
}
