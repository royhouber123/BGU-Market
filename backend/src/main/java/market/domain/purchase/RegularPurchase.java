package market.domain.purchase;

import market.application.External.IPaymentService;
import market.application.External.IShipmentService;

import java.util.List;

public class RegularPurchase {

    public Purchase purchase(String userId, List<PurchasedProduct> purchasedItems, String shippingAddress, String contactInfo, double totalDiscountPrice, IPaymentService paymentService, IShipmentService shipmentService, String currency, String cardNumber, String month, String year, String holder, String ccv) throws IllegalArgumentException, RuntimeException {
        double total = 0.0;
        if(purchasedItems == null || purchasedItems.isEmpty()) {
            throw new IllegalArgumentException("Items list cannot be null or empty");
        }
        for (PurchasedProduct item : purchasedItems) {
            total += item.getUnitPrice() * item.getQuantity();
        }
        total = total - totalDiscountPrice;
        if(total < 0) {
            total = 0; // Ensure total is not negative
        }
        
        // Updated to match interface: processPayment(String currency, double amount, String cardNumber, String month, String year, String holder, String ccv)
        String paymentId = paymentService.processPayment(currency, total, cardNumber, month, year, holder, ccv);
        if (paymentId == null || paymentId.trim().isEmpty()) {
            throw new RuntimeException("Payment failed for user: " + userId);
        }
        
        // Parse shipping address to extract components (assuming format: "Name, Address, City, Country, ZIP")
        String[] addressParts = shippingAddress.split(", ");
        String name = addressParts.length > 0 ? addressParts[0] : holder; // Use holder name if available
        String address = addressParts.length > 1 ? addressParts[1] : shippingAddress;
        String city = addressParts.length > 2 ? addressParts[2] : "Unknown";
        String country = addressParts.length > 3 ? addressParts[3] : "Unknown";
        String zip = addressParts.length > 4 ? addressParts[4] : "00000";
        
        // Updated to match interface: ship(String name, String address, String city, String country, String zip)
        String trackingId = shipmentService.ship(name, address, city, country, zip);
        if (trackingId == null || trackingId.trim().isEmpty()) {
            // If shipment fails, cancel the payment
            paymentService.cancelPayment(paymentId);
            throw new RuntimeException("Shipment failed for user: " + userId);
        }
        
        return new Purchase(userId, purchasedItems, total, shippingAddress, contactInfo);
    }

    private double calculateTotalWeight(List<PurchasedProduct> products) {
        return products.size(); // Assuming each product has a weight of 1 for simplicity
    }
}
