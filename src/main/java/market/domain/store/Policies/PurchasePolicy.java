package market.domain.store.Policies;

import market.domain.store.Store;

import java.util.Map;

public interface PurchasePolicy {
    boolean isPurchaseAllowed(Map<String, Integer> listings);
}
