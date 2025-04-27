package market.domain.store.Policies;

import market.domain.store.Store;

import java.util.Map;

public class MaxItemsPurchasePolicy implements PurchasePolicy {

    private int maxItems;

    public MaxItemsPurchasePolicy(int minItems) {
        this.maxItems = minItems;
    }

    @Override
    public boolean isPurchaseAllowed(final Store store, Map<String, Integer> listings) {
        return listings.values().stream().mapToInt(Integer::intValue).sum() <= maxItems;
    }
}
