package market.application.External;


import utils.ApiResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "external.services.shipment.type", havingValue = "mock")
public class ShipmentService implements IShipmentService {
    // This class is a placeholder for the shipment service implementation.
    // It can be used to handle shipment-related operations in the future.
    /** Schedules a delivery and returns a tracking id. */
    @Override
    public ApiResponse<String> ship(String address, String recipient, double weight) {

        System.out.println("Shipping to: " + address + ", Recipient: " + recipient + ", Weight: " + weight);
        return ApiResponse.ok("trackingId");
    }

    /** Cancels a shipment (best-effort). */
    @Override
    public ApiResponse<Void> cancel(String trackingId) {
        System.out.println("Cancelling shipment with tracking ID: " + trackingId);
        return ApiResponse.ok(null);
    }
    

    // @Override
    // public ApiResponse<String> getShippingInfo(String trackingId) {
    //     // Placeholder for getting shipping information based on tracking ID.
    //     return ApiResponse.ok("Shipping info for tracking ID: " + trackingId);
    // }
}