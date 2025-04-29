package market.application.External;

public interface IPaymentService {
    boolean processPayment(String paymentDetails);
    void refundPayment(String paymentId);
    String getPaymentStatus(String paymentId);
    void cancelPayment(String paymentId);
}