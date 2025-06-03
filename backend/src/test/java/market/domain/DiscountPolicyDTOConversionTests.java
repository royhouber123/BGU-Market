package market.domain;

import market.domain.purchase.PurchaseType;
import market.domain.store.*;
import market.domain.store.Policies.*;
import market.domain.store.Policies.Discounts.*;
import market.domain.store.Policies.Discounts.Conditions.*;
import market.dto.PolicyDTO;
import market.infrastructure.ListingRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DiscountPolicyDTOConversionTests {

    //private IStoreProductsManager productManager;
    private Listing milk, bread, wine;

    @BeforeEach
    void setup() {
        IListingRepository repo = new ListingRepository();
        String storeId = "store-1";
        StoreProductManager manager = new StoreProductManager(storeId, repo);

        milk = new Listing(storeId, "milk-id", "Milk", "Dairy", "Fresh milk", 10, PurchaseType.REGULAR, 15.0);
        bread = new Listing(storeId, "bread-id", "Bread", "Bakery", "Fresh bread", 20, PurchaseType.REGULAR, 10.0);
        wine = new Listing(storeId, "wine-id", "Wine", "Alcohol", "Red wine", 10, PurchaseType.REGULAR, 50.0);

        manager.addListing(milk);
        manager.addListing(bread);
        manager.addListing(wine);

        //productManager = manager;
    }

    @Test
    void testPercentageTargetedDiscountDTO() {
        PercentageTargetedDiscount discount = new PercentageTargetedDiscount(DiscountTargetType.PRODUCT, milk.getProductId(), 10);
        PolicyDTO.AddDiscountRequest dto = discount.toDTO();

        assertEquals("PERCENTAGE", dto.type());
        assertEquals("PRODUCT", dto.scope());
        assertEquals(milk.getProductId(), dto.scopeId());
        assertEquals(10, dto.value());
    }

    @Test
    void testCouponDiscountPolicyDTO() {
        CouponDiscountPolicy coupon = new CouponDiscountPolicy("CODE123", 25);
        coupon.submitCoupon("CODE123");
        PolicyDTO.AddDiscountRequest dto = coupon.toDTO();

        assertEquals("COUPON", dto.type());
        assertEquals("CODE123", dto.couponCode());
        assertEquals(25, dto.value());
    }

    @Test
    void testDefaultDiscountPolicyDTO() {
        DefaultDiscountPolicy def = new DefaultDiscountPolicy();
        PolicyDTO.AddDiscountRequest dto = def.toDTO();

        assertEquals("DEFAULT", dto.type());
        assertEquals(0.0, dto.value());
    }

    @Test
    void testProductQuantityConditionDTO() {
        DiscountCondition cond = new ProductQuantityCondition(milk.getProductId(), 3);
        PolicyDTO.DiscountCondition dto = cond.toDTO();

        assertEquals("PRODUCT_QUANTITY_AT_LEAST", dto.type());
        assertEquals(milk.getProductId(), dto.params().get("productId"));
    }

    @Test
    void testBasketTotalConditionDTO() {
        DiscountCondition cond = new BasketTotalCondition(100);
        PolicyDTO.DiscountCondition dto = cond.toDTO();

        assertEquals("BASKET_TOTAL_AT_LEAST", dto.type());
        assertEquals(100.0, dto.params().get("minTotal"));
    }

    @Test
    void testCategoryQuantityConditionDTO() {
        DiscountCondition cond = new CategoryQuantityCondition("Bakery", 2);
        PolicyDTO.DiscountCondition dto = cond.toDTO();

        assertEquals("CATEGORY_QUANTITY_AT_LEAST", dto.type());
        assertEquals("Bakery", dto.params().get("category"));
    }

    @Test
    void testCompositeConditionDTO() {
        DiscountCondition cond1 = new ProductQuantityCondition(milk.getProductId(), 2);
        DiscountCondition cond2 = new BasketTotalCondition(50);
        CompositeCondition comp = new CompositeCondition(List.of(cond1, cond2), LogicOperator.AND);
        PolicyDTO.DiscountCondition dto = comp.toDTO();

        assertEquals("COMPOSITE", dto.type());
        assertEquals("AND", dto.logic());
        assertEquals(2, dto.subConditions().size());
    }

    @Test
    void testConditionalDiscountDTO() {
        DiscountCondition cond = new ProductQuantityCondition(milk.getProductId(), 2);
        DiscountPolicy discount = new PercentageTargetedDiscount(DiscountTargetType.CATEGORY, "Dairy", 10);
        ConditionalDiscountPolicy condPolicy = new ConditionalDiscountPolicy(cond, discount);

        PolicyDTO.AddDiscountRequest dto = condPolicy.toDTO();

        assertEquals("CONDITIONAL", dto.type());
        assertEquals("CATEGORY", dto.scope());
        assertEquals("Dairy", dto.scopeId());
        assertNotNull(dto.condition());
    }

    @Test
    void testCompositeDiscountPolicyDTO() {
        DiscountPolicy d1 = new PercentageTargetedDiscount(DiscountTargetType.CATEGORY, "Dairy", 10);
        DiscountPolicy d2 = new CouponDiscountPolicy("SAVE10", 10);
        CompositeDiscountPolicy composite = new CompositeDiscountPolicy(DiscountCombinationType.SUM);
        composite.addPolicy(d1);
        composite.addPolicy(d2);

        PolicyDTO.AddDiscountRequest dto = composite.toDTO();

        assertEquals("COMPOSITE", dto.type());
        assertEquals("SUM", dto.combinationType());
        assertEquals(2, dto.subDiscounts().size());
    }
}