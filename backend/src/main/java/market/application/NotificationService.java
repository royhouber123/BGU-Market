package market.application;

import market.domain.notification.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class NotificationService {
    private final INotificationRepository notificationRepository;

    public NotificationService(INotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void sendNotification(String userId, String message) {
        Notification notification = new Notification(
            UUID.randomUUID().toString(),
            userId,
            message,
            Instant.now()
        );
        notificationRepository.addNotification(notification);
    }

    public List<Notification> getNotifications(String userId) {
        return notificationRepository.getNotificationsForUser(userId);
    }

    public void markAsRead(String notificationId, String userId) {
        notificationRepository.markAsRead(notificationId, userId);
    }
}