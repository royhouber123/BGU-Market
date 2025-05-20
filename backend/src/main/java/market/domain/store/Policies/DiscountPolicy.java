package market.domain.store.Policies;

import market.domain.store.IStoreProductsManager;
import market.dto.AddDiscountDTO;

import java.util.Map;

public interface DiscountPolicy {
    double calculateDiscount(Map<String, Integer> listings, IStoreProductsManager productManager);
    AddDiscountDTO toDTO();
}
