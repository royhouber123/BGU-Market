package market.domain.store.Policies.Policies;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Policies.PurchasePolicyEntity;
import market.dto.PolicyDTO;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "policy_min_items")
public class MinItemsPurchasePolicy extends PurchasePolicyEntity {

    private int minItems;

    protected MinItemsPurchasePolicy() {}

    public MinItemsPurchasePolicy(int minItems) {
        if (minItems < 1) {
            throw new IllegalArgumentException("Minimum items must be greater than 0");
        }
        this.minItems = minItems;
    }

    @Override
    public boolean isPurchaseAllowed(Map<String, Integer> listings, IStoreProductsManager productManager) {
        return listings.values().stream().mapToInt(Integer::intValue).sum() >= minItems;
    }


    @Override
    public PolicyDTO.AddPurchasePolicyRequest toDTO() {
        return new PolicyDTO.AddPurchasePolicyRequest("MINITEMS", minItems);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        MinItemsPurchasePolicy that = (MinItemsPurchasePolicy) obj;
        return minItems == that.minItems;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(minItems);
    }
}
