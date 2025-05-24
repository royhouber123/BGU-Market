package market.domain.store.Policies.Discounts.Conditions;

import java.util.Map;

import market.domain.store.IStoreProductsManager;
import market.dto.PolicyDTO;

public interface DiscountCondition {
    boolean isSatisfied(Map<String, Integer> listings, IStoreProductsManager productManager);
    PolicyDTO.DiscountCondition toDTO();
}
