package market.application.External;

public class ShipmentService {
    // This class is a placeholder for the shipment service implementation.
    // It can be used to handle shipment-related operations in the future.
    /** Schedules a delivery and returns a tracking id. */
    public String ship(String address, String recipient, double weight) {

        System.out.println("Shipping to: " + address + ", Recipient: " + recipient + ", Weight: " + weight);
        return "trackingId";
    }

    /** Cancels a shipment (best-effort). */
    public void cancel(String trackingId) {
        System.out.println("Cancelling shipment with tracking ID: " + trackingId);
    }

    public String getShippingInfo(String trackingId) {
        // Placeholder for getting shipping information based on tracking ID.
        return "Shipping info for tracking ID: " + trackingId;
    }
}
