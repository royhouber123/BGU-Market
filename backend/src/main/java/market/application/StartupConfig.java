package market.application;

import market.domain.user.Admin;
import market.domain.user.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import utils.Logger;

/**
 * Startup configuration that runs when the application boots.
 * Handles initialization tasks like creating the default admin user.
 */
@Component
public class StartupConfig implements CommandLineRunner {

    private static final Logger logger = Logger.getInstance();

    @Autowired
    private AdminConfig adminConfig;

    @Autowired
    private IUserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        logger.info("[StartupConfig] Initializing system on startup...");
        initializeAdminUser();
        logger.info("[StartupConfig] System initialization completed.");
    }

    /**
     * Ensures that an admin user exists in the system with the configured credentials.
     * If the admin user doesn't exist, it creates one.
     * If it exists, it updates the password to match the configuration.
     */
    private void initializeAdminUser() {
        String adminUsername = adminConfig.getAdminUsername();
        String adminPassword = adminConfig.getAdminPassword();
        
        logger.info("[StartupConfig] Initializing admin user with username: " + adminUsername);

        try {
            // Check if admin user already exists
            userRepository.findById(adminUsername);
            // If we get here, user exists - update password to match config
            userRepository.changePassword(adminUsername, adminPassword);
            logger.info("[StartupConfig] Admin user already exists. Password updated to match configuration.");
        } catch (RuntimeException e) {
            // User doesn't exist, create new admin user
            Admin admin = new Admin(adminUsername);
            userRepository.saveAdmin(admin, adminPassword);
            logger.info("[StartupConfig] New admin user created with username: " + adminUsername);
        }
    }
} 