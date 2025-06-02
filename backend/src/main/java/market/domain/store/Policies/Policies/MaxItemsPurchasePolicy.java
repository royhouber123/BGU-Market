package market.domain.store.Policies.Policies;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Policies.PurchasePolicy;
import market.dto.PolicyDTO;

import java.util.Map;
import java.util.Objects;

public class MaxItemsPurchasePolicy implements PurchasePolicy {

    private int maxItems;

    public MaxItemsPurchasePolicy(int maxItems) {
        if (maxItems < 1) {
            throw new IllegalArgumentException("Max items must be greater than 0");
        }
        this.maxItems = maxItems;
    }

    @Override
    public boolean isPurchaseAllowed(Map<String, Integer> listings, IStoreProductsManager productManager) {
        return listings.values().stream().mapToInt(Integer::intValue).sum() <= maxItems;
    }

    @Override
    public PolicyDTO.AddPurchasePolicyRequest toDTO() {
        return new PolicyDTO.AddPurchasePolicyRequest("MAXITEMS", maxItems);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // Same object reference
        if (obj == null || getClass() != obj.getClass()) return false; // Null or different class
        MaxItemsPurchasePolicy that = (MaxItemsPurchasePolicy) obj;
        return maxItems == that.maxItems; // Compare maxItems field
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxItems); // Use maxItems field for hash code calculation
    }
}
