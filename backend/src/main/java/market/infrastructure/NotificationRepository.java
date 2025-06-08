package market.infrastructure;

import market.domain.notification.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class NotificationRepository implements INotificationRepository {
    // userId -> List<Notification>
    private final Map<String, List<Notification>> notifications = new ConcurrentHashMap<>();

    @Override
    public void addNotification(Notification notification) {
        notifications.computeIfAbsent(notification.getUserId(), k -> new ArrayList<>()).add(notification);
    }

    @Override
    public List<Notification> getNotificationsForUser(String userId) {
        return notifications.getOrDefault(userId, Collections.emptyList());
    }


    @Override
    public void markAsRead(String notificationId, String userId) {
        List<Notification> userNotifications = notifications.get(userId);
        if (userNotifications != null) {
            userNotifications.stream()
                .filter(n -> n.getId().equals(notificationId))
                .forEach(Notification::markAsRead);
        }
    }
}