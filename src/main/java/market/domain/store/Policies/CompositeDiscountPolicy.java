package market.domain.store.Policies;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public double calculateDiscount(Map<String, Integer> listings) {
        double result = 0.0;

        if (combinationType == DiscountCombinationType.SUM) {
            for (DiscountPolicy policy : policies) {
                result += policy.calculateDiscount(listings);
            }
        } else if (combinationType == DiscountCombinationType.MAXIMUM) {
            for (DiscountPolicy policy : policies) {
                double discount = policy.calculateDiscount(listings);
                if (discount > result) {
                    result = discount;
                }
            }
        }

        return result;
    }
}