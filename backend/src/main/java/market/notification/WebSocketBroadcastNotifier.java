package market.notification;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

// Import NotificationMessage if it exists in another package
// import market.notification.NotificationMessage;

@Component
public class WebSocketBroadcastNotifier implements INotifier {
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketBroadcastNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public boolean notifyUser(String userId, String message) {
        // Create notification object with target user
        NotificationMessage notification = new NotificationMessage(userId, message);
        
        // Broadcast to all connected clients
        messagingTemplate.convertAndSend("/topic/notifications", notification);
        return true;
    }
}
