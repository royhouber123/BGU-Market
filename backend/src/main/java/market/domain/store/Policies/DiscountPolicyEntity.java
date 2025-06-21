package market.domain.store.Policies;

import jakarta.persistence.*;
import market.domain.store.IStoreProductsManager;
import market.domain.store.Store;
import market.dto.PolicyDTO;
import java.util.Map;

/**
 * Base JPA class for discount policies so that they can be persisted per store.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class DiscountPolicyEntity implements DiscountPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    public Long getId() { return id; }
    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }

    @Override
    public abstract double calculateDiscount(Map<String,Integer> listings, IStoreProductsManager pm);

    @Override
    public abstract PolicyDTO.AddDiscountRequest toDTO();
} 