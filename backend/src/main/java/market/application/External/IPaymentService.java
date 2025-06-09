package market.application.External;

import utils.ApiResponse;

public interface IPaymentService {
    String processPayment(String currency, double amount, String cardNumber, 
                               String month, String year, String holder, String ccv);
    // ApiResponse<Void> refundPayment(String paymentId);
    // ApiResponse<String> getPaymentStatus(String paymentId);
    boolean cancelPayment(String paymentId);
}