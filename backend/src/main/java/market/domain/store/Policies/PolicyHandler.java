package market.domain.store.Policies;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Store;
import market.domain.store.Policies.Discounts.CompositeDiscountPolicy;
import market.domain.store.Policies.Discounts.DiscountCombinationType;
import market.domain.store.Policies.Policies.DefaultPurchasePolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PolicyHandler {

    private List<PurchasePolicy> policies;
    private CompositeDiscountPolicy discountPolicy;

    public PolicyHandler() {
        policies = new ArrayList<>();
        policies.add(new DefaultPurchasePolicy());
        discountPolicy = new CompositeDiscountPolicy(DiscountCombinationType.SUM);
    }

    public PolicyHandler(Store store, DiscountCombinationType type) {
        policies = new ArrayList<>();
        policies.add(new DefaultPurchasePolicy());
        discountPolicy = new CompositeDiscountPolicy(type);
    }

    // Add a new purchase policy
    public void addPurchasePolicy(PurchasePolicy policy) {
        if(policies.contains(policy)) {
            throw new IllegalArgumentException("Policy already exists");
        }
        policies.add(policy);
    }


    public void removePurchasePolicy(PurchasePolicy policy) {
        if(!policies.contains(policy)) {
            throw new IllegalArgumentException("Policy does not exist");
        }
        policies.remove(policy);
    }

    // Add a new discount policy
    public void addDiscountPolicy(DiscountPolicy discount) {
        discountPolicy.addPolicy(discount);
    }

    public void removeDiscountPolicy(DiscountPolicy discount) {
        discountPolicy.removePolicy(discount);
    }

    public List<DiscountPolicy> getDiscountPolicies() {
        return discountPolicy.getPolicies();
    }

    // Check if purchase is allowed (all policies must approve)
    public boolean isPurchaseAllowed(Map<String, Integer> listings, IStoreProductsManager productManager ) {
        for (PurchasePolicy policy : policies) {
            if (!policy.isPurchaseAllowed(listings, productManager)) {
                return false;
            }
        }
        return true;
    }

    // Calculate total discount
    public double calculateDiscount(Map<String, Integer> listings, IStoreProductsManager productManager) {
        return discountPolicy.calculateDiscount(listings, productManager);
    }

    public List<PurchasePolicy> getPolicies() {
        return policies;
    }
}
