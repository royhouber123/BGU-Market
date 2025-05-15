package market.domain;


import market.domain.store.Listing;
import market.domain.purchase.PurchaseType;
import market.domain.store.Policies.DiscountPolicy;
import market.domain.store.Policies.Discounts.*;

import market.domain.store.IListingRepository;
import market.domain.store.IStoreProductsManager;
import market.infrastructure.ListingRepository;
import market.domain.store.StoreProductManager;
import market.dto.AddDiscountDTO;
import market.dto.DiscountConditionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DiscountPolicyFactoryTests {

    private IStoreProductsManager productManager;
    private Listing milk;
    private Listing rolls;

    @BeforeEach
    void setup() {
        IListingRepository repo = new ListingRepository();
        String storeId = "store-1";
        StoreProductManager manager = new StoreProductManager(storeId, repo);

        milk = new Listing(storeId, "milk-id", "Milk", "Dairy", "Fresh milk", 10, PurchaseType.REGULAR, 15.0);
        manager.addListing(milk);

        rolls = new Listing(storeId, "rolls-id", "Rolls", "Bakery", "Fresh rolls", 15, PurchaseType.REGULAR, 5.0);
        manager.addListing(rolls);

        productManager = manager;
    }

    @Test
    void testFromDTOPercentageDiscount() {
        AddDiscountDTO dto = new AddDiscountDTO(
            "PERCENTAGE",
            "PRODUCT",
            "milk-id",
            10.0,
            null,
            null,
            null,
            null
        );

        DiscountPolicy policy = DiscountPolicyFactory.fromDTO(dto);
        double discount = policy.calculateDiscount(Map.of(milk.getListingId(), 2), productManager);

        assertEquals(15.0 * 2 * 0.10, discount);
    }

    @Test
    void testFromDTOCouponDiscount() {
        AddDiscountDTO dto = new AddDiscountDTO(
            "COUPON",
            null,
            null,
            20.0,
            "CODE123",
            null,
            null,
            null
        );

        CouponDiscountPolicy policy = (CouponDiscountPolicy) DiscountPolicyFactory.fromDTO(dto);
        policy.submitCoupon("CODE123");
        double discount = policy.calculateDiscount(Map.of(milk.getListingId(), 1), productManager);

        assertEquals(20.0, discount);
    }

    @Test
    void testFromDTOConditionalDiscount() {
        DiscountConditionDTO condDto = new DiscountConditionDTO(
            "PRODUCT_QUANTITY_AT_LEAST",
            Map.of("productId", milk.getListingId(), "minQuantity", 2),
            null,
            null
        );

        AddDiscountDTO dto = new AddDiscountDTO(
            "CONDITIONAL",
            "PRODUCT",
            milk.getProductId(),
            10.0,
            null,
            condDto,
            null,
            null
        );

        DiscountPolicy policy = DiscountPolicyFactory.fromDTO(dto);

        double discount1 = policy.calculateDiscount(Map.of(milk.getListingId(), 1), productManager);
        double discount2 = policy.calculateDiscount(Map.of(milk.getListingId(), 2), productManager);

        assertEquals(0.0, discount1);
        assertEquals(15.0 * 2 * 0.10, discount2);
    }

        @Test
    void testCompositeDiscountPolicy() {
        // Setup two simple discounts
        DiscountPolicy dairyDiscount = new PercentageTargetedDiscount(DiscountTargetType.CATEGORY, "Dairy", 10); // 10%
        DiscountPolicy breadDiscount = new PercentageTargetedDiscount(DiscountTargetType.CATEGORY, "Bakery", 20); // 20%

        // Compose with SUM
        CompositeDiscountPolicy sumComposite = new CompositeDiscountPolicy(DiscountCombinationType.SUM);
        sumComposite.addPolicy(dairyDiscount);
        sumComposite.addPolicy(breadDiscount);

        // Compose with MAXIMUM
        CompositeDiscountPolicy maxComposite = new CompositeDiscountPolicy(DiscountCombinationType.MAXIMUM);
        maxComposite.addPolicy(dairyDiscount);
        maxComposite.addPolicy(breadDiscount);

        // Prepare listings map: 1 Milk (Dairy), 2 Bread (Bakery)
        Map<String, Integer> listings = Map.of(
            milk.getListingId(), 1,
            rolls.getListingId(), 2
        );

        // Calculate expected discounts
        double expectedDairy = milk.getPrice() * 1 * 0.10;
        double expectedBakery = rolls.getPrice() * 2 * 0.20;

        // Assert SUM combines both
        double sumResult = sumComposite.calculateDiscount(listings, productManager);
        assertEquals(expectedDairy + expectedBakery, sumResult, 0.001);

        // Assert MAXIMUM picks larger discount
        double maxResult = maxComposite.calculateDiscount(listings, productManager);
        assertEquals(Math.max(expectedDairy, expectedBakery), maxResult, 0.001);
    }
}
