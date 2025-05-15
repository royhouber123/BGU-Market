package market.domain.store.Policies.Policies;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Policies.PurchasePolicy;
import market.dto.AddPurchasePolicyDTO;

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
    public boolean isPurchaseAllowed(Map<String, Integer> listings, IStoreProductsManager productManager) {
        return listings.values().stream().mapToInt(Integer::intValue).sum() <= maxItems;
    }

    @Override
    public AddPurchasePolicyDTO toDTO() {
        return new AddPurchasePolicyDTO("MAXITEMS", maxItems);
    }
}
