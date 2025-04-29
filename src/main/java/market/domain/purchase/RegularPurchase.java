package market.domain.purchase;

import market.application.External.IPaymentService;
import market.application.External.IShipmentService;
import java.util.List;

public class RegularPurchase {

    public Purchase purchase(String userId, List<PurchasedProduct> purchasedItems, String shippingAddress, String contactInfo, double totalDiscountPrice, IPaymentService paymentService, IShipmentService shipmentService) {
        double total = 0.0;

        for (PurchasedProduct item : purchasedItems) {
            total += item.getUnitPrice();
        }
        total=total-totalDiscountPrice;
        paymentService.processPayment("User: " + userId + ", Amount: " + total);
        double totalWeight = calculateTotalWeight(purchasedItems); 
        shipmentService.ship(shippingAddress, userId, totalWeight);
        return new Purchase(userId, purchasedItems, total, shippingAddress, contactInfo);
    }

    private double calculateTotalWeight(List<PurchasedProduct> products) {
        return products.size(); // Assuming each product has a weight of 1 for simplicity
    }
}
