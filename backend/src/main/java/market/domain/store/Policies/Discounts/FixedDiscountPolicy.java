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
@Table(name = "discount_fixed")
public class FixedDiscountPolicy extends DiscountPolicyEntity {

    private DiscountTargetType targetType;
    private String targetId;
    private double fixedAmount;

    protected FixedDiscountPolicy() { /* JPA */ }

    public FixedDiscountPolicy(String targetId, double fixedAmount, DiscountTargetType targetType) {
        if (fixedAmount < 0) {
            throw new IllegalArgumentException("Fixed discount amount must be non-negative");
        }
        this.targetType = targetType;
        this.targetId = targetId;
        this.fixedAmount = fixedAmount;
    }

    // Legacy constructor (kept for backward compatibility)
    public FixedDiscountPolicy(DiscountTargetType targetType, String targetId, double fixedAmount) {
        this(targetId, fixedAmount, targetType);
    }

    @Override
    public double calculateDiscount(Map<String, Integer> listings, IStoreProductsManager productManager) {
        double discount = 0.0;
        double totalPrice = 0.0;

        // First, calculate the total price of relevant items
        switch (targetType) {
            case STORE -> {
                // Calculate total price of entire order
                for (Map.Entry<String, Integer> entry : listings.entrySet()) {
                    Listing listing = productManager.getListingById(entry.getKey());
                    if (listing != null) {
                        totalPrice += listing.getPrice() * entry.getValue();
                    }
                }
                // Apply fixed discount once to the entire store order
                if (!listings.isEmpty()) {
                    discount = fixedAmount;
                }
            }
            case PRODUCT -> {
                // Calculate total price and discount for the specific product
                for (Map.Entry<String, Integer> entry : listings.entrySet()) {
                    Listing listing = productManager.getListingById(entry.getKey());
                    if (listing != null && listing.getProductId().equals(targetId)) {
                        double itemTotal = listing.getPrice() * entry.getValue();
                        totalPrice += itemTotal;
                        discount += fixedAmount * entry.getValue();
                    }
                }
            }
            case CATEGORY -> {
                // Calculate total price and discount for items in the category
                for (Map.Entry<String, Integer> entry : listings.entrySet()) {
                    Listing listing = productManager.getListingById(entry.getKey());
                    if (listing != null && listing.getCategory().equalsIgnoreCase(targetId)) {
                        double itemTotal = listing.getPrice() * entry.getValue();
                        totalPrice += itemTotal;
                        discount += fixedAmount * entry.getValue();
                    }
                }
            }
        }

        // 🔧 KEY FIX: Ensure discount doesn't exceed the total price
        return Math.min(discount, totalPrice);
    }

    @Override
    public PolicyDTO.AddDiscountRequest toDTO() {
        return new PolicyDTO.AddDiscountRequest(
            "FIXED",                   // type
            targetType.name(),         // scope (STORE, PRODUCT, CATEGORY)
            targetId,                  // scopeId
            fixedAmount,               // value
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
        
        FixedDiscountPolicy that = (FixedDiscountPolicy) obj;
        return Double.compare(that.fixedAmount, fixedAmount) == 0 &&
               targetType == that.targetType &&
               (targetId != null ? targetId.equals(that.targetId) : that.targetId == null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetType, targetId, fixedAmount);
    }

    // Getters for testing/debugging
    public DiscountTargetType getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }
    public double getFixedAmount() { return fixedAmount; }
}
