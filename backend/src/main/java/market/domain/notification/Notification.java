package market.domain.notification;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.Instant;

public class Notification {
    private final String id;
    private final String userId;
    private final String message;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
    private Instant timestamp;
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
    @JsonIgnore
    public Instant getTimestamp() { return timestamp; }
    public boolean isRead() { return read; }
    public void markAsRead() { this.read = true; }

    @JsonProperty("timestamp")
    public String getTimestampAsString() {
        return timestamp.toString();
    }

    // public String toJson() throws Exception {
    //     ObjectMapper mapper = new ObjectMapper();
    //     mapper.registerModule(new JavaTimeModule());
    //     mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    //     return mapper.writeValueAsString(this);
    // }
}