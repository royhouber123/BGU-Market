package market.domain.store.Policies.Discounts;

import java.util.Map;
import java.util.Objects;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Policies.DiscountPolicy;
import market.domain.store.Policies.Discounts.Conditions.DiscountCondition;
import market.dto.PolicyDTO;

public class ConditionalDiscountPolicy implements DiscountPolicy {

    private final DiscountCondition condition;
    private final DiscountPolicy discount;

    public ConditionalDiscountPolicy(DiscountCondition condition, DiscountPolicy discount) {
        this.condition = condition;
        this.discount = discount;
    }

    @Override
    public double calculateDiscount(Map<String, Integer> listings, IStoreProductsManager productManager) {
        if (condition.isSatisfied(listings, productManager)) {
            return discount.calculateDiscount(listings, productManager);
        }
        return 0.0;
    }

    public PolicyDTO.AddDiscountRequest toDTO() {
        PolicyDTO.AddDiscountRequest innerDto = discount.toDTO();

        return new PolicyDTO.AddDiscountRequest(
            "CONDITIONAL",         // type
            innerDto.scope(),      // scope from inner discount
            innerDto.scopeId(),    // scopeId from inner discount
            innerDto.value(),      // discount value
            innerDto.couponCode(), // coupon code (if any)
            condition.toDTO(),     // the condition DTO for this conditional discount
            null,                  // subDiscounts — not applicable for conditional
            null                   // combinationType — not applicable for conditional
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // Same object reference
        if (obj == null || getClass() != obj.getClass()) return false; // Different class or null object
        ConditionalDiscountPolicy that = (ConditionalDiscountPolicy) obj;
        return condition.equals(that.condition) && discount.equals(that.discount); // Compare fields
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition, discount); // Hash based on fields
    }
}