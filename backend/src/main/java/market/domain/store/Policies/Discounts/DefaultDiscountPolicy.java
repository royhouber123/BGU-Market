package market.domain.store.Policies.Discounts;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Policies.DiscountPolicy;
import market.dto.PolicyDTO;

import java.util.Map;

// The default policy: no discount
public class DefaultDiscountPolicy implements DiscountPolicy {
    @Override
    public double calculateDiscount(Map<String, Integer> listings, IStoreProductsManager productManager) {
        return 0;
    }

    @Override
    public PolicyDTO.AddDiscountRequest toDTO() {
        return new PolicyDTO.AddDiscountRequest(
            "DEFAULT",     // type
            null,          // scope
            null,          // scopeId
            0.0,           // value
            null,          // couponCode
            null,          // condition
            null,          // subDiscounts
            null           // combinationType
        );
    }
}
