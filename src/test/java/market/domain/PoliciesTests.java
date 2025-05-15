package market.domain;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Listing;
import market.domain.store.Policies.DiscountPolicy;
import market.domain.store.Policies.PolicyHandler;
import market.domain.store.Policies.Discounts.CompositeDiscountPolicy;
import market.domain.store.Policies.Discounts.CouponDiscountPolicy;
import market.domain.store.Policies.Discounts.DefaultDiscountPolicy;
import market.domain.store.Policies.Discounts.DiscountCombinationType;
import market.domain.store.Policies.Policies.DefaultPurchasePolicy;
import market.domain.store.Policies.Policies.MaxItemsPurchasePolicy;
import market.domain.store.Policies.Policies.MinItemsPurchasePolicy;
import market.domain.store.Policies.Policies.MinPricePurchasePolicy;

class PoliciesTests {

    private DummyStoreProductsManager dummyStore;

    @BeforeEach
    void setUp() {
        dummyStore = new DummyStoreProductsManager();
        dummyStore.addDummyListing("prod1", 100.0);
        dummyStore.addDummyListing("prod2", 200.0);
    }



    // ---------------------------------------
    // DefaultPurchasePolicy tests
    // ---------------------------------------
    @Test
    void defaultPurchasePolicyAlwaysTrue() {
        DefaultPurchasePolicy policy = new DefaultPurchasePolicy();
        assertTrue(policy.isPurchaseAllowed(new HashMap<>(),dummyStore));
    }

    // ---------------------------------------
    // MaxItemsPurchasePolicy tests
    // ---------------------------------------
    @Test
    void maxItemsPolicyPassesWhenUnderLimit() {
        MaxItemsPurchasePolicy policy = new MaxItemsPurchasePolicy(5);
        Map<String, Integer> cart = Map.of("item1", 3);
        assertTrue(policy.isPurchaseAllowed(cart,dummyStore));
    }

    @Test
    void maxItemsPolicyFailsWhenOverLimit() {
        MaxItemsPurchasePolicy policy = new MaxItemsPurchasePolicy(2);
        Map<String, Integer> cart = Map.of("item1", 3);
        assertFalse(policy.isPurchaseAllowed(cart,dummyStore));
    }

    // ---------------------------------------
    // MinItemsPurchasePolicy tests
    // ---------------------------------------
    @Test
    void minItemsPolicyFailsWhenUnderLimit() {
        MinItemsPurchasePolicy policy = new MinItemsPurchasePolicy(3);
        Map<String, Integer> cart = Map.of("item1", 2);
        assertFalse(policy.isPurchaseAllowed(cart,dummyStore));
    }

    @Test
    void minItemsPolicyPassesWhenAtLimit() {
        MinItemsPurchasePolicy policy = new MinItemsPurchasePolicy(2);
        Map<String, Integer> cart = Map.of("item1", 2);
        assertTrue(policy.isPurchaseAllowed(cart,dummyStore));
    }

    // ---------------------------------------
    // MinPricePurchasePolicy tests
    // ---------------------------------------
    @Test
    void minPricePolicyPassesWhenEnoughPrice() {
        MinPricePurchasePolicy policy = new MinPricePurchasePolicy(150);
        Map<String, Integer> cart = Map.of("prod1", 2); // 100 * 2 = 200

        assertTrue(policy.isPurchaseAllowed(cart,dummyStore));
    }

    @Test
    void minPricePolicyFailsWhenUnderPrice() {
        MinPricePurchasePolicy policy = new MinPricePurchasePolicy(500);
        Map<String, Integer> cart = Map.of("prod1", 2); // 100 * 2 = 200

        assertFalse(policy.isPurchaseAllowed(cart,dummyStore));
    }


    // ---------------------------------------
    // PolicyHandler tests
    // ---------------------------------------
    @Test
    void policyHandlerAllowsDefaultPurchase() {
        PolicyHandler handler = new PolicyHandler();
        assertTrue(handler.isPurchaseAllowed(new HashMap<>(),dummyStore));
    }

    @Test
    void policyHandlerAddRemovePurchasePolicy() {
        PolicyHandler handler = new PolicyHandler();
        MaxItemsPurchasePolicy policy = new MaxItemsPurchasePolicy( 5);
        handler.addPurchasePolicy(policy);
        assertTrue(handler.getPolicies().contains(policy));

        handler.removePurchasePolicy(policy);
        assertFalse(handler.getPolicies().contains(policy));
    }


    static class DummyStoreProductsManager implements IStoreProductsManager {

        private final Map<String, Listing> dummyListings = new HashMap<>();

        public void addDummyListing(String productId, double price) {
            Listing listing = new Listing("store1", productId, "Dummy Product", "category", "A dummy product", 10, market.domain.purchase.PurchaseType.REGULAR, (int) price);
            dummyListings.put(productId, listing);
        }

        @Override
        public Listing getListingById(String listingId) {
            return dummyListings.get(listingId);
        }

        @Override public String addListing(Listing listing) { return ""; }
        @Override public boolean removeListing(String listingId) { return false; }
        @Override public List<Listing> getListingsByProductName(String productName) { return null; }
        @Override public List<Listing> getListingsByProductId(String productId) { return null; }
        @Override public List<Listing> getAllListings() { return null; }
        @Override public boolean purchaseFromListing(String listingId, int quantity) { return false; }
        @Override public void disableAllListings(){}
        @Override public void enableAllListings(){}

    }
}
