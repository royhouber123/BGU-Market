package market.notification;

public class NotificationMessage {
    private String targetUserId;
    private String message;
    private long timestamp;

    public NotificationMessage(String targetUserId, String message) {
        this.targetUserId = targetUserId;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public String getTargetUserId() { return targetUserId; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
}
