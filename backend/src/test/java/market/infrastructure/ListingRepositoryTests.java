package market.infrastructure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import market.domain.purchase.PurchaseType;
import market.domain.store.Listing;

class ListingRepositoryTests {

    private ListingRepository repository;
    private Listing listing1;
    private Listing listing2;
    private Listing listing3;

    @BeforeEach
    void setup() {
        repository = new ListingRepository();

        listing1 = new Listing("store1", "prod1", "Product1","category", "desc", 10, PurchaseType.REGULAR, 100);
        listing2 = new Listing("store1", "prod2", "Product2", "category", "desc", 5, PurchaseType.REGULAR, 200);
        listing3 = new Listing("store2", "prod1", "Product1", "category", "desc", 8, PurchaseType.REGULAR, 150);

        repository.addListing(listing1);
        repository.addListing(listing2);
        repository.addListing(listing3);
    }

    @Test
    void testGetListingById() {
        assertEquals(listing1, repository.getListingById(listing1.getListingId()));
    }

    @Test
    void testRemoveListing() {
        assertTrue(repository.removeListing(listing2.getListingId()));
        assertNull(repository.getListingById(listing2.getListingId()));
    }

    @Test
    void testGetListingsByProductIdAndStore() {
        List<Listing> result = repository.getListingsByProductIdAndStore("prod1", "store1");
        assertEquals(1, result.size());
        assertEquals(listing1, result.get(0));
    }

    @Test
    void testUpdateStockSuccess() {
        Map<String, Map<String, Integer>> shopping = new HashMap<>();
        Map<String, Integer> store1Bag = new HashMap<>();
        store1Bag.put(listing1.getListingId(), 3);
        store1Bag.put(listing2.getListingId(), 2);
        shopping.put("store1", store1Bag);

        assertTrue(repository.updateStockForPurchasedItems(shopping));
        assertEquals(7, listing1.getQuantityAvailable());
        assertEquals(3, listing2.getQuantityAvailable());
    }

    @Test
    void testUpdateStockFailure_NotEnough() {
        Map<String, Map<String, Integer>> shopping = new HashMap<>();
        Map<String, Integer> store1Bag = new HashMap<>();
        store1Bag.put(listing1.getListingId(), 100); // exceeds available
        shopping.put("store1", store1Bag);

        Exception e = assertThrows(RuntimeException.class, () -> {
            repository.updateStockForPurchasedItems(shopping);
        });
        assertTrue(e.getMessage().contains("Not enough stock"));
    }

    @Test
    void testConcurrentPurchases() throws InterruptedException {
        int numThreads = 20;
        int quantityPerThread = 1;
        CountDownLatch latch = new CountDownLatch(numThreads);
        AtomicInteger successCount = new AtomicInteger(0);

        Listing l = new Listing("storeX", "prodX", "ConcurrentProduct", "category", "desc", 20, PurchaseType.REGULAR, 50);
        repository.addListing(l);

        for (int i = 0; i < numThreads; i++) {
            new Thread(() -> {
                Map<String, Map<String, Integer>> map = new HashMap<>();
                Map<String, Integer> bag = new HashMap<>();
                bag.put(l.getListingId(), quantityPerThread);
                map.put("storeX", bag);

                try {
                    if (repository.updateStockForPurchasedItems(map)) {
                        successCount.getAndIncrement();
                    }
                } catch (RuntimeException e) {
                    // expected if not enough stock
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();
        assertEquals(20, successCount.get());
        assertEquals(0, l.getQuantityAvailable());
    }


    @Test
    void testDisableEnableListingsByStoreId() {
        // Disable all listings in store1
        repository.disableListingsByStoreId("store1");

        assertTrue(!listing1.isActive());
        assertTrue(!listing2.isActive());
        assertTrue(listing3.isActive()); // from store2, should remain active

        // Enable all listings in store1
        repository.enableListingsByStoreId("store1");

        assertTrue(listing1.isActive());
        assertTrue(listing2.isActive());
        assertTrue(listing3.isActive()); // still active
    }

    @Test
    void testCalculateStoreBagWithoutDiscountSuccess() throws Exception {
        Map<String, Integer> items = new HashMap<>();
        items.put(listing1.getListingId(), 2); // 2 * 100 = 200
        items.put(listing2.getListingId(), 1); // 1 * 200 = 200

        double total = repository.calculateStoreBagWithoutDiscount(items);
        assertEquals(400.0, total);
    }

    @Test
    void testCalculateStoreBagWithoutDiscountFailsOnInactive() {
        listing1.disable(); // simulate store closed

        Map<String, Integer> items = new HashMap<>();
        items.put(listing1.getListingId(), 2); // inactive
        items.put(listing2.getListingId(), 1);

        Exception e = assertThrows(Exception.class, () ->
            repository.calculateStoreBagWithoutDiscount(items)
        );
        assertTrue(e.getMessage().contains("not found or inactive"));
    }

    @Test
    void testProductPriceSuccess() throws Exception {
        double price = repository.ProductPrice(listing1.getListingId());
        assertEquals(100.0, price);
    }

    @Test
    void testProductPriceFailsOnInactive() {
        listing2.disable();
        Exception e = assertThrows(Exception.class, () ->
            repository.ProductPrice(listing2.getListingId())
        );
        assertTrue(e.getMessage().contains("not found or inactive"));
    }


}