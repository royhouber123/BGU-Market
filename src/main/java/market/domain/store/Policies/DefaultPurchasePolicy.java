package market.domain.store.Policies;

import market.domain.store.Store;

import java.util.Map;

public class DefaultPurchasePolicy implements PurchasePolicy {

    @Override
    public boolean isPurchaseAllowed(Map<String, Integer> listings) {
        return true;
    }
}
