package market.domain.store.Policies;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Store;

import java.util.Map;

public class MaxItemsPurchasePolicy implements PurchasePolicy {

    private int maxItems;

    public MaxItemsPurchasePolicy(int maxItems) {
        if (maxItems < 1) {
            throw new IllegalArgumentException("Max items must be greater than 0");
        }
        this.maxItems = maxItems;
    }

    @Override
    public boolean isPurchaseAllowed(Map<String, Integer> listings) {
        return listings.values().stream().mapToInt(Integer::intValue).sum() <= maxItems;
    }
}
