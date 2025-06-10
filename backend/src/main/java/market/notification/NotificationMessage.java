package market.notification;

public class NotificationMessage {
    private String userName;
    private String message;
    private long timestamp;

    public NotificationMessage(String targetUserId, String message) {
        this.userName = targetUserId;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public String getUserName() { return userName; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
}
