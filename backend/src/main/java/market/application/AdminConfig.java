package market.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Configuration class for admin user settings.
 * Reads configuration from application.properties file.
 */
@Component
public class AdminConfig {

    @Value("${admin.username:u1}")
    private String adminUsername;

    @Value("${admin.password:password123}")
    private String adminPassword;

    public String getAdminUsername() {
        return adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }
} 