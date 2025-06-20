package market.domain.store.Policies;

import jakarta.persistence.*;
import market.domain.store.IStoreProductsManager;
import market.domain.store.Store;
import market.dto.PolicyDTO;
import java.util.Map;

/**
 * Base JPA class for every concrete purchase policy.
 * Implements the existing PurchasePolicy business interface so
 * existing logic continues to work unmodified.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class PurchasePolicyEntity implements PurchasePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Owning store */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    public Long getId() { return id; }
    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }

    /* The two methods from PurchasePolicy remain abstract – subclasses already
       provide their implementations. */
    @Override
    public abstract boolean isPurchaseAllowed(Map<String,Integer> listings, IStoreProductsManager productManager);

    @Override
    public abstract PolicyDTO.AddPurchasePolicyRequest toDTO();
} 