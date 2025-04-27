package market.domain.purchase;

import market.domain.purchase.PurchasedProduct;
import java.util.ArrayList;
import java.util.List;

public class RegularPurchase {

    public Purchase purchase(String userId, List<PurchasedProduct> purchasedItems, String shippingAddress, String contactInfo) {
        double total = 0.0;

        for (PurchasedProduct item : purchasedItems) {
            total += item.getTotalPrice();
        }

        return new Purchase(userId, purchasedItems, total, shippingAddress, contactInfo);
    }
}
