package market.domain.notification;

import java.time.Instant;

public class Notification {
    private final String id;
    private final String userId;
    private final String message;
    private final Instant timestamp;
    private boolean read;

    public Notification(String id, String userId, String message, Instant timestamp) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.timestamp = timestamp;
        this.read = false;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getMessage() { return message; }
    public Instant getTimestamp() { return timestamp; }
    public boolean isRead() { return read; }
    public void markAsRead() { this.read = true; }
}