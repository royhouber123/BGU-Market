package market.domain.store.Policies.Discounts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public AddDiscountDTO toDTO() {
        List<AddDiscountDTO> subs = policies.stream()
            .map(DiscountPolicy::toDTO)
            .toList();

        return new AddDiscountDTO(
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
}