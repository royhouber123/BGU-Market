package market.application.External;

import utils.ApiResponse;

public interface IPaymentService {
    ApiResponse<Boolean> processPayment(String paymentDetails);
    //ApiResponse<Void> refundPayment(String paymentId);
    //ApiResponse<String> getPaymentStatus(String paymentId);
    ApiResponse<Void> cancelPayment(String paymentId);
}