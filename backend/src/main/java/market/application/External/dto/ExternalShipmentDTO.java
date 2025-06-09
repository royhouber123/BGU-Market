package market.application.External.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExternalShipmentDTO {
    
    public record ShipmentRequest(
        @JsonProperty("action_type") String actionType,
        @JsonProperty("name") String name,
        @JsonProperty("address") String address,
        @JsonProperty("city") String city,
        @JsonProperty("country") String country,
        @JsonProperty("zip") String zip
    ) {
        // Factory method for supply action
        public static ShipmentRequest supply(String name, String address, String city, String country, String zip) {
            return new ShipmentRequest("supply", name, address, city, country, zip);
        }
        
        // Factory method for handshake action
        public static ShipmentRequest handshake() {
            return new ShipmentRequest("handshake", null, null, null, null, null);
        }
    }
    
    public record ShipmentResponse(
        @JsonProperty("transaction_id") String transactionId,
        @JsonProperty("error_message") String errorMessage,
        @JsonProperty("status") String status
    ) {
        // Helper methods
        public boolean isSuccessful() {
            return "OK".equals(status) && transactionId != null && !transactionId.trim().isEmpty();
        }
        
        public boolean hasError() {
            return errorMessage != null && !errorMessage.trim().isEmpty();
        }
    }
    
    public record CancelShipmentRequest(
        @JsonProperty("action_type") String actionType,
        @JsonProperty("transaction_id") String transactionId
    ) {
        public static CancelShipmentRequest cancel(String transactionId) {
            return new CancelShipmentRequest("cancel_supply", transactionId);
        }
    }
    
    public record CancelShipmentResponse(
        @JsonProperty("transaction_id") String transactionId,
        @JsonProperty("error_message") String errorMessage,
        @JsonProperty("status") String status
    ) {
        public boolean isSuccessful() {
            return "OK".equals(status);
        }
        
        public boolean hasError() {
            return errorMessage != null && !errorMessage.trim().isEmpty();
        }
    }
}
