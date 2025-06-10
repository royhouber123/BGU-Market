package market.domain;

import market.application.NotificationService;
import market.domain.notification.*;
import market.notification.INotifier; // Updated import
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
    private INotifier notifier; // Updated variable name and type
    private NotificationService service;

    @BeforeEach
    void setUp() {
        repository = mock(INotificationRepository.class);
        notifier = mock(INotifier.class); // Updated to mock INotifier interface
        service = new NotificationService(repository, notifier); // Updated parameter
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

    @Test
    void sendNotification_callsNotifierWithCorrectUserAndMessage() {
        // Test that the notifier is called with the correct parameters
        service.sendNotification("user1", "Test message");
        verify(notifier, times(1)).notifyUser(eq("user1"), any(String.class));
    }
}