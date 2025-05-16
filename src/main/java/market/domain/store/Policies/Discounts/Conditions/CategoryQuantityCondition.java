package market.domain.store.Policies.Discounts.Conditions;

import java.util.Collections;
import java.util.Map;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Listing;
import market.dto.DiscountConditionDTO;

public class CategoryQuantityCondition implements DiscountCondition {

    private final String category;
    private final int minQuantity;

    public CategoryQuantityCondition(String category, int minQuantity) {
        this.category = category;
        this.minQuantity = minQuantity;
    }

    @Override
    public boolean isSatisfied(Map<String, Integer> listings, IStoreProductsManager productManager) {
        int count = 0;

        for (Map.Entry<String, Integer> entry : listings.entrySet()) {
            Listing listing = productManager.getListingById(entry.getKey());
            if (listing != null && listing.getCategory().equalsIgnoreCase(category)) {
                count += entry.getValue();
            }
        }

        return count >= minQuantity;
    }

    @Override
    public DiscountConditionDTO toDTO() {
        return new DiscountConditionDTO(
            "CATEGORY_QUANTITY_AT_LEAST",      // type
            Map.of(
                "category", category,
                "minQuantity", minQuantity
            ),
            Collections.emptyList(),           // no subconditions
            null                              // no logic operator
        );
    }
}