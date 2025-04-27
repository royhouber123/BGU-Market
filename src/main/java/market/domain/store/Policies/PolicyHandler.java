package market.domain.store.Policies;

import market.domain.store.Store;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PolicyHandler {

    private final Store store;

    private List<PurchasePolicy> policies;
    private CompositeDiscountPolicy discountPolicy;

    public PolicyHandler(Store store) {
        this.store = store;
        policies = new ArrayList<>();
        policies.add(new DefaultPurchasePolicy());
        discountPolicy = new CompositeDiscountPolicy(DiscountCombinationType.SUM);
    }

    public PolicyHandler(Store store, DiscountCombinationType type) {
        this.store = store;
        policies = new ArrayList<>();
        policies.add(new DefaultPurchasePolicy());
        discountPolicy = new CompositeDiscountPolicy(type);
    }

    // Add a new purchase policy
    public void addPurchasePolicy(PurchasePolicy policy) {
        policies.add(policy);
    }

    // Add a new discount policy
    public void addDiscountPolicy(DiscountPolicy discount) {
        discountPolicy.addPolicy(discount);
    }

    // Check if purchase is allowed (all policies must approve)
    public boolean isPurchaseAllowed(Map<String, Integer> listings) {
        for (PurchasePolicy policy : policies) {
            if (!policy.isPurchaseAllowed(listings)) {
                return false;
            }
        }
        return true;
    }

    // Calculate total discount
    public double calculateDiscount(Map<String, Integer> listings) {
        return discountPolicy.calculateDiscount(listings);
    }

}
