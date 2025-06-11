package tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import support.AcceptanceTestSpringBase;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test to verify that our test database configuration works correctly.
 * These tests will only run if MySQL is available.
 */
@EnabledIf("tests.DatabaseConnectionTest#isMySQLAvailable")
public class DatabaseConnectionTest extends AcceptanceTestSpringBase {

    /**
     * Check if MySQL is available before running any tests in this class
     */
    static boolean isMySQLAvailable() {
        try {
            DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/?useSSL=false", 
                "bgu", 
                "changeme"
            ).close();
            return true;
        } catch (Exception e) {
            System.out.println("⚠️  MySQL is not available - skipping DatabaseConnectionTest. " +
                "To run these tests, ensure MySQL is running with user 'bgu' and password 'changeme'");
            return false;
        }
    }

    @Test
    void testDatabaseConnection() {
        // This test verifies that:
        // 1. Spring context loads successfully
        // 2. Database connection is established
        // 3. All repositories are injected properly
        
        assertNotNull(userRepository, "UserRepository should be injected");
        assertNotNull(storeRepository, "StoreRepository should be injected");
        assertNotNull(listingRepository, "ListingRepository should be injected");
        assertNotNull(purchaseRepository, "PurchaseRepository should be injected");
        
        System.out.println("✅ Test database connection successful!");
        System.out.println("✅ All repositories injected successfully!");
    }

    @Test 
    void testUserServiceIntegration() throws Exception {
        // Test that we can actually use the services with the test database
        
        // Register a test user (returns Void, just check no exception is thrown)
        assertDoesNotThrow(() -> {
            userService.register("testuser", "password123");
        }, "User registration should not throw exception");
        
        // Try to login
        var loginResponse = authService.login("testuser", "password123");
        assertNotNull(loginResponse, "Login should return a token");
        assertNotNull(loginResponse.token(), "Token should not be null");
        
        System.out.println("✅ User service integration test successful!");
    }
} 