package market.domain.purchase;

import market.application.External.IPaymentService;
import market.application.External.IShipmentService;
import utils.ApiResponse;

import java.util.List;

public class RegularPurchase {

    public Purchase purchase(String userId, List<PurchasedProduct> purchasedItems, String shippingAddress, String paymentDetails, double totalDiscountPrice, IPaymentService paymentService, IShipmentService shipmentService) throws IllegalArgumentException, RuntimeException {
        double total = 0.0;
        if(purchasedItems == null || purchasedItems.isEmpty()) {
            throw new IllegalArgumentException("Items list cannot be null or empty");
        }
        for (PurchasedProduct item : purchasedItems) {
            total += item.getUnitPrice() * item.getQuantity();
        }
        total=total-totalDiscountPrice;
        if(total < 0) {
            total = 0; // Ensure total is not negative
        }
        ApiResponse<Boolean> paymentResponse = paymentService.processPayment(paymentDetails);
        if (!paymentResponse.isSuccess() || paymentResponse.getData() == null || !paymentResponse.getData()) {
            throw new RuntimeException("Payment failed for user: " + userId);
        }
        double totalWeight = calculateTotalWeight(purchasedItems); 
        shipmentService.ship(shippingAddress, userId, totalWeight);
        return new Purchase(userId, purchasedItems, total, shippingAddress, paymentDetails);
    }

    private double calculateTotalWeight(List<PurchasedProduct> products) {
        return products.size(); // Assuming each product has a weight of 1 for simplicity
    }
}
