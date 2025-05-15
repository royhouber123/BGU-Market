package market.domain.store.Policies.Discounts;

import java.util.Map;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Policies.DiscountPolicy;
import market.domain.store.Policies.Discounts.Conditions.DiscountCondition;
import market.dto.AddDiscountDTO;

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

    public AddDiscountDTO toDTO() {
        AddDiscountDTO innerDto = discount.toDTO();

        return new AddDiscountDTO(
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
}