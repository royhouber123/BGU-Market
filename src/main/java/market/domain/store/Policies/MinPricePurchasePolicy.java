package market.domain.store.Policies;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Listing;
import market.domain.store.Store;

import java.util.Map;

public class MinPricePurchasePolicy implements PurchasePolicy {

    int minPrice;
    final IStoreProductsManager store;

    public MinPricePurchasePolicy(final IStoreProductsManager store, int minPrice) {
        if (minPrice < 0) {
            throw new IllegalArgumentException("minPrice cannot be negative");
        }
        this.minPrice = minPrice;
        this.store = store;
    }


    @Override
    public boolean isPurchaseAllowed(Map<String, Integer> listings) {
        Listing l;
        double price = 0;
        for (Map.Entry<String, Integer> entry : listings.entrySet()) {
            l = store.getListingById(entry.getKey());
            if(l == null) {
                throw new IllegalArgumentException("Listing " + entry.getKey() + " not found");
            }
            price += entry.getValue() * l.getPrice();
        }
        return price >= minPrice;
    }
}
