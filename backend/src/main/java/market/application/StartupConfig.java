package market.application;

import market.domain.user.Admin;
import market.domain.user.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import utils.Logger;

/**
 * Startup configuration that runs when the application boots.
 * Handles initialization tasks like creating the default admin user.
 * 
 * Note: Demo data population is handled by DemoDataPopulator using the ApplicationReadyEvent
 * which runs after all beans are initialized and the application context is ready.
 */
@Component
public class StartupConfig implements CommandLineRunner {

    private static final Logger logger = Logger.getInstance();

    @Autowired
    private AdminConfig adminConfig;

    @Autowired
    private IUserRepository userRepository;
    
    @Autowired
    private Environment env;

    @Override
    public void run(String... args) throws Exception {
        logger.info("[StartupConfig] Initializing system on startup...");
        
        // ✅ CHECK YOUR ACTUAL PROPERTY NAME
        String populateData = env.getProperty("bgu.market.populate-demo-data");
        boolean isDemoMode = "true".equalsIgnoreCase(populateData);
        
        if (!isDemoMode) {
            // Demo is OFF - initialize admin here
            logger.info("[StartupConfig] Demo mode disabled - initializing admin from properties");
            initializeAdminUser();
        } else {
            // Demo is ON - DemoDataPopulator will handle admin initialization
            logger.info("[StartupConfig] Demo mode enabled - admin initialization delegated to DemoDataPopulator");
        }
        
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
        
        if (adminUsername == null || adminPassword == null) {
            throw new RuntimeException("[StartupConfig] Admin credentials not found in properties (admin.username/admin.password) and demo mode is disabled!");
        }
        
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

    /**
     * PUBLIC METHOD: For DemoDataPopulator to call when in demo mode
     */
    public void initializeAdminWithCredentials(String username, String password) {
        if (username == null || password == null) {
            throw new RuntimeException("[StartupConfig] Cannot initialize admin - null credentials provided!");
        }
        
        logger.info("[StartupConfig] Initializing admin user with username: " + username);

        try {
            // Check if admin user already exists
            userRepository.findById(username);
            // If we get here, user exists - update password to match config
            userRepository.changePassword(username, password);
            logger.info("[StartupConfig] Admin user already exists. Password updated to match configuration.");
        } catch (RuntimeException e) {
            // User doesn't exist, create new admin user
            Admin admin = new Admin(username);
            userRepository.saveAdmin(admin, password);
            logger.info("[StartupConfig] New admin user created with username: " + username);
        }
    }
}