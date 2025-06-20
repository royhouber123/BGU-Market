package market.domain.store.Policies.Policies;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Policies.PurchasePolicy;
import market.dto.PolicyDTO;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import market.domain.store.Policies.PurchasePolicyEntity;

import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "policy_max_items")
public class MaxItemsPurchasePolicy extends PurchasePolicyEntity {

    private int maxItems;

    protected MaxItemsPurchasePolicy() { /* for JPA */ }

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
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        MaxItemsPurchasePolicy that = (MaxItemsPurchasePolicy) obj;
        return maxItems == that.maxItems;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(maxItems);
    }
}
