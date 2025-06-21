package market.domain.store.Policies.Discounts;

import java.util.Map;
import java.util.Objects;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Listing;
import market.domain.store.Policies.DiscountPolicyEntity;
import market.dto.PolicyDTO;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "discount_percentage_targeted")
public class PercentageTargetedDiscount extends DiscountPolicyEntity {

    private DiscountTargetType targetType;
    private String targetId;
    private double percentage;

    protected PercentageTargetedDiscount() { /* JPA */ }

    public PercentageTargetedDiscount(String targetId, double percentage, DiscountTargetType targetType) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.percentage = percentage;
    }

    // Legacy constructor (kept for backward compatibility)
    public PercentageTargetedDiscount(DiscountTargetType targetType, String targetId, double percentage) {
        this(targetId, percentage, targetType);
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        PercentageTargetedDiscount that = (PercentageTargetedDiscount) obj;
        return Double.compare(that.percentage, percentage) == 0 &&
               targetType == that.targetType &&
               (targetId != null ? targetId.equals(that.targetId) : that.targetId == null);
    }

    @Override
    public int hashCode() {
        int result = targetType != null ? targetType.hashCode() : 0;
        result = 31 * result + (targetId != null ? targetId.hashCode() : 0);
        long temp = Double.doubleToLongBits(percentage);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}