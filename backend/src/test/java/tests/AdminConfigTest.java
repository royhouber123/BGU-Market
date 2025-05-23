package tests;

import market.application.AdminConfig;
import market.application.AuthService;
import market.application.StartupConfig;
import market.domain.user.IUserRepository;
import market.domain.user.User;
import market.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import utils.ApiResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify admin user configuration functionality.
 */
public class AdminConfigTest {

    private StartupConfig startupConfig;
    private AdminConfig adminConfig;
    private IUserRepository userRepository;
    private AuthService authService;

    @BeforeEach
    void setup() {
        // Create test instances
        userRepository = new UserRepository();
        authService = new AuthService(userRepository);
        adminConfig = new AdminConfig();
        startupConfig = new StartupConfig();

        // Set up the admin config with test values using reflection
        ReflectionTestUtils.setField(adminConfig, "adminUsername", "admin");
        ReflectionTestUtils.setField(adminConfig, "adminPassword", "admin");

        // Inject dependencies into startup config using reflection
        ReflectionTestUtils.setField(startupConfig, "adminConfig", adminConfig);
        ReflectionTestUtils.setField(startupConfig, "userRepository", userRepository);
    }

    @Test
    void testAdminUserCreationOnStartup() throws Exception {
        // Act: Run the startup configuration
        startupConfig.run();

        // Assert: Admin user should exist
        User adminUser = userRepository.findById("admin");
        assertNotNull(adminUser, "Admin user should be created on startup");
        assertEquals("admin", adminUser.getUserName(), "Admin username should match configuration");
    }

    @Test
    void testAdminUserLogin() throws Exception {
        // Arrange: Run startup to create admin user
        startupConfig.run();

        // Act: Try to login with admin credentials
        ApiResponse<AuthService.AuthToken> loginResponse = authService.login("admin", "admin");

        // Assert: Login should be successful
        assertTrue(loginResponse.isSuccess(), "Admin login should be successful");
        assertNotNull(loginResponse.getData(), "Login should return a token");
        assertNotNull(loginResponse.getData().token(), "Token should not be null");
    }

    @Test
    void testAdminUserPasswordUpdate() throws Exception {
        // Arrange: Run startup to create admin user
        startupConfig.run();

        // Verify initial login works
        ApiResponse<AuthService.AuthToken> initialLogin = authService.login("admin", "admin");
        assertTrue(initialLogin.isSuccess(), "Initial admin login should work");

        // Act: Update admin config with new password and run startup again
        ReflectionTestUtils.setField(adminConfig, "adminPassword", "newPassword");
        startupConfig.run();

        // Assert: Should be able to login with new password
        ApiResponse<AuthService.AuthToken> newLogin = authService.login("admin", "newPassword");
        assertTrue(newLogin.isSuccess(), "Login with new password should work");

        // Old password should not work
        ApiResponse<AuthService.AuthToken> oldLogin = authService.login("admin", "admin");
        assertFalse(oldLogin.isSuccess(), "Login with old password should fail");
    }

    @Test
    void testConfigurationValues() {
        // Test that admin config reads default values correctly
        assertEquals("admin", adminConfig.getAdminUsername(), "Default admin username should be 'admin'");
        assertEquals("admin", adminConfig.getAdminPassword(), "Default admin password should be 'admin'");
    }
} 