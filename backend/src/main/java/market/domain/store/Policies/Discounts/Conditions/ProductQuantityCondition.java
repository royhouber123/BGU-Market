package market.domain.store.Policies.Discounts.Conditions;

import java.util.Collections;
import java.util.Map;

import market.domain.store.IStoreProductsManager;
import market.dto.PolicyDTO;

public class ProductQuantityCondition implements DiscountCondition {

    private final String productId;
    private final int minQuantity;

    public ProductQuantityCondition(String productId, int minQuantity) {
        this.productId = productId;
        this.minQuantity = minQuantity;
    }

    @Override
    public boolean isSatisfied(Map<String, Integer> listings, IStoreProductsManager productManager) {

        return listings.getOrDefault(productId, 0) >= minQuantity;
    }

    @Override
    public PolicyDTO.DiscountCondition toDTO() {
        return new PolicyDTO.DiscountCondition(
            "PRODUCT_QUANTITY_AT_LEAST",
            Map.of(
                "productId", productId,
                "minQuantity", minQuantity
            ),
            Collections.emptyList(), // no subconditions
            null                     // no logic operator
        );
    }
}