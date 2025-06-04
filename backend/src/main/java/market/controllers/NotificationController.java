package market.controllers;

import market.application.NotificationService;
import market.domain.notification.Notification;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Get all notifications for a user
    @GetMapping("/{userId}")
    public List<Notification> getNotifications(@PathVariable String userId) {
        return notificationService.getNotifications(userId);
    }

    // Mark a notification as read
    @PostMapping("/{userId}/read/{notificationId}")
    public void markAsRead(@PathVariable String userId, @PathVariable String notificationId) {
        notificationService.markAsRead(notificationId, userId);
    }
}