package market.domain.store.Policies.Discounts;

import java.util.Map;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Listing;
import market.domain.store.Policies.DiscountPolicy;
import market.dto.PolicyDTO;

public class PercentageTargetedDiscount implements DiscountPolicy {

    private final DiscountTargetType targetType;
    private final String targetId;
    private final double percentage;

    public PercentageTargetedDiscount(DiscountTargetType targetType, String targetId, double percentage) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.percentage = percentage;
    }

    @Override
    public double calculateDiscount(Map<String, Integer> listings, IStoreProductsManager productManager) {
        double discount = 0.0;

        for (Map.Entry<String, Integer> entry : listings.entrySet()) {
            Listing listing = productManager.getListingById(entry.getKey());
            if (listing == null) continue;

            if (matches(listing)) {
                discount += listing.getPrice() * entry.getValue() * (percentage / 100.0);
            }
        }

        return discount;
    }

    private boolean matches(Listing listing) {
        return switch (targetType) {
            case STORE -> true;
            case PRODUCT -> listing.getProductId().equals(targetId);
            case CATEGORY -> listing.getCategory().equalsIgnoreCase(targetId);
        };
    }

    @Override
    public PolicyDTO.AddDiscountRequest toDTO() {
        return new PolicyDTO.AddDiscountRequest(
            "PERCENTAGE",              // type
            targetType.name(),         // scope (STORE, PRODUCT, CATEGORY)
            targetId,                  // scopeId
            percentage,                // value
            null,                      // couponCode (not used)
            null,                      // condition (not used)
            null,                      // subDiscounts (not composite)
            null                       // combinationType (not composite)
        );
    }
}