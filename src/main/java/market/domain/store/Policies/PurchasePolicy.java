package market.domain.store.Policies;

import market.domain.store.Store;

import java.util.Map;

public interface PurchasePolicy {
    boolean isPurchaseAllowed(final Store srore, Map<String, Integer> listings);
}
