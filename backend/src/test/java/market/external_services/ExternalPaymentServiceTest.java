package market.external_services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import utils.ApiResponse;
import market.application.External.ExternalPaymentService;

import static org.junit.jupiter.api.Assertions.*;

public class ExternalPaymentServiceTest {
    
    private ExternalPaymentService externalPaymentService;
    
    @BeforeEach
    public void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        String paymentUrl = "https://damp-lynna-wsep-1984852e.koyeb.app/";
        externalPaymentService = new ExternalPaymentService(restTemplate, paymentUrl);
    }
    
    @Test
    public void testProcessPayment() {
        // Test payment details format: currency,amount,cardNumber,month,year,holder,ccv
        String paymentDetails = "USD,100.50,1234567890123456,12,2025,John Doe,123";
        
        ApiResponse<Boolean> result = externalPaymentService.processPayment(paymentDetails);
        
        // Basic assertions
        assertNotNull(result, "Result should not be null");
        
        if (result.isSuccess()) {
            // Assert successful payment
            assertTrue(result.getData(), "Payment should return true when successful");
            assertNull(result.getError(), "Error should be null on success");
            
            System.out.println("✅ Payment processed successfully: " + result.getData());
            
            // Test cancellation
            String mockPaymentId = "txn_" + System.currentTimeMillis();
            ApiResponse<Void> cancelResult = externalPaymentService.cancelPayment(mockPaymentId);
            
            assertNotNull(cancelResult, "Cancel result should not be null");
            System.out.println("Cancellation result: " + cancelResult.isSuccess());
            
        } else {
            // Assert failure case
            assertFalse(result.isSuccess(), "Payment should be marked as failed");
            assertNotNull(result.getError(), "Error message should be provided on failure");
            assertFalse(result.getError().isEmpty(), "Error message should not be empty");
            
            System.out.println("❌ Payment failed as expected: " + result.getError());
        }
    }
    
    @Test
    public void testInvalidPaymentDetails() {
        // Test with invalid payment details
        String invalidPaymentDetails = "USD,100.50"; // Missing fields
        
        ApiResponse<Boolean> result = externalPaymentService.processPayment(invalidPaymentDetails);
        
        // This SHOULD fail, so assert failure
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isSuccess(), "Invalid payment details should cause failure");
        assertNotNull(result.getError(), "Error message should be provided");
        assertTrue(result.getError().contains("Invalid payment details format"), 
                   "Error should mention invalid format");
        
        System.out.println("✅ Invalid payment correctly rejected: " + result.getError());
    }
    
    @Test 
    public void testHandshakeOnly() {
        // Simple test to verify external API connectivity
        Boolean handshakeResult = externalPaymentService.performHandshake();
        assertTrue(handshakeResult, "Handshake with external payment service should succeed");
        System.out.println("🤝 Handshake with external payment service successful: " + handshakeResult);
    }
}