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
        try {
            // Act: Try to login with admin credentials
            AuthService.AuthToken loginResponse = authService.login("admin", "admin");
            assertNotNull(loginResponse, "Login should return a token");
            assertNotNull(loginResponse.token(), "Token should not be null");
        } catch (Exception e) {
            fail("Admin login should not throw an exception: " + e.getMessage());
        }
    }

    @Test
    void testAdminUserPasswordUpdate() throws Exception {
        // Arrange: Run startup to create admin user
        startupConfig.run();
        try {
            // Verify initial login works
            authService.login("admin", "admin");
        } catch (Exception e) {
            fail("Initial admin login should work" + e.getMessage());
        }
        // Act: Update admin config with new password and run startup again
        ReflectionTestUtils.setField(adminConfig, "adminPassword", "newPassword");
        startupConfig.run();
        try {
            // Assert: Should be able to login with new password
            authService.login("admin", "newPassword");
        } catch (Exception e) {
            fail("Login with new password should work: " + e.getMessage());
        } try {
            // Old password should not work
            authService.login("admin", "admin");
            fail("Login with old password should not work");
        } catch (Exception e) {
            // Assert: Old password should fail
            assertTrue(e.getMessage().contains("Invalid username or password"), "Login with old password should fail");
        }
    }

    @Test
    void testConfigurationValues() {
        // Test that admin config reads default values correctly
        assertEquals("admin", adminConfig.getAdminUsername(), "Default admin username should be 'admin'");
        assertEquals("admin", adminConfig.getAdminPassword(), "Default admin password should be 'admin'");
    }
} 