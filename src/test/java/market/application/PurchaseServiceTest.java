package market.application;

import market.domain.policy.*;
import market.domain.purchase.*;
import market.infrastructure.InMemoryPurchaseRepository;
import market.model.*;
import market.services.Store;
import market.services.StoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PurchaseServiceTest {

    private PurchaseService purchaseService;
    private InMemoryPurchaseRepository repository;
    private MockStoreService storeService;

    @BeforeEach
    public void setup() {
        repository = new InMemoryPurchaseRepository();
        storeService = new MockStoreService();
        purchaseService = new PurchaseService(storeService, repository);
    }

    @Test
    public void testRegularPurchaseSuccess() {
        String userId = "user123";
        String storeId = "store123";

        // Cart setup
        Product product = new Product("p1", "Beer", 10.0);
        CartItem item = new CartItem("p1", 2, 10.0, 0.0); // no discount
        StoreBag bag = new StoreBag(storeId, Map.of("p1", item));
        ShoppingCart cart = new ShoppingCart(List.of(bag));

        // Act
        Purchase purchase = purchaseService.executePurchase(
                userId,
                cart,
                "123 Test St",
                "0521234567",
                PurchaseType.REGULAR
        );

        // Assert
        assertNotNull(purchase);
        assertEquals(userId, purchase.getUserId());
        assertEquals(1, purchase.getProducts().size());
        assertEquals(20.0, purchase.getTotalPrice(), 0.01);

        List<Purchase> saved = repository.getPurchasesByUser(userId);
        assertEquals(1, saved.size());
    }

    @Test
    public void testPurchaseBlockedByPolicy() {
        String userId = "user123";
        String storeId = "store123";

        // Cart setup
        Product product = new Product("p1", "Wine", 50.0);
        CartItem item = new CartItem("p1", 1, 50.0, 0.0);
        StoreBag bag = new StoreBag(storeId, Map.of("p1", item));
        ShoppingCart cart = new ShoppingCart(List.of(bag));

        // Override mock store with policy that always blocks
        StoreService blockingStoreService = new StoreService() {
            @Override
            public Store getStoreById(String storeId) {
                return new Store() {
                    @Override
                    public PurchasePolicy getPurchasePolicy() {
                        return (uid, b) -> {
                            throw new PurchaseNotAllowedException("Underage!");
                        };
                    }

                    @Override
                    public DiscountPolicy getDiscountPolicy() {
                        return b -> {}; // no discount
                    }
                };
            }
        };

        PurchaseService service = new PurchaseService(blockingStoreService, repository);

        // Act + Assert
        assertThrows(PurchaseNotAllowedException.class, () -> {
            service.executePurchase(
                    userId,
                    cart,
                    "123 Wine Street",
                    "0520000000",
                    PurchaseType.REGULAR
            );
        });
    }

    @Test
    public void testDiscountPolicyApplied() {
        String userId = "user999";
        String storeId = "storeDiscounted";

        // Cart setup
        Product product = new Product("pD1", "Fancy Item", 100.0);
        CartItem item = new CartItem("pD1", 1, 100.0, 0.0); // original price, no discount set yet
        StoreBag bag = new StoreBag(storeId, Map.of("pD1", item));
        ShoppingCart cart = new ShoppingCart(List.of(bag));

        // market.application.StoreService with a DiscountPolicy that applies 20% off
        StoreService discountStoreService = new StoreService() {
            @Override
            public Store getStoreById(String storeId) {
                return new Store() {
                    @Override
                    public PurchasePolicy getPurchasePolicy() {
                        return (uid, b) -> {}; // always allow
                    }

                    @Override
                    public DiscountPolicy getDiscountPolicy() {
                        return b -> {
                            for (CartItem i : b.getItems()) {
                                i.setUnitPrice(i.getUnitPrice() * 0.8); // apply 20% off
                            }
                        };
                    }
                };
            }
        };

        PurchaseService service = new PurchaseService(discountStoreService, repository);

        // Act
        Purchase purchase = service.executePurchase(
                userId,
                cart,
                "1 Discount Lane",
                "0544444444",
                PurchaseType.REGULAR
        );

        // Assert
        assertNotNull(purchase);
        assertEquals(1, purchase.getProducts().size());
        PurchasedProduct purchased = purchase.getProducts().get(0);
        assertEquals(80.0, purchased.getUnitPrice(), 0.01); // price after discount
        assertEquals(80.0, purchase.getTotalPrice(), 0.01);
    }


    // Mocks below

    private static class MockStoreService implements StoreService {
        @Override
        public Store getStoreById(String storeId) {
            return new MockStore();
        }
    }

    private static class MockStore implements Store {
        @Override
        public PurchasePolicy getPurchasePolicy() {
            return (userId, bag) -> {}; // always pass
        }

        @Override
        public DiscountPolicy getDiscountPolicy() {
            return bag -> {}; // no discount
        }
    }
}
