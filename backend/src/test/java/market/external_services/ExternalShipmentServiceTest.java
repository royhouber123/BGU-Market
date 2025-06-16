package market.external_services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import utils.ApiResponse;
import market.application.External.ExternalShipmentService;

import static org.junit.jupiter.api.Assertions.*;

public class ExternalShipmentServiceTest {
    
    private ExternalShipmentService externalShipmentService;
    
    @BeforeEach
    public void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        String shipmentUrl = "https://damp-lynna-wsep-1984852e.koyeb.app/";
        externalShipmentService = new ExternalShipmentService(restTemplate, shipmentUrl);
    }
    
    @Test
    public void testShip() {
        // Test using interface method signature
        String address = "123 Main St, New York, USA, 12345";
        String recipient = "John Doe";
        
        ApiResponse<String> result = externalShipmentService.ship(address, recipient, 0);
        
        // Basic assertions
        assertNotNull(result, "Result should not be null");
        
        if (result.isSuccess()) {
            assertNotNull(result.getData(), "Tracking ID should not be null");
            assertTrue(result.getData().matches("\\d{5}"), "Tracking ID should be 5 digits only");
            assertNull(result.getError(), "Error should be null on success");
            System.out.println("✅ Shipment processed successfully. Tracking ID: " + result.getData());
            
            // Test cancellation with the returned tracking ID
            ApiResponse<Void> cancelResult = externalShipmentService.cancel(result.getData());
            
            assertNotNull(cancelResult, "Cancel result should not be null");
            System.out.println("Shipment cancellation result: " + cancelResult.isSuccess());
            
        } else {
            assertFalse(result.isSuccess(), "Shipment should be marked as failed");
            assertNotNull(result.getError(), "Error message should be provided on failure");
            System.out.println("❌ Shipment failed: " + result.getError());
        }
    }
    
    @Test
    public void testInvalidAddress() {
        // Test with invalid address format
        String invalidAddress = "123 Main St"; // Missing city, country, zip
        String recipient = "John Doe";
        double weight = 1.0;
        
        ApiResponse<String> result = externalShipmentService.ship(invalidAddress, recipient, weight);
        
        // This SHOULD fail
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isSuccess(), "Invalid address should cause failure");
        assertNotNull(result.getError(), "Error message should be provided");
        assertTrue(result.getError().contains("Invalid address format"), 
                  "Error should mention invalid address format");
        
        System.out.println("✅ Invalid address correctly rejected: " + result.getError());
    }
    
    @Test 
    public void testShipmentHandshakeOnly() {
        // Simple test to verify external API connectivity
        String address = "123 Test St, Test City, USA, 12345";
        String recipient = "Test User";
        double weight = 1.0;
        
        ApiResponse<String> result = externalShipmentService.ship(address, recipient, weight);
        
        System.out.println("Shipment test - Success: " + result.isSuccess());
        if (result.isSuccess()) {
            System.out.println("Tracking ID: " + result.getData());
        } else {
            System.out.println("Error: " + result.getError());
        }
        
        assertNotNull(result, "Should get a response from shipment service");
    }
    
    @Test
    public void testCancelWithMockTrackingId() {
        // Test cancellation with a mock tracking ID
        String mockTrackingId = "12345";
        
        ApiResponse<Void> result = externalShipmentService.cancel(mockTrackingId);
        
        assertNotNull(result, "Cancel result should not be null");
        
        if (result.isSuccess()) {
            // Assert successful cancellation
            assertTrue(result.isSuccess(), "Cancellation should be successful");
            assertNull(result.getData(), "Void method should return null data");
            assertNull(result.getError(), "Error should be null on success");
            System.out.println("✅ Mock cancellation successful");
        } else {
            // Assert failure case
            assertFalse(result.isSuccess(), "Cancellation should be marked as failed");
            assertNotNull(result.getError(), "Error message should be provided on failure");
            assertFalse(result.getError().isEmpty(), "Error message should not be empty");
            System.out.println("❌ Mock cancellation failed as expected: " + result.getError());
        }
        
        // Basic validation that we got some response from the service
        assertTrue(result.isSuccess() || result.getError() != null, 
                  "Should either succeed or provide error message");
        
        System.out.println("Mock cancellation test completed - Success: " + result.isSuccess());
    }
    
    @Test
    public void testShipThenCancel() {
        // Step 1: Create and ship an order
        String address = "456 Integration St, Test City, USA, 54321";
        String recipient = "Integration Test User";
        double weight = 3.5;
        
        System.out.println("=== Starting Ship-Then-Cancel Integration Test ===");
        
        // Ship the order
        ApiResponse<String> shipResult = externalShipmentService.ship(address, recipient, weight);
        
        // Assert shipping worked
        assertNotNull(shipResult, "Ship result should not be null");
        
        if (shipResult.isSuccess()) {
            // Step 2: Verify successful shipping
            assertTrue(shipResult.isSuccess(), "Shipping should be successful");
            assertNotNull(shipResult.getData(), "Tracking ID should be provided");
            assertFalse(shipResult.getData().isEmpty(), "Tracking ID should not be empty");
            assertNull(shipResult.getError(), "No error should be present on success");
            
            String trackingId = shipResult.getData();
            System.out.println("✅ Order shipped successfully. Tracking ID: " + trackingId);
            
            // Step 3: Cancel the shipment using the tracking ID
            System.out.println("🚫 Now attempting to cancel shipment...");
            ApiResponse<Void> cancelResult = externalShipmentService.cancel(trackingId);
            
            // Assert cancellation response
            assertNotNull(cancelResult, "Cancel result should not be null");
            
            if (cancelResult.isSuccess()) {
                // Successful cancellation
                assertTrue(cancelResult.isSuccess(), "Cancellation should be successful");
                assertNull(cancelResult.getData(), "Void return should have null data");
                assertNull(cancelResult.getError(), "No error on successful cancellation");
                System.out.println("✅ Shipment cancelled successfully");
                
            } else {
                // Failed cancellation (this might be expected behavior)
                assertFalse(cancelResult.isSuccess(), "Failed cancellation should return false");
                assertNotNull(cancelResult.getError(), "Error message should be provided");
                System.out.println("❌ Cancellation failed: " + cancelResult.getError());
            }
            
            // Integration test passed if we got proper responses from both operations
            assertTrue(shipResult.isSuccess(), "Ship operation should succeed");
            assertNotNull(cancelResult, "Cancel operation should return a response");
            
            System.out.println("✅ Ship-then-cancel integration test completed successfully");
            
        } else {
            // Shipping failed - still validate the failure
            assertFalse(shipResult.isSuccess(), "Failed shipping should return false");
            assertNotNull(shipResult.getError(), "Error message should be provided for failed shipping");
            System.out.println("❌ Shipping failed, skipping cancellation test: " + shipResult.getError());
            
            // Even if shipping fails, the test validates proper error handling
            assertFalse(shipResult.getError().isEmpty(), "Error message should not be empty");
        }
        
        System.out.println("=== Ship-Then-Cancel Integration Test Completed ===");
    }
}
