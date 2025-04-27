package market.domain.store.Policies;

import market.domain.store.Listing;
import market.domain.store.Store;

import java.util.Map;

public class MinPricePurchasePolicy implements PurchasePolicy {

    int minPrice;

    public MinPricePurchasePolicy(int minPrice) {
        this.minPrice = minPrice;
    }


    @Override
    public boolean isPurchaseAllowed(final Store store, Map<String, Integer> listings) {
        Listing l;
        int price = 0;
        for (Map.Entry<String, Integer> entry : listings.entrySet()) {
            l = store.getListing(entry.getKey());
            price += entry.getValue() * l.getPrice();
        }
        return price >= minPrice;
    }
}
