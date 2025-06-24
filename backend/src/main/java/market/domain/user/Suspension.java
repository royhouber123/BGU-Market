package market.domain.user;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "suspensions")
public class Suspension {

    @Id
    @Column(name = "user_name", nullable = false, unique = true)
    private String userName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_name", referencedColumnName = "user_name", insertable = false, updatable = false)
    private User user;

    @Column(name = "suspension_start", nullable = false)
    private LocalDateTime suspensionStart;

    @Column(name = "suspension_duration_hours", nullable = false)
    private long suspensionDurationHours; // 0 means permanent

    public Suspension() {
        // Required for JPA
    }

    public Suspension(String userName, long durationHours) {
        this.userName = userName;
        this.suspensionStart = LocalDateTime.now(java.time.ZoneId.of("Asia/Jerusalem"));
        this.suspensionDurationHours = durationHours;
    }

    // Getters and setters

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.userName = user.getUserName();
        }
    }

    public LocalDateTime getSuspensionStart() {
        return suspensionStart;
    }

    public void setSuspensionStart(LocalDateTime suspensionStart) {
        this.suspensionStart = suspensionStart;
    }

    public long getSuspensionDurationHours() {
        return suspensionDurationHours;
    }

    public void setSuspensionDurationHours(long suspensionDurationHours) {
        this.suspensionDurationHours = suspensionDurationHours;
    }

    public boolean isSuspended() {
        if (suspensionDurationHours == 0) return true; // permanent
        LocalDateTime end = suspensionStart.plus(java.time.Duration.ofHours(suspensionDurationHours));
        return LocalDateTime.now().isBefore(end);
    }

    public LocalDateTime getSuspensionEnd() {
        return (suspensionDurationHours == 0) ? null :
               suspensionStart.plus(java.time.Duration.ofHours(suspensionDurationHours));
    }
}