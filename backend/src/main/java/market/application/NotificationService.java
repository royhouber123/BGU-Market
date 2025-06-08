package market.application;

import market.domain.notification.*;
import market.notification.INotifier;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class NotificationService {
    private final INotificationRepository notificationRepository;
    private final INotifier notificationSender; // Changed from NotificationWebSocketHandler
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NotificationService(INotificationRepository notificationRepository, INotifier notificationSender) {
        this.notificationRepository = notificationRepository;
        this.notificationSender = notificationSender; // Updated parameter
    }

    public void sendNotification(String userName, String message) {
        Notification notification = new Notification(
            UUID.randomUUID().toString(),
            userName,
            message,
            Instant.now()
        );
        notificationRepository.addNotification(notification);

        // Send via notification sender interface
        try {
            String notifJson = objectMapper.writeValueAsString(notification);
            notificationSender.notifyUser(userName, notifJson); // Updated method call
        } catch (Exception e) {
            throw new RuntimeException("Failed to send notification", e);
            // handle error
        }
    }

    public List<Notification> getNotifications(String userId) {
        return notificationRepository.getNotificationsForUser(userId);
    }

    public void markAsRead(String notificationId, String userId) {
        notificationRepository.markAsRead(notificationId, userId);
    }
}