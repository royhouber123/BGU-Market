package tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assumptions;
import support.AcceptanceTestSpringBase;

import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test to verify that our test database configuration works correctly.
 * These tests will be skipped if MySQL is not available.
 */
public class DatabaseConnectionTest extends AcceptanceTestSpringBase {

    @BeforeEach
    void checkMySQLAvailability() {
        // Check if MySQL is available before running tests
        try {
            DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/?useSSL=false", 
                "bgu", 
                "changeme"
            ).close();
        } catch (SQLException e) {
            Assumptions.assumeTrue(false, 
                "MySQL is not available - skipping database integration tests. " +
                "To run these tests, ensure MySQL is running with user 'bgu' and password 'changeme'");
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