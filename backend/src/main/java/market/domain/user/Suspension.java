package market.domain.user;
import java.time.Duration;
import java.time.Instant;

public class Suspension {
    private final String userName;
    private final Instant suspensionStart;
    private final long suspensionDurationHours; // 0 means permanent

    public Suspension(String userName, long durationHours) {
        this.userName = userName;
        this.suspensionStart = Instant.now();
        this.suspensionDurationHours = durationHours;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isSuspended() {
        if (suspensionDurationHours == 0) return true; // permanent
        Instant end = suspensionStart.plus(Duration.ofHours(suspensionDurationHours));
        return Instant.now().isBefore(end);
    }

    public Instant getSuspensionEnd() {
        return (suspensionDurationHours == 0) ? null :
               suspensionStart.plus(Duration.ofHours(suspensionDurationHours));
    }
}