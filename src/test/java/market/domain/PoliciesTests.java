package market.domain;


import market.domain.store.IStoreProductsManager;
import market.domain.store.Listing;
import market.domain.store.Policies.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PoliciesTests {

    private DummyStoreProductsManager dummyStore;

    @BeforeEach
    void setUp() {
        dummyStore = new DummyStoreProductsManager();
        dummyStore.addDummyListing("prod1", 100.0);
        dummyStore.addDummyListing("prod2", 200.0);
    }

    // ---------------------------------------
    // CompositeDiscountPolicy tests
    // ---------------------------------------
    @Test
    void compositeDiscountPolicySumCombination() {
        CompositeDiscountPolicy policy = new CompositeDiscountPolicy(DiscountCombinationType.SUM);
        policy.addPolicy(new DummyDiscountPolicy(10.0));
        policy.addPolicy(new DummyDiscountPolicy(5.0));

        double discount = policy.calculateDiscount(new HashMap<>());
        assertEquals(15.0, discount);
    }

    @Test
    void compositeDiscountPolicyMaximumCombination() {
        CompositeDiscountPolicy policy = new CompositeDiscountPolicy(DiscountCombinationType.MAXIMUM);
        policy.addPolicy(new DummyDiscountPolicy(10.0));
        policy.addPolicy(new DummyDiscountPolicy(5.0));

        double discount = policy.calculateDiscount(new HashMap<>());
        assertEquals(10.0, discount);
    }

    @Test
    void compositeDiscountPolicyAddSamePolicyTwiceFails() {
        CompositeDiscountPolicy policy = new CompositeDiscountPolicy(DiscountCombinationType.SUM);
        DummyDiscountPolicy d = new DummyDiscountPolicy(10.0);
        policy.addPolicy(d);
        assertThrows(IllegalArgumentException.class, () -> policy.addPolicy(d));
    }

    @Test
    void compositeDiscountPolicyRemoveNonExistentPolicyFails() {
        CompositeDiscountPolicy policy = new CompositeDiscountPolicy(DiscountCombinationType.SUM);
        DummyDiscountPolicy d = new DummyDiscountPolicy(10.0);
        assertThrows(IllegalArgumentException.class, () -> policy.removePolicy(d));
    }

    // ---------------------------------------
    // CouponDiscountPolicy tests
    // ---------------------------------------
    @Test
    void couponDiscountPolicyWorksWhenCouponSubmitted() {
        CouponDiscountPolicy policy = new CouponDiscountPolicy("COUPON123", 20.0);
        policy.submitCoupon("COUPON123");

        assertEquals(20.0, policy.calculateDiscount(new HashMap<>()));
    }

    @Test
    void couponDiscountPolicyNoDiscountWithoutCoupon() {
        CouponDiscountPolicy policy = new CouponDiscountPolicy("COUPON123", 20.0);
        assertEquals(0.0, policy.calculateDiscount(new HashMap<>()));
    }

    // ---------------------------------------
    // DefaultDiscountPolicy tests
    // ---------------------------------------
    @Test
    void defaultDiscountPolicyAlwaysZero() {
        DefaultDiscountPolicy policy = new DefaultDiscountPolicy();
        assertEquals(0.0, policy.calculateDiscount(new HashMap<>()));
    }

    // ---------------------------------------
    // DefaultPurchasePolicy tests
    // ---------------------------------------
    @Test
    void defaultPurchasePolicyAlwaysTrue() {
        DefaultPurchasePolicy policy = new DefaultPurchasePolicy();
        assertTrue(policy.isPurchaseAllowed(new HashMap<>()));
    }

    // ---------------------------------------
    // MaxItemsPurchasePolicy tests
    // ---------------------------------------
    @Test
    void maxItemsPolicyPassesWhenUnderLimit() {
        MaxItemsPurchasePolicy policy = new MaxItemsPurchasePolicy(5);
        Map<String, Integer> cart = Map.of("item1", 3);
        assertTrue(policy.isPurchaseAllowed(cart));
    }

    @Test
    void maxItemsPolicyFailsWhenOverLimit() {
        MaxItemsPurchasePolicy policy = new MaxItemsPurchasePolicy(2);
        Map<String, Integer> cart = Map.of("item1", 3);
        assertFalse(policy.isPurchaseAllowed(cart));
    }

    // ---------------------------------------
    // MinItemsPurchasePolicy tests
    // ---------------------------------------
    @Test
    void minItemsPolicyFailsWhenUnderLimit() {
        MinItemsPurchasePolicy policy = new MinItemsPurchasePolicy(3);
        Map<String, Integer> cart = Map.of("item1", 2);
        assertFalse(policy.isPurchaseAllowed(cart));
    }

    @Test
    void minItemsPolicyPassesWhenAtLimit() {
        MinItemsPurchasePolicy policy = new MinItemsPurchasePolicy(2);
        Map<String, Integer> cart = Map.of("item1", 2);
        assertTrue(policy.isPurchaseAllowed(cart));
    }

    // ---------------------------------------
    // MinPricePurchasePolicy tests
    // ---------------------------------------
    @Test
    void minPricePolicyPassesWhenEnoughPrice() {
        MinPricePurchasePolicy policy = new MinPricePurchasePolicy(dummyStore, 150);
        Map<String, Integer> cart = Map.of("prod1", 2); // 100 * 2 = 200

        assertTrue(policy.isPurchaseAllowed(cart));
    }

    @Test
    void minPricePolicyFailsWhenUnderPrice() {
        MinPricePurchasePolicy policy = new MinPricePurchasePolicy(dummyStore, 500);
        Map<String, Integer> cart = Map.of("prod1", 2); // 100 * 2 = 200

        assertFalse(policy.isPurchaseAllowed(cart));
    }

    // ---------------------------------------
    // PercentageDiscountPolicy tests
    // ---------------------------------------
    @Test
    void percentageDiscountPolicyCorrectCalculation() {
        PercentageDiscountPolicy policy = new PercentageDiscountPolicy(dummyStore, 10); // 10% off
        Map<String, Integer> cart = Map.of("prod1", 2); // 2 * 100 = 200

        assertEquals(20.0, policy.calculateDiscount(cart), 0.01);
    }

    // ---------------------------------------
    // ProductPercentageDiscountPolicy tests
    // ---------------------------------------
    @Test
    void productPercentageDiscountPolicyWorks() {
        Map<String, Double> discounts = Map.of("prod1", 10.0);
        ProductPrecentageDiscountPolicy policy = new ProductPrecentageDiscountPolicy(dummyStore, discounts);

        Map<String, Integer> cart = Map.of("prod1", 2); // 2 * 100 = 200
        assertEquals(20.0, policy.calculateDiscount(cart), 0.01);
    }

    // ---------------------------------------
    // PolicyHandler tests
    // ---------------------------------------
    @Test
    void policyHandlerAllowsDefaultPurchase() {
        PolicyHandler handler = new PolicyHandler();
        assertTrue(handler.isPurchaseAllowed(new HashMap<>()));
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

    @Test
    void policyHandlerDiscountCalculationSum() {
        PolicyHandler handler = new PolicyHandler();
        handler.addDiscountPolicy(new DummyDiscountPolicy(10.0));
        handler.addDiscountPolicy(new DummyDiscountPolicy(20.0));

        assertEquals(30.0, handler.calculateDiscount(new HashMap<>()));
    }

    // ====== Dummy helpers ======

    static class DummyDiscountPolicy implements DiscountPolicy {
        private final double discount;

        public DummyDiscountPolicy(double discount) {
            this.discount = discount;
        }

        @Override
        public double calculateDiscount(Map<String, Integer> listings) {
            return discount;
        }
    }

    static class DummyStoreProductsManager implements IStoreProductsManager {

        private final Map<String, Listing> dummyListings = new HashMap<>();

        public void addDummyListing(String productId, double price) {
            Listing listing = new Listing("store1", productId, "Dummy Product", "A dummy product", 10, market.domain.purchase.PurchaseType.REGULAR, (int) price);
            dummyListings.put(productId, listing);
        }

        @Override
        public Listing getListingById(String listingId) {
            return dummyListings.get(listingId);
        }

        @Override public boolean addListing(Listing listing) { return false; }
        @Override public boolean removeListing(String listingId) { return false; }
        @Override public List<Listing> getListingsByProductName(String productName) { return null; }
        @Override public List<Listing> getListingsByProductId(String productId) { return null; }
        @Override public List<Listing> getAllListings() { return null; }
        @Override public boolean purchaseFromListing(String listingId, int quantity) { return false; }
    }
}
