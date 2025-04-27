package market.domain;

import market.domain.store.Policies.DefaultPurchasePolicy;
import market.domain.store.Store;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PoliciesTest {

    private Store store;

    @BeforeEach
    void setup() {
        store = new Store("1","store1", 1);
        store.addNewListing("p1", 100); // price = 100
        store.addNewListing("p2", 200); // price = 200
        store.addNewListing("p3", 300); // price = 300
    }

    // ----------------------
    // Purchase Policies Tests
    // ----------------------

    @Test
    void testDefaultPurchasePolicyAlwaysAllows() {
        DefaultPurchasePolicy policy = new DefaultPurchasePolicy();
        Map<String, Integer> listings = Map.of("p1", 1);

        assertTrue(policy.isPurchaseAllowed(store, listings));
    }

    @Test
    void testMinItemsPurchasePolicy() {
        MinItemsPurchasePolicy policy = new MinItemsPurchasePolicy(2);

        Map<String, Integer> listingsEnough = Map.of("p1", 2);
        Map<String, Integer> listingsNotEnough = Map.of("p1", 1);

        assertTrue(policy.isPurchaseAllowed(store, listingsEnough));
        assertFalse(policy.isPurchaseAllowed(store, listingsNotEnough));
    }

    @Test
    void testMaxItemsPurchasePolicy() {
        MaxItemsPurchasePolicy policy = new MaxItemsPurchasePolicy(2);

        Map<String, Integer> listingsWithin = Map.of("p1", 1);
        Map<String, Integer> listingsExceed = Map.of("p1", 2, "p2", 1);

        assertTrue(policy.isPurchaseAllowed(store, listingsWithin));
        assertFalse(policy.isPurchaseAllowed(store, listingsExceed));
    }

    @Test
    void testMinPricePurchasePolicy() {
        MinPricePurchasePolicy policy = new MinPricePurchasePolicy(250);

        Map<String, Integer> listingsEnough = Map.of("p2", 1, "p1", 1); // 200+100 = 300
        Map<String, Integer> listingsNotEnough = Map.of("p1", 2); // 100*2 = 200

        assertTrue(policy.isPurchaseAllowed(store, listingsEnough));
        assertFalse(policy.isPurchaseAllowed(store, listingsNotEnough));
    }

    // ----------------------
    // Discount Policies Tests
    // ----------------------

    @Test
    void testDefaultDiscountPolicyReturnsZero() {
        DefaultDiscountPolicy discount = new DefaultDiscountPolicy();
        Map<String, Integer> listings = Map.of("p1", 3);

        assertEquals(0.0, discount.calculateDiscount(listings));
    }

    @Test
    void testPercentageDiscountPolicy() {
        PercentageDiscountPolicy discount = new PercentageDiscountPolicy(store, 10);

        Map<String, Integer> listings = Map.of("p1", 1, "p2", 1); // 100 + 200 = 300

        assertEquals(30.0, discount.calculateDiscount(listings));
    }

    @Test
    void testProductPrecentageDiscountPolicy() {
        Map<String, Double> productDiscounts = Map.of(
                "p1", 0.10,
                "p2", 0.20
        );

        ProductPrecentageDiscountPolicy discount = new ProductPrecentageDiscountPolicy(store, productDiscounts);

        Map<String, Integer> listings = Map.of("p1", 2, "p2", 1);
        // p1: 100*2*10% = 20
        // p2: 200*1*20% = 40
        // Total = 60
        assertEquals(60.0, discount.calculateDiscount(listings));
    }

    @Test
    void testCompositeDiscountPolicySum() {
        DiscountPolicy d1 = (listings) -> 10.0;
        DiscountPolicy d2 = (listings) -> 20.0;

        CompositeDiscountPolicy composite = new CompositeDiscountPolicy(DiscountCombinationType.SUM);
        composite.addPolicy(d1);
        composite.addPolicy(d2);

        assertEquals(30.0, composite.calculateDiscount(new HashMap<>()));
    }

    @Test
    void testCompositeDiscountPolicyMaximum() {
        DiscountPolicy d1 = (listings) -> 10.0;
        DiscountPolicy d2 = (listings) -> 20.0;

        CompositeDiscountPolicy composite = new CompositeDiscountPolicy(DiscountCombinationType.MAXIMUM);
        composite.addPolicy(d1);
        composite.addPolicy(d2);

        assertEquals(20.0, composite.calculateDiscount(new HashMap<>()));
    }

    // ----------------------
    // PolicyHandler Tests
    // ----------------------

    @Test
    void testPolicyHandlerAllowsAndAppliesDiscount() {
        PolicyHandler handler = new PolicyHandler(store);
        handler.addPurchasePolicy(new MinItemsPurchasePolicy(1));
        handler.addDiscountPolicy(new PercentageDiscountPolicy(store, 10));

        Map<String, Integer> listings = Map.of("p1", 1);

        assertTrue(handler.isPurchaseAllowed(listings));
        assertEquals(10.0, handler.calculateDiscount(listings)); // 10% of 100
    }

    @Test
    void testPolicyHandlerBlocksPurchaseIfPolicyFails() {
        PolicyHandler handler = new PolicyHandler(store);
        handler.addPurchasePolicy(new MinItemsPurchasePolicy(5)); // Need at least 5 items

        Map<String, Integer> listings = Map.of("p1", 1);

        assertFalse(handler.isPurchaseAllowed(listings));
    }
}