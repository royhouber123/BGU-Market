package market.domain.store.Policies.Discounts.Conditions;

import java.util.Map;

import market.domain.store.IStoreProductsManager;
import market.dto.DiscountConditionDTO;

public interface DiscountCondition {
    boolean isSatisfied(Map<String, Integer> listings, IStoreProductsManager productManager);
    DiscountConditionDTO toDTO();
}
