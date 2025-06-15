package market.application.External;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import utils.ApiResponse;

@Service
@ConditionalOnProperty(name = "external.services.payment.type", havingValue = "external")
public class ExternalPaymentService implements IPaymentService {
    
    private final RestTemplate restTemplate;
    private final String paymentUrl;
    
    public ExternalPaymentService(RestTemplate restTemplate, 
                                 @Value("${external.payment.url:https://damp-lynna-wsep-1984852e.koyeb.app/}") String paymentUrl) {
        this.restTemplate = restTemplate;
        this.paymentUrl = paymentUrl;
        System.out.println("ExternalPaymentService initialized with URL: " + paymentUrl);
    }
    
    @Override
    public ApiResponse<Boolean> processPayment(String paymentDetails) {
        try {
            // Step 1: Perform handshake
            System.out.println("🤝 Performing handshake...");
            boolean handshakeSuccess = performHandshake();
            if (!handshakeSuccess) {
                return ApiResponse.fail("Handshake with payment service failed");
            }
            
            // Step 2: Parse payment details
            System.out.println("💳 Parsing payment details: " + paymentDetails);
            String[] details = paymentDetails.split(",");
            if (details.length < 7) {
                return ApiResponse.fail("Invalid payment details format. Expected: currency,amount,cardNumber,month,year,holder,ccv");
            }
            
            String currency = details[0].trim();
            String amount = details[1].trim();
            String cardNumber = details[2].trim();
            String month = details[3].trim();
            String year = details[4].trim();
            String holder = details[5].trim();
            String cvv = details[6].trim();
            String transactionId = "txn_" + System.currentTimeMillis();
            
            // Step 3: Make payment request
            System.out.println("💳 Processing payment...");
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("action_type", "pay");
            formData.add("currency", currency);
            formData.add("amount", amount);
            formData.add("card_number", cardNumber);
            formData.add("month", month);
            formData.add("year", year);
            formData.add("holder", holder);
            formData.add("cvv", cvv);
            formData.add("id", transactionId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(paymentUrl, entity, String.class);
            String responseBody = response.getBody();
            
            System.out.println("💳 Payment response: " + responseBody);
            System.out.println("💳 Transaction ID: " + transactionId);
            
            // Step 4: Parse response
            // Check if response contains a transaction ID of exactly 5 digits
            if (responseBody != null) {
                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\b\\d{5}\\b").matcher(responseBody);
                if (matcher.find()) {
                    return ApiResponse.ok(true);
                }
            }
            return ApiResponse.fail("Payment processing failed: " + responseBody);
            
        } catch (Exception e) {
            System.err.println("❌ Payment processing failed: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.fail("Payment service error: " + e.getMessage());
        }
    }
    
    @Override
    public ApiResponse<Void> cancelPayment(String paymentId) {
        try {
            // Step 1: Perform handshake
            System.out.println("🤝 Performing handshake for cancellation...");
            boolean handshakeSuccess = performHandshake();
            if (!handshakeSuccess) {
                return ApiResponse.fail("Handshake with payment service failed");
            }
            
            // Step 2: Make cancellation request
            System.out.println("🚫 Cancelling payment: " + paymentId);
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("action_type", "cancel_pay");
            formData.add("transaction_id", paymentId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(paymentUrl, entity, String.class);
            String responseBody = response.getBody();
            
            System.out.println("🚫 Cancellation response: " + responseBody);
            
            // Step 3: Parse response
            if (responseBody != null && (responseBody.contains("\"status\":\"success\"") || responseBody.contains("OK"))) {
                return ApiResponse.ok(null); // Success with void (null data)
            } else {
                return ApiResponse.fail("Payment cancellation failed: " + responseBody);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Payment cancellation failed: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.fail("Payment cancellation error: " + e.getMessage());
        }
    }
    
    public boolean performHandshake() {
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("action_type", "handshake");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(paymentUrl, entity, String.class);
            String responseBody = response.getBody();
            
            System.out.println("🤝 Handshake response: " + responseBody);
            
            // Check if handshake was successful
            return response.getStatusCode().is2xxSuccessful() && 
                   responseBody != null && 
                   (responseBody.contains("\"status\":\"success\"") || responseBody.contains("OK") || !responseBody.contains("error"));
            
        } catch (Exception e) {
            System.err.println("❌ Handshake failed: " + e.getMessage());
            return false;
        }
    }
}
