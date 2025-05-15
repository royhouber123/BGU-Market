package market.domain.store.Policies.Policies;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Store;
import market.domain.store.Policies.PurchasePolicy;
import market.dto.AddPurchasePolicyDTO;

import java.util.Map;

public class MinItemsPurchasePolicy implements PurchasePolicy {

    private int minItems;

    public MinItemsPurchasePolicy(int minItems) {
        if (minItems < 1) {
            throw new IllegalArgumentException("Minimum items must be greater than 0");
        }
        this.minItems = minItems;
    }

    @Override
    public boolean isPurchaseAllowed(Map<String, Integer> listings, IStoreProductsManager productManager) {
        return listings.values().stream().mapToInt(Integer::intValue).sum() >= minItems;
    }


    @Override
    public AddPurchasePolicyDTO toDTO() {
        return new AddPurchasePolicyDTO("MINITEMS", minItems);
    }
}
