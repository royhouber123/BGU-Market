package market.domain.store.Policies;

import java.util.Map;

import market.domain.store.IStoreProductsManager;
import market.dto.PolicyDTO;

public interface PurchasePolicy {
    boolean isPurchaseAllowed(Map<String, Integer> listings, IStoreProductsManager productManager);
    PolicyDTO.AddPurchasePolicyRequest toDTO();
}
