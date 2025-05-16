package market.domain;

import market.domain.store.Listing;
import market.domain.store.Policies.Policies.*;
import market.domain.store.Policies.PurchasePolicy;
import market.domain.store.StoreProductManager;
import market.dto.AddPurchasePolicyDTO;
import market.infrastructure.ListingRepository;
import market.domain.purchase.PurchaseType;
import market.domain.store.IStoreProductsManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PurchasePolicySerializationTests {

    private IStoreProductsManager productManager;
    private Listing milk;
    private Listing bread;

    @BeforeEach
    void setup() {
        ListingRepository repo = new ListingRepository();
        productManager = new StoreProductManager("store1", repo);

        milk = new Listing("store1", "milk", "Milk", "Dairy", "1L milk", 100, PurchaseType.REGULAR, 10.0);
        bread = new Listing("store1", "bread", "Bread", "Bakery", "Loaf of bread", 100, PurchaseType.REGULAR, 5.0);

        productManager.addListing(milk);
        productManager.addListing(bread);
    }

    @Test
    void testMinItemsPolicySerialization() {
        PurchasePolicy original = new MinItemsPurchasePolicy(3);
        AddPurchasePolicyDTO dto = original.toDTO();

        assertEquals("MINITEMS", dto.type());
        assertEquals(3, dto.value());

        PurchasePolicy fromDto = PurchasePolicyFactory.fromDTO(dto);
        assertTrue(fromDto.isPurchaseAllowed(Map.of(milk.getListingId(), 2, bread.getListingId(), 1), productManager));
        assertFalse(fromDto.isPurchaseAllowed(Map.of(milk.getListingId(), 2),productManager));
    }

    @Test
    void testMaxItemsPolicySerialization() {
        PurchasePolicy original = new MaxItemsPurchasePolicy(3);
        AddPurchasePolicyDTO dto = original.toDTO();

        assertEquals("MAXITEMS", dto.type());
        assertEquals(3, dto.value());

        PurchasePolicy fromDto = PurchasePolicyFactory.fromDTO(dto);
        assertTrue(fromDto.isPurchaseAllowed(Map.of(milk.getListingId(), 1),productManager));
        assertFalse(fromDto.isPurchaseAllowed(Map.of(milk.getListingId(), 2, bread.getListingId(), 2),productManager));
    }

    @Test
    void testMinPricePolicySerialization() {
        PurchasePolicy original = new MinPricePurchasePolicy(20);
        AddPurchasePolicyDTO dto = original.toDTO();

        assertEquals("MINPRICE", dto.type());
        assertEquals(20, dto.value());

        PurchasePolicy fromDto = PurchasePolicyFactory.fromDTO(dto);
        assertTrue(fromDto.isPurchaseAllowed(Map.of(milk.getListingId(), 2, bread.getListingId(), 1),productManager)); // 25
        assertFalse(fromDto.isPurchaseAllowed(Map.of(bread.getListingId(), 3),productManager)); // 15
    }

    @Test
    void testRoundTripToDTOAndBack() {
        List<PurchasePolicy> policies = List.of(
            new MinItemsPurchasePolicy(2),
            new MaxItemsPurchasePolicy(4),
            new MinPricePurchasePolicy(30)
        );

        for (PurchasePolicy original : policies) {
            AddPurchasePolicyDTO dto = original.toDTO();
            PurchasePolicy recreated = PurchasePolicyFactory.fromDTO(dto);

            assertEquals(original.getClass(), recreated.getClass());
            assertNotNull(recreated.toDTO());
            assertEquals(dto.type(), recreated.toDTO().type());
            assertEquals(dto.value(), recreated.toDTO().value());
        }
    }
}