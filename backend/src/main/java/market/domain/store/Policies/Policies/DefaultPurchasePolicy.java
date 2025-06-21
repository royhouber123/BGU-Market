package market.domain.store.Policies.Policies;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Policies.PurchasePolicyEntity;
import market.dto.PolicyDTO;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Map;
import java.util.Objects;

@Entity
@Table(name="policy_default")
public class DefaultPurchasePolicy extends PurchasePolicyEntity {

    public DefaultPurchasePolicy() {}

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
        if (this == obj) return true;
        return obj != null && getClass() == obj.getClass();
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
