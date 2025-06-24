package market.domain.store.Policies.Discounts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Policies.DiscountPolicy;
import market.domain.store.Policies.DiscountPolicyEntity;
import market.domain.store.Policies.Discounts.DiscountCombinationType;
import market.dto.PolicyDTO;
import jakarta.persistence.FetchType;

// import market.dto.AddDiscountDTO;

@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "discount_composite")
public class CompositeDiscountPolicy extends DiscountPolicyEntity {

    /**
     * The child policies that compose this composite discount. We persist them in a separate
     * join-table so that a composite can contain an arbitrary number of other policies while
     * retaining proper cascading semantics.
     */
    @jakarta.persistence.OneToMany(
        cascade = jakarta.persistence.CascadeType.ALL,
        targetEntity = DiscountPolicyEntity.class,
        fetch = jakarta.persistence.FetchType.EAGER
    )
    @jakarta.persistence.JoinTable(
        name = "discount_composite_children",
        joinColumns = @jakarta.persistence.JoinColumn(name = "parent_id"),
        inverseJoinColumns = @jakarta.persistence.JoinColumn(name = "child_id")
    )
    private List<DiscountPolicy> policies = new ArrayList<>();

    /**
     * How the child discounts are combined (SUM / MAXIMUM).
     */
    private DiscountCombinationType combinationType;

    protected CompositeDiscountPolicy() { /* JPA */ }

    public CompositeDiscountPolicy(DiscountCombinationType combinationType) {
        this.combinationType = combinationType;
    }

    public void addPolicy(DiscountPolicy policy) {
        if (policies.contains(policy)) {
            throw new IllegalArgumentException("Policy already exists");
        }
        policies.add(policy);
    }

    public void removePolicy(DiscountPolicy policy) {
        if (!policies.contains(policy)) {
            throw new IllegalArgumentException("Policy does not exist");
        }
        policies.remove(policy);
    }

    public List<DiscountPolicy> getPolicies() {
        return policies;
    }

    @Override
    public double calculateDiscount(Map<String, Integer> listings, IStoreProductsManager productManager) {
        double result = 0.0;

        if (combinationType == DiscountCombinationType.SUM) {
            for (DiscountPolicy policy : policies) {
                result += policy.calculateDiscount(listings, productManager);
            }
        } else if (combinationType == DiscountCombinationType.MAXIMUM) {
            for (DiscountPolicy policy : policies) {
                double discount = policy.calculateDiscount(listings, productManager);
                if (discount > result) {
                    result = discount;
                }
            }
        }

        return result;
    }

    public PolicyDTO.AddDiscountRequest toDTO() {
        List<PolicyDTO.AddDiscountRequest> subs = policies.stream()
            .map(DiscountPolicy::toDTO)
            .toList();

        return new PolicyDTO.AddDiscountRequest(
            "COMPOSITE",
            null,
            null,
            0,
            null,
            null,
            subs,
            combinationType.name()
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        CompositeDiscountPolicy other = (CompositeDiscountPolicy) obj;
        return combinationType == other.combinationType &&
               policies.equals(other.policies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(combinationType, policies);
    }
}