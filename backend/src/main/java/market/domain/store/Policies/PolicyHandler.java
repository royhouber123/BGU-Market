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
        // Calculate the total price of all items in the cart
        double totalPrice = 0.0;
        for (Map.Entry<String, Integer> entry : listings.entrySet()) {
            var listing = productManager.getListingById(entry.getKey());
            if (listing != null) {
                totalPrice += listing.getPrice() * entry.getValue();
            }
        }
        
        // Calculate the discount from all policies
        double discount = discountPolicy.calculateDiscount(listings, productManager);
        
        // Ensure discount is non-negative and doesn't exceed total price
        discount = Math.max(0.0, discount); // No negative discounts
        discount = Math.min(discount, totalPrice); // Don't exceed total price
        
        return discount;
    }

    public List<PurchasePolicy> getPolicies() {
        return policies;
    }
}
