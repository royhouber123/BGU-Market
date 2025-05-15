package market.application.External;

import utils.ApiResponse;

public interface IShipmentService {
    ApiResponse<String> ship(String address, String recipient, double weight);
    ApiResponse<Void> cancel(String trackingId); 
    ApiResponse<String> getShippingInfo(String trackingId); 
}