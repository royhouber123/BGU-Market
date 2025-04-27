package market.domain.store.Policies;

import market.domain.store.Store;

import java.util.Map;

public class MinItemsPurchasePolicy implements PurchasePolicy {

    private int minItems;

    public MinItemsPurchasePolicy(int minItems) {
        this.minItems = minItems;
    }

    @Override
    public boolean isPurchaseAllowed(final Store store, Map<String, Integer> listings) {
        return listings.values().stream().mapToInt(Integer::intValue).sum() >= minItems;
    }
}
