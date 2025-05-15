package market.domain.store.Policies.Policies;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Store;
import market.domain.store.Policies.PurchasePolicy;
import market.dto.AddPurchasePolicyDTO;

import java.util.Map;

public class DefaultPurchasePolicy implements PurchasePolicy {

    @Override
    public boolean isPurchaseAllowed(Map<String, Integer> listings, IStoreProductsManager productManager) {
        return true;
    }
    
    @Override
    public AddPurchasePolicyDTO toDTO() {
        return new AddPurchasePolicyDTO("DEFAULT", 0);
    }
}
