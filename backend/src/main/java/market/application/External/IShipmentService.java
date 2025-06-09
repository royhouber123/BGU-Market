package market.application.External;

import utils.ApiResponse;

public interface IShipmentService {
    String ship(String name, String address, String city, 
                                String country, String zip);
    boolean cancel(String trackingId); 
    // ApiResponse<String> getShippingInfo(String trackingId); 
}