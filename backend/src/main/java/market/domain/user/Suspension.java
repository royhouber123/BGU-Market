package market.domain.user;
import java.time.Instant;

public class Suspension {
    private final String userName;
    private final Instant suspensionStart;
    private final long suspensionDurationMillis; // 0 means permanent

    public Suspension(String userName, long durationMillis) {
        this.userName = userName;
        this.suspensionStart = Instant.now();
        this.suspensionDurationMillis = durationMillis;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isSuspended() {
        if (suspensionDurationMillis == 0) return true; // permanent
        Instant end = suspensionStart.plusMillis(suspensionDurationMillis);
        return Instant.now().isBefore(end);
    }

    public Instant getSuspensionEnd() {
        return (suspensionDurationMillis == 0) ? null :
               suspensionStart.plusMillis(suspensionDurationMillis);
    }
}