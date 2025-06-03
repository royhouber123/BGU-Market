package market.domain.store.Policies.Discounts;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Policies.DiscountPolicy;
import market.dto.PolicyDTO;

import java.util.Map;
import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // Same object reference
        if (obj == null || getClass() != obj.getClass()) return false; // Different class or null object
        // Since this class has no fields, objects of this type are equal by default
        return true;
    }

    @Override
    public int hashCode() {
        // Since all instances of DefaultDiscountPolicy are logically equivalent, return a constant value
        return Objects.hash(0); // Or any arbitrary constant value like 0
    }
}
