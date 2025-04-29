package market.application.External;

public interface IShipmentService {
    String ship(String address, String recipient, double weight);
    void cancel(String trackingId); 
    String getShippingInfo(String trackingId); 
}
