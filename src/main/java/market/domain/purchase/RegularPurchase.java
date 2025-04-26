package market.domain.purchase;



import java.util.ArrayList;
import java.util.List;

public class RegularPurchase implements IPurchase {

    @Override
    public Purchase purchase(String userId, List<PurchasedProduct> purchasedItems, String shippingAddress, String contactInfo) {
        double total = 0.0;

        for (PurchasedProduct item : purchasedItems) {
            total += item.getTotalPrice();
        }

        return new Purchase(userId, purchasedItems, total, shippingAddress, contactInfo);
    }
}
