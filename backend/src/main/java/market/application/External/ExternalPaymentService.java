package market.application.External;

import market.application.External.dto.ExternalPaymentDTO.*;
import market.application.External.config.ExternalServiceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import javax.management.RuntimeErrorException;

@Service
public class ExternalPaymentService implements IPaymentService {
    
    private final RestTemplate restTemplate;
    private final ExternalServiceConfig config;
    
    @Autowired
    public ExternalPaymentService(RestTemplate restTemplate, ExternalServiceConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
        System.out.println("=== ExternalPaymentService initialized ===");
        System.out.println("Payment URL: " + config.getPaymentUrl());
    }
    
    @Override
    public String processPayment(String currency, double amount, String cardNumber, 
                               String month, String year, String holder, String ccv)  {
        
        System.out.println("=== Starting Payment Processing ===");
        System.out.println("Currency: " + currency);
        System.out.println("Amount: " + amount);
        System.out.println("Card Number: ****" + cardNumber.substring(Math.max(0, cardNumber.length() - 4)));
        System.out.println("Holder: " + holder);
        System.out.println("Month: " + month + ", Year: " + year);
        
        // First perform handshake
        System.out.println("Performing handshake with payment service...");
        if (!performHandshake()) {
            System.err.println("❌ Handshake failed!");
            throw new RuntimeException("Failed to establish connection with payment service");
        }
        System.out.println("✅ Handshake successful!");
        
        // Generate unique transaction ID
        String transactionId = UUID.randomUUID().toString();
        System.out.println("Generated transaction ID: " + transactionId);
        
        // Create payment request
        PaymentRequest request = PaymentRequest.payment(
            currency, amount, cardNumber, month, year, holder, ccv, transactionId
        );
        System.out.println("Created payment request with transaction ID: " + transactionId);
        
        try {
            System.out.println("Sending payment request to: " + config.getPaymentUrl());
            
            // Send payment request
            PaymentResponse response = sendPaymentRequest(request);
            
            System.out.println("Received payment response:");
            System.out.println("- Transaction ID: " + response.transactionId());
            System.out.println("- Status: " + response.status());
            System.out.println("- Successful: " + response.isSuccessful());
            System.out.println("- Has Error: " + response.hasError());
            if (response.hasError()) {
                System.out.println("- Error Message: " + response.errorMessage());
            }
            
            if (response.isSuccessful()) {
                System.out.println("✅ Payment processed successfully!");
                return response.transactionId();
            } else if (response.hasError()) {
                System.err.println("❌ Payment failed with error!");
                throw new RuntimeException(
                    "Payment failed: " + response.errorMessage() +
                    ", Transaction ID: " + response.transactionId() +
                    ", Status: " + response.status()
                );
            } else {
                System.err.println("❌ Payment failed with unknown error!");
                throw new RuntimeException("Payment failed with unknown error");
            }
            
        } catch (RestClientException e) {
            System.err.println("❌ Network error during payment processing: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Network error during payment processing", e);
        } catch (Exception e) {
            System.err.println("❌ Unexpected error during payment processing: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Unexpected error during payment processing", e);
        }
    }
    
    @Override
    public boolean cancelPayment(String transactionId)  {
        System.out.println("=== Starting Payment Cancellation ===");
        System.out.println("Transaction ID to cancel: " + transactionId);
        
        CancelPaymentRequest request = CancelPaymentRequest.cancel(transactionId);
        
        try {
            System.out.println("Sending cancellation request to: " + config.getPaymentUrl());
            CancelPaymentResponse response = sendCancelRequest(request);
            
            System.out.println("Received cancellation response:");
            System.out.println("- Transaction ID: " + response.transactionId());
            System.out.println("- Status: " + response.status());
            System.out.println("- Successful: " + response.isSuccessful());
            
            if (response.isSuccessful()) {
                System.out.println("✅ Payment cancelled successfully!");
                return true;
            } else if (response.hasError()) {
                System.err.println("❌ Payment cancellation failed!");
                throw new RuntimeException(
                    "Payment cancellation failed: " + response.errorMessage() +
                    ", Transaction ID: " + response.transactionId() +
                    ", Status: " + response.status()
                );
            }
            return false;
            
        } catch (RestClientException e) {
            System.err.println("❌ Network error during payment cancellation: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Network error during payment cancellation", e);
        }
    }
    
    private boolean performHandshake() {
        try {
            System.out.println("Attempting handshake with URL: " + config.getPaymentUrl());
            PaymentRequest handshakeRequest = PaymentRequest.handshake();
            System.out.println("Created handshake request");
            
            PaymentResponse response = sendPaymentRequest(handshakeRequest);
            System.out.println("Handshake response status: " + response.status());
            
            boolean isOk = "OK".equals(response.status());
            System.out.println("Handshake result: " + (isOk ? "SUCCESS" : "FAILED"));
            return isOk;
            
        } catch (Exception e) {
            System.err.println("❌ Handshake failed with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private PaymentResponse sendPaymentRequest(PaymentRequest request) {
        System.out.println("Preparing HTTP request...");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        System.out.println("Headers set: " + headers);
        
        HttpEntity<PaymentRequest> entity = new HttpEntity<>(request, headers);
        System.out.println("HTTP entity created");
        
        System.out.println("Making POST request to: " + config.getPaymentUrl());
        ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
            config.getPaymentUrl(),
            entity,
            PaymentResponse.class
        );
        
        System.out.println("HTTP Response Status: " + response.getStatusCode());
        System.out.println("Response body received");
        
        return response.getBody();
    }
    
    private CancelPaymentResponse sendCancelRequest(CancelPaymentRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<CancelPaymentRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<CancelPaymentResponse> response = restTemplate.postForEntity(
            config.getPaymentUrl(),
            entity,
            CancelPaymentResponse.class
        );
        
        return response.getBody();
    }
}
