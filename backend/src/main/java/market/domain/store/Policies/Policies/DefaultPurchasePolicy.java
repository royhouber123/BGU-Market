package market.domain.store.Policies.Policies;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Policies.PurchasePolicy;
import market.dto.PolicyDTO;

import java.util.Map;
import java.util.Objects;

public class DefaultPurchasePolicy implements PurchasePolicy {

    @Override
    public boolean isPurchaseAllowed(Map<String, Integer> listings, IStoreProductsManager productManager) {
        return true;
    }
    
    @Override
    public PolicyDTO.AddPurchasePolicyRequest toDTO() {
        return new PolicyDTO.AddPurchasePolicyRequest("DEFAULT", 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // Same object reference
        if (obj == null || getClass() != obj.getClass()) return false; // Different class or null object
        // Since this class has no fields, all instances are considered equal
        return true;
    }

    @Override
    public int hashCode() {
        // Since all instances of DefaultPurchasePolicy are logically equivalent, return a constant value
        return Objects.hash(0); // Or any arbitrary constant value like 0
    }
}
