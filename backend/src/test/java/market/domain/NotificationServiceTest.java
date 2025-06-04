package market.domain;

import market.application.NotificationService;
import market.domain.notification.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationServiceTest {
    private INotificationRepository repository;
    private NotificationService service;

    @BeforeEach
    void setUp() {
        repository = mock(INotificationRepository.class);
        service = new NotificationService(repository);
    }

    @Test
    void sendNotification_callsRepository() {
        service.sendNotification("user1", "Test message");
        verify(repository, times(1)).addNotification(any(Notification.class));
    }

    @Test
    void getNotifications_delegatesToRepository() {
        service.getNotifications("user1");
        verify(repository, times(1)).getNotificationsForUser("user1");
    }

    @Test
    void markAsRead_delegatesToRepository() {
        service.markAsRead("notif1", "user1");
        verify(repository, times(1)).markAsRead("notif1", "user1");
    }

    @Test
    void sendNotification_createsNotificationWithCorrectFields() {
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        String userId = "user42";
        String message = "Welcome!";
        service.sendNotification(userId, message);

        verify(repository).addNotification(captor.capture());
        Notification notif = captor.getValue();
        assertEquals(userId, notif.getUserId());
        assertEquals(message, notif.getMessage());
        assertNotNull(notif.getId());
        assertNotNull(notif.getTimestamp());
        assertFalse(notif.isRead());
    }

    @Test
    void sendMultipleNotifications_areAllPassedToRepository() {
        service.sendNotification("user1", "msg1");
        service.sendNotification("user2", "msg2");
        verify(repository, times(2)).addNotification(any(Notification.class));
    }
}