package market.domain.store.Policies.Discounts;

import market.domain.store.Policies.DiscountPolicy;
import market.domain.store.Policies.Discounts.Conditions.ConditionFactory;
import market.domain.store.Policies.Discounts.Conditions.DiscountCondition;
import market.dto.PolicyDTO;

import java.util.List;

public class DiscountPolicyFactory {

    public static DiscountPolicy fromDTO(PolicyDTO.AddDiscountRequest dto) {
        String type = dto.type().toUpperCase();

        return switch (type) {
            case "PERCENTAGE" -> {
                DiscountTargetType targetType = DiscountTargetType.valueOf(dto.scope().toUpperCase());
                yield new PercentageTargetedDiscount(targetType, dto.scopeId(), dto.value());
            }

            case "FIXED" -> {
                DiscountTargetType targetType = DiscountTargetType.valueOf(dto.scope().toUpperCase());
                yield new FixedDiscountPolicy(targetType, dto.scopeId(), dto.value());
            }

            case "COUPON" -> new CouponDiscountPolicy(dto.couponCode(), dto.value());

            case "CONDITIONAL" -> {
                // Create the inner discount based on the first sub-discount or infer from type
                DiscountPolicy inner;
                if (dto.subDiscounts() != null && !dto.subDiscounts().isEmpty()) {
                    // Use the first sub-discount as the base discount
                    inner = fromDTO(dto.subDiscounts().get(0));
                } else {
                    // Fallback: infer from the main type (this maintains backward compatibility)
                    DiscountTargetType targetType = DiscountTargetType.valueOf(dto.scope().toUpperCase());
                    inner = new PercentageTargetedDiscount(targetType, dto.scopeId(), dto.value());
                }
                DiscountCondition condition = ConditionFactory.fromDTO(dto.condition());
                yield new ConditionalDiscountPolicy(condition, inner);
            }

            case "COMPOSITE" -> {
                DiscountCombinationType combinationType = DiscountCombinationType.valueOf(dto.combinationType().toUpperCase());
                CompositeDiscountPolicy composite = new CompositeDiscountPolicy(combinationType);
                for (PolicyDTO.AddDiscountRequest sub : dto.subDiscounts()) {
                    composite.addPolicy(fromDTO(sub));  // recursive
                }
                yield composite;
            }

            default -> throw new IllegalArgumentException("Unsupported discount type: " + dto.type());
        };
    }

    /**
     * Creates a flat percentage discount for a specific target:
     * - STORE: applies to all listings
     * - PRODUCT: only to a specific productId
     * - CATEGORY: only to listings from that category
     */
    public static DiscountPolicy createPercentageTargetedDiscount(
            DiscountTargetType targetType,
            String targetId,
            double percentage
    ) {
        return new PercentageTargetedDiscount(targetType, targetId, percentage);
    }

    /**
     * Wraps a discount with a condition: applies the discount only if the condition is satisfied.
     */
    public static DiscountPolicy createConditionalDiscount(
            DiscountCondition condition,
            DiscountPolicy discount
    ) {
        return new ConditionalDiscountPolicy(condition, discount);
    }

    /**
     * Composes multiple discounts into one composite policy.
     * Combination type can be SUM or MAXIMUM.
     */
    public static DiscountPolicy createCompositeDiscount(
            DiscountCombinationType combinationType,
            List<DiscountPolicy> policies
    ) {
        CompositeDiscountPolicy composite = new CompositeDiscountPolicy(combinationType);
        for (DiscountPolicy p : policies) {
            composite.addPolicy(p);
        }
        return composite;
    }

    /**
     * Creates a fixed-amount coupon discount that must be activated via .submitCoupon().
     */
    public static CouponDiscountPolicy createCouponDiscount(
            String couponCode,
            double amount
    ) {
        return new CouponDiscountPolicy(couponCode, amount);
    }

    /**
     * Creates a fixed amount discount for a specific target:
     * - STORE: applies fixed amount once to entire order
     * - PRODUCT: applies fixed amount per quantity of specific productId
     * - CATEGORY: applies fixed amount per quantity of items from that category
     */
    public static DiscountPolicy createFixedDiscount(
            DiscountTargetType targetType,
            String targetId,
            double fixedAmount
    ) {
        return new FixedDiscountPolicy(targetType, targetId, fixedAmount);
    }
}