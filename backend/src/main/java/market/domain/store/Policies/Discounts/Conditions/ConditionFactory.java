package market.domain.store.Policies.Discounts.Conditions;

import java.util.List;

import market.dto.PolicyDTO;

public class ConditionFactory {

    public static DiscountCondition fromDTO(PolicyDTO.DiscountCondition dto) {
        return switch (dto.type().toUpperCase()) {
            case "BASKET_TOTAL_AT_LEAST" ->
                ConditionFactory.basketTotalAtLeast(((Number) dto.params().get("minTotal")).doubleValue());
            case "PRODUCT_QUANTITY_AT_LEAST" ->
                ConditionFactory.productQuantityAtLeast(
                    (String) dto.params().get("productId"),
                    ((Number) dto.params().get("minQuantity")).intValue()
                );
            case "PRODUCT_CATEGORY_CONTAINS" ->
                ConditionFactory.categoryQuantityAtLeast(
                    (String) dto.params().get("category"),
                    ((Number) dto.params().getOrDefault("minQuantity", 1)).intValue()
                );
            case "COMPOSITE" -> {
                List<DiscountCondition> subs = dto.subConditions().stream()
                    .map(ConditionFactory::fromDTO)
                    .toList();
                yield switch (dto.logic().toUpperCase()) {
                    case "AND" -> ConditionFactory.and(subs);
                    case "OR" -> ConditionFactory.or(subs);
                    case "XOR" -> ConditionFactory.xor(subs);
                    default -> throw new IllegalArgumentException("Unsupported logic: " + dto.logic());
                };
            }
            default -> throw new IllegalArgumentException("Unknown condition type: " + dto.type());
        };
    }

    /**
     * Basket total is greater than or equal to the given minimum total.
     */
    public static DiscountCondition basketTotalAtLeast(double threshold) {
        return new BasketTotalCondition(threshold);
    }

    /**
     * Basket contains at least 'minQuantity' of the specified product ID.
     */
    public static DiscountCondition productQuantityAtLeast(String productId, int minQuantity) {
        return new ProductQuantityCondition(productId, minQuantity);
    }

    /**
     * Basket contains any positive quantity of the specified product ID.
     */
    public static DiscountCondition containsProduct(String productId) {
        return new ProductQuantityCondition(productId, 1);
    }

    /**
     * Basket contains at least 'minQuantity' items from a given category.
     */
    public static DiscountCondition categoryQuantityAtLeast(String category, int minQuantity) {
        return new CategoryQuantityCondition(category, minQuantity);
    }

    /**
     * Basket contains any product from the specified category.
     */
    public static DiscountCondition containsCategory(String category) {
        return new CategoryQuantityCondition(category, 1);
    }

    /**
     * Logical AND composition of multiple conditions.
     */
    public static DiscountCondition and(List<DiscountCondition> conditions) {
        return new CompositeCondition(conditions, LogicOperator.AND);
    }

    /**
     * Logical OR composition of multiple conditions.
     */
    public static DiscountCondition or(List<DiscountCondition> conditions) {
        return new CompositeCondition(conditions, LogicOperator.OR);
    }

    /**
     * Logical XOR composition of multiple conditions.
     */
    public static DiscountCondition xor(List<DiscountCondition> conditions) {
        return new CompositeCondition(conditions, LogicOperator.XOR);
    }
}