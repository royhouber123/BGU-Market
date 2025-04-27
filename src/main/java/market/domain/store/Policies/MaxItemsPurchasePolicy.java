package market.domain.store.Policies;

import market.domain.store.Store;

import java.util.Map;

public class MaxItemsPurchasePolicy implements PurchasePolicy {

    private int maxItems;
    private final Store store;

    public MaxItemsPurchasePolicy(final Store store, int maxItems) {
        if (maxItems < 1) {
            throw new IllegalArgumentException("Max items must be greater than 0");
        }
        this.maxItems = maxItems;
        this.store = store;
    }

    @Override
    public boolean isPurchaseAllowed(Map<String, Integer> listings) {
        return listings.values().stream().mapToInt(Integer::intValue).sum() <= maxItems;
    }
}
