package market.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import market.domain.notification.Notification;
import market.infrastructure.NotificationRepository;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationRepositoryTest {
    private NotificationRepository repository;

    @BeforeEach
    void setUp() {
        repository = new NotificationRepository();
    }

    @Test
    void addAndGetNotificationsForUser() {
        Notification n1 = new Notification("1", "user1", "Hello", Instant.now());
        Notification n2 = new Notification("2", "user1", "World", Instant.now());
        repository.addNotification(n1);
        repository.addNotification(n2);

        List<Notification> notifications = repository.getNotificationsForUser("user1");
        assertEquals(2, notifications.size());
        assertTrue(notifications.stream().anyMatch(n -> n.getMessage().equals("Hello")));
        assertTrue(notifications.stream().anyMatch(n -> n.getMessage().equals("World")));
    }

    @Test
    void markAsRead() {
        Notification n1 = new Notification("1", "user1", "Test", Instant.now());
        repository.addNotification(n1);
        repository.markAsRead("1", "user1");

        List<Notification> notifications = repository.getNotificationsForUser("user1");
        assertTrue(notifications.get(0).isRead());
    }
}