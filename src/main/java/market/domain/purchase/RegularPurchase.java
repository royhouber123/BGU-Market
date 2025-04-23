package market.domain.purchase;

import market.model.*;

import java.util.ArrayList;
import java.util.List;

public class RegularPurchase implements IPurchase {

    @Override
    public Purchase purchase(String userId, ShoppingCart cart, String shippingAddress, String contactInfo) {
        List<PurchasedProduct> purchasedItems = new ArrayList<>();
        double total = 0.0;

        for (StoreBag bag : cart.getStoreBags()) {
            for (CartItem item : bag.getItems()) {
                double unitPrice = item.getUnitPrice();
                double discount = item.getDiscountPercentage(); // אחוז הנחה
                double discountedPrice = unitPrice * (1 - discount / 100.0);
                double subtotal = discountedPrice * item.getQuantity();
                total += subtotal;

                purchasedItems.add(new PurchasedProduct(
                        item.getProductId(),
                        bag.getStoreId(),
                        item.getQuantity(),
                        discountedPrice
                ));
            }
        }

        return new Purchase(userId, purchasedItems, total, shippingAddress, contactInfo);
    }
}
