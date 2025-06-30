package market.domain;

import market.domain.purchase.PurchaseType;
import market.domain.store.*;
import market.domain.store.Policies.DiscountPolicy;
import market.domain.store.Policies.Discounts.*;
import market.domain.store.Policies.Discounts.Conditions.*;
import market.infrastructure.ListingRepository;
import market.dto.PolicyDTO;

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

    @Test
    void testFixedDiscountPolicy_Product() {
        DiscountPolicy policy = new FixedDiscountPolicy("P1", 5.0, DiscountTargetType.PRODUCT);
        Map<String, Integer> basket = Map.of(cheeseId, 2); // 2 cheese at $10 each = $20

        // Should apply $5 discount per item = $10 total discount
        assertEquals(10.0, policy.calculateDiscount(basket, productManager), 0.001);
    }

    @Test
    void testFixedDiscountPolicy_Store() {
        DiscountPolicy policy = new FixedDiscountPolicy(null, 15.0, DiscountTargetType.STORE);
        Map<String, Integer> basket = Map.of(cheeseId, 1, yogurtId, 1); // $25 total

        // Should apply $15 discount once to entire order
        assertEquals(15.0, policy.calculateDiscount(basket, productManager), 0.001);
    }

    @Test
    void testFixedDiscountPolicy_CategoryTarget() {
        // Test fixed discount on category
        FixedDiscountPolicy policy = new FixedDiscountPolicy("dairy", 3.0, DiscountTargetType.CATEGORY);
        Map<String, Integer> basket = Map.of(cheeseId, 2, yogurtId, 1); // 3 dairy items

        // Should apply $3 discount per dairy item = $9 total
        double discount = policy.calculateDiscount(basket, productManager);
        assertEquals(9.0, discount, 0.001, "Category fixed discount should be $3 × 3 dairy items = $9");
    }

    @Test
    void testFixedDiscountPolicy_ExceedsProductPrice() {
        // Test when fixed discount exceeds item price
        FixedDiscountPolicy policy = new FixedDiscountPolicy("P1", 50.0, DiscountTargetType.PRODUCT);
        Map<String, Integer> basket = Map.of(cheeseId, 1); // $10 cheese

        // Should cap at item price: Math.min(50, 10) = 10
        double discount = policy.calculateDiscount(basket, productManager);
        assertEquals(10.0, discount, 0.001, "Discount should be capped at item price ($10)");
    }

    @Test
    void testFixedDiscountPolicy_ExceedsStoreTotal() {
        // Test when store discount exceeds total cart value
        FixedDiscountPolicy policy = new FixedDiscountPolicy(null, 100.0, DiscountTargetType.STORE);
        Map<String, Integer> basket = Map.of(cheeseId, 1); // $10 total

        // Should cap at cart total: Math.min(100, 10) = 10
        double discount = policy.calculateDiscount(basket, productManager);
        assertEquals(10.0, discount, 0.001, "Store discount should be capped at cart total ($10)");
    }

    @Test
    void testFixedDiscountPolicy_ZeroQuantity() {
        // Test with empty basket
        FixedDiscountPolicy policy = new FixedDiscountPolicy("P1", 5.0, DiscountTargetType.PRODUCT);
        Map<String, Integer> basket = Map.of();

        double discount = policy.calculateDiscount(basket, productManager);
        assertEquals(0.0, discount, 0.001, "No discount for empty basket");
    }

    @Test
    void testFixedDiscountPolicy_NoMatchingProduct() {
        // Test when target product is not in basket
        FixedDiscountPolicy policy = new FixedDiscountPolicy("P999", 5.0, DiscountTargetType.PRODUCT);
        Map<String, Integer> basket = Map.of(cheeseId, 2);

        double discount = policy.calculateDiscount(basket, productManager);
        assertEquals(0.0, discount, 0.001, "No discount when target product not in basket");
    }

    @Test
    void testFixedDiscountPolicy_MultipleProducts() {
        // Test fixed discount with multiple matching products
        FixedDiscountPolicy policy = new FixedDiscountPolicy("P1", 2.0, DiscountTargetType.PRODUCT);
        Map<String, Integer> basket = Map.of(
            cheeseId, 3,   // 3 × $2 = $6 discount
            yogurtId, 1    // Not P1, no discount
        );

        double discount = policy.calculateDiscount(basket, productManager);
        assertEquals(6.0, discount, 0.001, "Should apply $2 × 3 matching items = $6");
    }

    @Test
    void testFixedDiscountPolicy_NegativeAmount() {
        // Test that negative discount amounts are rejected
        assertThrows(IllegalArgumentException.class, () -> {
            new FixedDiscountPolicy("P1", -5.0, DiscountTargetType.PRODUCT);
        }, "Should throw exception for negative discount amount");
    }

    @Test
    void testFixedDiscountPolicy_DTO_Conversion() {
        // Test DTO conversion
        FixedDiscountPolicy policy = new FixedDiscountPolicy("product-123", 10.0, DiscountTargetType.PRODUCT);
        PolicyDTO.AddDiscountRequest dto = policy.toDTO();

        assertEquals("FIXED", dto.type());
        assertEquals("PRODUCT", dto.scope());
        assertEquals("product-123", dto.scopeId());
        assertEquals(10.0, dto.value());
        assertNull(dto.couponCode());
        assertNull(dto.condition());
    }

    @Test
    void testFixedDiscountPolicy_Equals() {
        // Test equals method
        FixedDiscountPolicy policy1 = new FixedDiscountPolicy("P1", 5.0, DiscountTargetType.PRODUCT);
        FixedDiscountPolicy policy2 = new FixedDiscountPolicy("P1", 5.0, DiscountTargetType.PRODUCT);
        FixedDiscountPolicy policy3 = new FixedDiscountPolicy("P2", 5.0, DiscountTargetType.PRODUCT);

        assertEquals(policy1, policy2, "Identical policies should be equal");
        assertNotEquals(policy1, policy3, "Different target policies should not be equal");
    }

    @Test
    void testFixedDiscountPolicy_HashCode() {
        // Test hashCode consistency
        FixedDiscountPolicy policy1 = new FixedDiscountPolicy("P1", 5.0, DiscountTargetType.PRODUCT);
        FixedDiscountPolicy policy2 = new FixedDiscountPolicy("P1", 5.0, DiscountTargetType.PRODUCT);

        assertEquals(policy1.hashCode(), policy2.hashCode(), "Equal policies should have same hash code");
    }
}