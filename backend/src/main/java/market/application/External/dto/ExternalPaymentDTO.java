package market.application.External.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExternalPaymentDTO {
    
    public record PaymentRequest(
        @JsonProperty("action_type") String actionType,
        @JsonProperty("currency") String currency,
        @JsonProperty("amount") double amount,
        @JsonProperty("card_number") String cardNumber,
        @JsonProperty("month") String month,
        @JsonProperty("year") String year,
        @JsonProperty("holder") String holder,
        @JsonProperty("ccv") String ccv,
        @JsonProperty("id") String id
    ) {
        // Factory method for payment action
        public static PaymentRequest payment(String currency, double amount, String cardNumber, 
                                           String month, String year, String holder, String ccv, String id) {
            return new PaymentRequest("pay", currency, amount, cardNumber, month, year, holder, ccv, id);
        }
        
        // Factory method for handshake action
        public static PaymentRequest handshake() {
            return new PaymentRequest("handshake", null, 0, null, null, null, null, null, null);
        }
    }
    
    public record PaymentResponse(
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
    
    public record CancelPaymentRequest(
        @JsonProperty("action_type") String actionType,
        @JsonProperty("transaction_id") String transactionId
    ) {
        public static CancelPaymentRequest cancel(String transactionId) {
            return new CancelPaymentRequest("cancel_pay", transactionId);
        }
    }
    
    public record CancelPaymentResponse(
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
