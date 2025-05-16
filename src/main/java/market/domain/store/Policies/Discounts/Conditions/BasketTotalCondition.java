package market.domain.store.Policies.Discounts.Conditions;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Listing;
import market.dto.DiscountConditionDTO;

import java.util.Collections;
import java.util.Map;

public class BasketTotalCondition implements DiscountCondition {

    private final double minTotal;

    public BasketTotalCondition(double minTotal) {
        this.minTotal = minTotal;
    }

    @Override
    public boolean isSatisfied(Map<String, Integer> listings, IStoreProductsManager productManager) {
        double total = 0.0;

        for (Map.Entry<String, Integer> entry : listings.entrySet()) {
            Listing listing = productManager.getListingById(entry.getKey());
            if (listing != null) {
                total += listing.getPrice() * entry.getValue();
            }
        }

        return total >= minTotal;
    }
    
    @Override
    public DiscountConditionDTO toDTO() {
        return new DiscountConditionDTO(
            "BASKET_TOTAL_AT_LEAST",      // type
            Map.of("minTotal", minTotal), // params
            Collections.emptyList(),      // no subconditions
            null                         // no logic operator
        );
    }
}