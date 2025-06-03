package market.domain;

import market.domain.purchase.PurchaseType;
import market.domain.store.*;
import market.domain.store.Policies.DiscountPolicy;
import market.domain.store.Policies.Discounts.*;
import market.domain.store.Policies.Discounts.Conditions.*;
import market.infrastructure.ListingRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class DiscountPolicyTests {

    private StoreProductManager productManager;
    private String cheeseId, yogurtId, breadId;

    @BeforeEach
    void setUp() {
        IListingRepository repo = new ListingRepository();
        productManager = new StoreProductManager("store1", repo);

        Listing cheese = new Listing("store1", "P1", "Cheese", "dairy", "desc", 100, PurchaseType.REGULAR, 10);
        Listing yogurt = new Listing("store1", "P2", "Yogurt", "dairy", "desc", 100, PurchaseType.REGULAR, 15);
        Listing bread = new Listing("store1", "P3", "Bread", "bakery", "desc", 100, PurchaseType.REGULAR, 8);

        cheeseId = repo.addListing(cheese);
        yogurtId = repo.addListing(yogurt);
        breadId = repo.addListing(bread);
    }


    @Test
    void testPercentageTargetedDiscount() {
        DiscountPolicy policy = DiscountPolicyFactory.createPercentageTargetedDiscount(
            DiscountTargetType.CATEGORY, "dairy", 10
        );

        Map<String, Integer> basket = Map.of(
            cheeseId, 2,
            yogurtId, 2
        );
        // Total = 10*2 + 15*2 = 50, 10% = 5
        assertEquals(5.0, policy.calculateDiscount(basket, productManager), 0.001);
    }

    @Test
    void testCouponDiscountPolicy() {
        CouponDiscountPolicy coupon = new CouponDiscountPolicy("SAVE10", 10.0);
        coupon.submitCoupon("SAVE10");

        Map<String, Integer> basket = Map.of("P1", 1);
        assertEquals(10.0, coupon.calculateDiscount(basket, productManager));
    }

    @Test
    void testConditionalDiscount_applies() {
        DiscountCondition condition = ConditionFactory.basketTotalAtLeast(30);
        DiscountPolicy discount = DiscountPolicyFactory.createPercentageTargetedDiscount(
            DiscountTargetType.STORE, null, 10
        );
        DiscountPolicy conditional = DiscountPolicyFactory.createConditionalDiscount(condition, discount);

        Map<String, Integer> basket = Map.of(
            cheeseId, 2,  // 10 * 2
            breadId, 2    // 8 * 2 → Total = 36 → should apply
        );

        assertEquals(3.6, conditional.calculateDiscount(basket, productManager), 0.001);
    }

    @Test
    void testConditionalDiscount_doesNotApply() {
        DiscountCondition condition = new BasketTotalCondition(100);
        DiscountPolicy base = new PercentageTargetedDiscount(DiscountTargetType.STORE, null, 10);
        DiscountPolicy conditional = new ConditionalDiscountPolicy(condition, base);

        Map<String, Integer> basket = Map.of("P3", 1); // bread = 8
        assertEquals(0.0, conditional.calculateDiscount(basket, productManager));
    }

    @Test
    void testCompositeDiscount_SUM() {
        DiscountPolicy d1 = DiscountPolicyFactory.createPercentageTargetedDiscount(
            DiscountTargetType.PRODUCT, "P1", 10
        );
        CouponDiscountPolicy d2 = DiscountPolicyFactory.createCouponDiscount("SUMME", 5.0);
        d2.submitCoupon("SUMME");

        DiscountPolicy combo = DiscountPolicyFactory.createCompositeDiscount(
            DiscountCombinationType.SUM,
            List.of(d1, d2)
        );

        Map<String, Integer> basket = Map.of(cheeseId, 1); // P1 is productId of cheese
        // d1: 10% of 10 = 1, d2 = 5 → total = 6
        assertEquals(6.0, combo.calculateDiscount(basket, productManager), 0.001);
    }

    @Test
    void testCompositeDiscount_MAXIMUM() {
        DiscountPolicy d1 = new PercentageTargetedDiscount(DiscountTargetType.PRODUCT, "P1", 10); // 1
        CouponDiscountPolicy d2 = new CouponDiscountPolicy("MAXXX", 7.0);
        d2.submitCoupon("MAXXX");

        CompositeDiscountPolicy combo = new CompositeDiscountPolicy(DiscountCombinationType.MAXIMUM);
        combo.addPolicy(d1);
        combo.addPolicy(d2);

        Map<String, Integer> basket = Map.of("P1", 1);
        assertEquals(7.0, combo.calculateDiscount(basket, productManager), 0.001);
    }

    @Test
    void testLogicalConditions_AND_OR_XOR() {
        Map<String, Integer> basket = Map.of("P1", 3, "P2", 2, "P3", 1); // cheese, yogurt, bread

        DiscountCondition cond1 = ConditionFactory.productQuantityAtLeast("P1", 3); // true
        DiscountCondition cond2 = ConditionFactory.productQuantityAtLeast("P2", 2); // true
        DiscountCondition cond3 = ConditionFactory.productQuantityAtLeast("P3", 2); // false

        assertTrue(ConditionFactory.and(List.of(cond1, cond2)).isSatisfied(basket, productManager));
        assertTrue(ConditionFactory.or(List.of(cond1, cond3)).isSatisfied(basket, productManager));
        assertFalse(ConditionFactory.xor(List.of(cond1, cond2)).isSatisfied(basket, productManager));
    }

    @Test
    void testCompositeCondition_AND_true() {
        DiscountCondition c1 = ConditionFactory.productQuantityAtLeast(cheeseId, 2);
        DiscountCondition c2 = ConditionFactory.productQuantityAtLeast(yogurtId, 1);

        DiscountCondition and = ConditionFactory.and(List.of(c1, c2));
        Map<String, Integer> basket = Map.of(cheeseId, 2, yogurtId, 1);

        assertTrue(and.isSatisfied(basket, productManager));
    }

    @Test
    void testCompositeCondition_AND_false() {
        DiscountCondition c1 = ConditionFactory.productQuantityAtLeast(cheeseId, 2);
        DiscountCondition c2 = ConditionFactory.productQuantityAtLeast(yogurtId, 3);

        DiscountCondition and = ConditionFactory.and(List.of(c1, c2));
        Map<String, Integer> basket = Map.of(cheeseId, 2, yogurtId, 1);

        assertFalse(and.isSatisfied(basket, productManager));
    }

    @Test
    void testCompositeCondition_OR_true() {
        DiscountCondition c1 = ConditionFactory.productQuantityAtLeast(cheeseId, 3);
        DiscountCondition c2 = ConditionFactory.productQuantityAtLeast(breadId, 2);

        DiscountCondition or = ConditionFactory.or(List.of(c1, c2));
        Map<String, Integer> basket = Map.of(cheeseId, 1, breadId, 2);

        assertTrue(or.isSatisfied(basket, productManager));
    }

    @Test
    void testCompositeCondition_XOR_true() {
        DiscountCondition c1 = ConditionFactory.containsProduct(cheeseId);
        DiscountCondition c2 = ConditionFactory.containsProduct(yogurtId);

        DiscountCondition xor = ConditionFactory.xor(List.of(c1, c2));
        Map<String, Integer> basket = Map.of(cheeseId, 1);

        assertTrue(xor.isSatisfied(basket, productManager));
    }

    @Test
    void testCompositeCondition_XOR_false_twoTrue() {
        DiscountCondition c1 = ConditionFactory.containsProduct(cheeseId);
        DiscountCondition c2 = ConditionFactory.containsProduct(yogurtId);

        DiscountCondition xor = ConditionFactory.xor(List.of(c1, c2));
        Map<String, Integer> basket = Map.of(cheeseId, 1, yogurtId, 1);

        assertFalse(xor.isSatisfied(basket, productManager));
    }

    @Test
    void testCompositeCondition_nested() {
        DiscountCondition cond1 = ConditionFactory.productQuantityAtLeast(cheeseId, 2);
        DiscountCondition cond2 = ConditionFactory.productQuantityAtLeast(yogurtId, 2);
        DiscountCondition cond3 = ConditionFactory.productQuantityAtLeast(breadId, 3);

        // (cheese ≥ 2 AND yogurt ≥ 2) OR bread ≥ 3
        DiscountCondition complex = ConditionFactory.or(List.of(
                ConditionFactory.and(List.of(cond1, cond2)),
                cond3
        ));

        Map<String, Integer> basket = Map.of(cheeseId, 2, yogurtId, 2);
        assertTrue(complex.isSatisfied(basket, productManager));
    }

    @Test
    void testEmptyBasketFailsAll() {
        DiscountCondition cond = ConditionFactory.basketTotalAtLeast(10);
        assertFalse(cond.isSatisfied(Collections.emptyMap(), productManager));
    }

    @Test
    void testInvalidProductIgnored() {
        DiscountCondition cond = ConditionFactory.productQuantityAtLeast("NOT_EXIST", 1);
        assertFalse(cond.isSatisfied(Map.of(cheeseId, 3), productManager));
    }

    @Test
    void testEdgeCase_productExactlyAtThreshold() {
        DiscountCondition cond = ConditionFactory.productQuantityAtLeast(cheeseId, 3);
        Map<String, Integer> basket = Map.of(cheeseId, 3);

        assertTrue(cond.isSatisfied(basket, productManager));
    }

    @Test
    void testBasketTotalCondition_exactThreshold() {
        DiscountCondition cond = ConditionFactory.basketTotalAtLeast(30);
        Map<String, Integer> basket = Map.of(
                cheeseId, 2, // 10*2
                breadId, 1   // 8 → 28
        );
        assertFalse(cond.isSatisfied(basket, productManager));

        basket = Map.of(
                cheeseId, 2, // 20
                breadId, 2   // 16 → 36
        );
        assertTrue(cond.isSatisfied(basket, productManager));
    }
}