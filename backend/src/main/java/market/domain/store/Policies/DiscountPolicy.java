package market.domain.store.Policies;

import market.domain.store.IStoreProductsManager;
import market.dto.PolicyDTO;

import java.util.Map;

public interface DiscountPolicy {
    double calculateDiscount(Map<String, Integer> listings, IStoreProductsManager productManager);
    PolicyDTO.AddDiscountRequest toDTO();
}
