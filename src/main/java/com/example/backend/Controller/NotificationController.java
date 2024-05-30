package com.example.backend.Controller;

import com.example.backend.Entity.Notification;
import com.example.backend.Repository.NotificationRepository;
import com.example.backend.Security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/Notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getUserNotifications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        List<Notification> notifications = notificationRepository.findByReceiverId(userId);

        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/notifications/markAsSeen")
    public Map<String, String> markNotificationAsSeen(@RequestBody Long notificationId) {
        Map<String, String> response = new HashMap<>();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        // Vérifie si la notification appartient à l'utilisateur actuellement authentifié
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
            if (notification.getReceiver().getId().equals(userId)) {
                // Marquer la notification comme vue
                notification.setSeen(true);
                notificationRepository.save(notification);
                response.put("message", "Notification marquée comme vue avec succès.");
            } else {
                response.put("error", "Vous n'êtes pas autorisé à accéder à cette notification.");
            }
        } else {
            response.put("error", "Notification non trouvée.");
        }
        return response;

    }
}
