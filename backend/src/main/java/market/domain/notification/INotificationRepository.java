package market.domain.notification;

import java.util.List;

public interface INotificationRepository {
    void addNotification(Notification notification);
    List<Notification> getNotificationsForUser(String userId);
    void markAsRead(String notificationId, String userId);
}