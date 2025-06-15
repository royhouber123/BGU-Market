package market.application.External;

import utils.ApiResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "external.services.payment.type", havingValue = "mock")
public class PaymentService implements IPaymentService {
    // This class is a placeholder for the payment service implementation.
    // In a real-world application, this would contain methods to process payments,
    // handle payment gateways, and manage transactions.
    
    @Override
    public ApiResponse<Boolean> processPayment(String paymentDetails) {
        // Logic to process payment
        System.out.println("Processing payment with details: " + paymentDetails);
        return ApiResponse.ok(true);
    }

    @Override
    public ApiResponse<Void> cancelPayment(String paymentId) {
        // Logic to cancel payment
        System.out.println("Cancelling payment with ID: " + paymentId);
        return ApiResponse.ok(null);
    }
    
}