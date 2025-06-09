package market.application.External;

import market.application.External.dto.ExternalShipmentDTO.*;
import market.application.External.config.ExternalServiceConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class ExternalShipmentService implements IShipmentService {
    
    private final RestTemplate restTemplate;
    private final ExternalServiceConfig config;
    
    @Autowired
    public ExternalShipmentService(RestTemplate restTemplate, ExternalServiceConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
        System.out.println("=== ExternalShipmentService initialized ===");
        System.out.println("Shipment URL: " + config.getShipmentUrl());
    }
    
    @Override
    public String ship(String name, String address, String city, 
                       String country, String zip) {
        
        System.out.println("=== Starting Shipment Processing ===");
        System.out.println("Name: " + name);
        System.out.println("Address: " + address);
        System.out.println("City: " + city);
        System.out.println("Country: " + country);
        System.out.println("ZIP: " + zip);
        
        // First perform handshake
        System.out.println("Performing handshake with shipment service...");
        if (!performHandshake()) {
            System.err.println("❌ Shipment handshake failed!");
            throw new RuntimeException("Failed to establish connection with shipment service");
        }
        System.out.println("✅ Shipment handshake successful!");
        
        // Create shipment request
        ShipmentRequest request = ShipmentRequest.supply(name, address, city, country, zip);
        System.out.println("Created shipment request for: " + name);
        
        try {
            System.out.println("Sending shipment request to: " + config.getShipmentUrl());
            
            // Send shipment request
            ShipmentResponse response = sendShipmentRequest(request);
            
            System.out.println("Received shipment response:");
            System.out.println("- Transaction ID: " + response.transactionId());
            System.out.println("- Status: " + response.status());
            System.out.println("- Successful: " + response.isSuccessful());
            System.out.println("- Has Error: " + response.hasError());
            if (response.hasError()) {
                System.out.println("- Error Message: " + response.errorMessage());
            }
            
            if (response.isSuccessful()) {
                System.out.println("✅ Shipment processed successfully!");
                System.out.println("Tracking ID: " + response.transactionId());
                return response.transactionId();
            } else if (response.hasError()) {
                System.err.println("❌ Shipment failed with error!");
                throw new RuntimeException(
                    "Shipment failed: " + response.errorMessage() +
                    ", Transaction ID: " + response.transactionId() +
                    ", Status: " + response.status()
                );
            } else {
                System.err.println("❌ Shipment failed with unknown error!");
                throw new RuntimeException("Shipment failed with unknown error");
            }
            
        } catch (RestClientException e) {
            System.err.println("❌ Network error during shipment processing: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Network error during shipment processing", e);
        } catch (Exception e) {
            System.err.println("❌ Unexpected error during shipment processing: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Unexpected error during shipment processing", e);
        }
    }
    
    @Override
    public boolean cancel(String transactionId) {
        System.out.println("=== Starting Shipment Cancellation ===");
        System.out.println("Transaction ID to cancel: " + transactionId);
        
        CancelShipmentRequest request = CancelShipmentRequest.cancel(transactionId);
        
        try {
            System.out.println("Sending shipment cancellation request to: " + config.getShipmentUrl());
            CancelShipmentResponse response = sendCancelRequest(request);
            
            System.out.println("Received shipment cancellation response:");
            System.out.println("- Transaction ID: " + response.transactionId());
            System.out.println("- Status: " + response.status());
            System.out.println("- Successful: " + response.isSuccessful());
            
            if (response.isSuccessful()) {
                System.out.println("✅ Shipment cancelled successfully!");
                return true;
            } else if (response.hasError()) {
                System.err.println("❌ Shipment cancellation failed!");
                throw new RuntimeException(
                    "Shipment cancellation failed: " + response.errorMessage() +
                    ", Transaction ID: " + response.transactionId() +
                    ", Status: " + response.status()
                );
            }
            return false;
            
        } catch (RestClientException e) {
            System.err.println("❌ Network error during shipment cancellation: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Network error during shipment cancellation", e);
        }
    }
    
    private boolean performHandshake() {
        try {
            System.out.println("Attempting shipment handshake with URL: " + config.getShipmentUrl());
            ShipmentRequest handshakeRequest = ShipmentRequest.handshake();
            System.out.println("Created shipment handshake request");
            
            ShipmentResponse response = sendShipmentRequest(handshakeRequest);
            System.out.println("Shipment handshake response status: " + response.status());
            
            boolean isOk = "OK".equals(response.status());
            System.out.println("Shipment handshake result: " + (isOk ? "SUCCESS" : "FAILED"));
            return isOk;
            
        } catch (Exception e) {
            System.err.println("❌ Shipment handshake failed with exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private ShipmentResponse sendShipmentRequest(ShipmentRequest request) {
        System.out.println("Preparing shipment HTTP request...");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        System.out.println("Shipment headers set: " + headers);
        
        HttpEntity<ShipmentRequest> entity = new HttpEntity<>(request, headers);
        System.out.println("Shipment HTTP entity created");
        
        System.out.println("Making POST request to shipment service: " + config.getShipmentUrl());
        ResponseEntity<ShipmentResponse> response = restTemplate.postForEntity(
            config.getShipmentUrl(),
            entity,
            ShipmentResponse.class
        );
        
        System.out.println("Shipment HTTP Response Status: " + response.getStatusCode());
        System.out.println("Shipment response body received");
        
        return response.getBody();
    }
    
    private CancelShipmentResponse sendCancelRequest(CancelShipmentRequest request) {
        System.out.println("Preparing shipment cancellation HTTP request...");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<CancelShipmentRequest> entity = new HttpEntity<>(request, headers);
        
        System.out.println("Making POST cancellation request to shipment service: " + config.getShipmentUrl());
        ResponseEntity<CancelShipmentResponse> response = restTemplate.postForEntity(
            config.getShipmentUrl(),
            entity,
            CancelShipmentResponse.class
        );
        
        System.out.println("Shipment cancellation HTTP Response Status: " + response.getStatusCode());
        
        return response.getBody();
    }
}
