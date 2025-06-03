package market.domain.store.Policies.Discounts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Policies.DiscountPolicy;
import market.dto.PolicyDTO;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Policies.DiscountPolicy;
import market.dto.AddDiscountDTO;

public class CompositeDiscountPolicy implements DiscountPolicy {
    private List<DiscountPolicy> policies;
    private DiscountCombinationType combinationType;

    public CompositeDiscountPolicy(DiscountCombinationType combinationType) {
        this.policies = new ArrayList<>();
        this.combinationType = combinationType;
    }

    public void addPolicy(DiscountPolicy policy) {
        if(policies.contains(policy)) {
            throw new IllegalArgumentException("Policy already exists");
        }
        policies.add(policy);
    }

    public void removePolicy(DiscountPolicy policy) {
        if(!policies.contains(policy)) {
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

<<<<<<<< HEAD:src/main/java/market/domain/store/Policies/Discounts/CompositeDiscountPolicy.java
    public AddDiscountDTO toDTO() {
        List<AddDiscountDTO> subs = policies.stream()
            .map(DiscountPolicy::toDTO)
            .toList();

        return new AddDiscountDTO(
========
    public PolicyDTO.AddDiscountRequest toDTO() {
        List<PolicyDTO.AddDiscountRequest> subs = policies.stream()
            .map(DiscountPolicy::toDTO)
            .toList();

        return new PolicyDTO.AddDiscountRequest(
>>>>>>>> 320f278ee54468ddfe901cd5efab26387f38a0c3:backend/src/main/java/market/domain/store/Policies/Discounts/CompositeDiscountPolicy.java
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
<<<<<<<< HEAD:src/main/java/market/domain/store/Policies/Discounts/CompositeDiscountPolicy.java
========

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
>>>>>>>> 320f278ee54468ddfe901cd5efab26387f38a0c3:backend/src/main/java/market/domain/store/Policies/Discounts/CompositeDiscountPolicy.java
}