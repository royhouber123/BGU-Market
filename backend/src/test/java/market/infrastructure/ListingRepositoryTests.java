package market.infrastructure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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



    //councurrency tests!!!!!!!!!!!!!!!!!!!!!!
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
    void testConcurrentPurchases_5Threads_Only2Succeed() throws InterruptedException {
        int numThreads = 5;
        int quantityPerThread = 1;
        CountDownLatch latch = new CountDownLatch(numThreads);
        AtomicInteger successCount = new AtomicInteger(0);

        Listing l = new Listing("storeX", "prodY", "LimitedItem", "category", "desc", 2, PurchaseType.REGULAR, 100);
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
                    // expected
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();
        assertEquals(2, successCount.get(), "Only 2 threads should succeed");
        assertEquals(0, l.getQuantityAvailable(), "All stock should be gone");
    }


    @Test
    void testConcurrentPurchases_10Threads_Requesting3_Only5Succeed() throws InterruptedException {
        int numThreads = 10;
        int quantityPerThread = 3;
        CountDownLatch latch = new CountDownLatch(numThreads);
        AtomicInteger successCount = new AtomicInteger(0);

        Listing l = new Listing("storeZ", "prodZ", "BundleItem", "category", "desc", 15, PurchaseType.REGULAR, 150);
        repository.addListing(l);

        for (int i = 0; i < numThreads; i++) {
            new Thread(() -> {
                Map<String, Map<String, Integer>> map = new HashMap<>();
                Map<String, Integer> bag = new HashMap<>();
                bag.put(l.getListingId(), quantityPerThread);
                map.put("storeZ", bag);

                try {
                    if (repository.updateStockForPurchasedItems(map)) {
                        successCount.getAndIncrement();
                    }
                } catch (RuntimeException e) {
                    // expected if over capacity
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();
        assertEquals(5, successCount.get(), "Only 5 threads should be able to purchase 3 units each");
        assertEquals(0, l.getQuantityAvailable(), "All inventory should be consumed");
    }

    @Test
    void testDoublePurchaseFromSameThread() {
        Listing l = new Listing("storeW", "prodW", "RepeatProduct", "category", "desc", 5, PurchaseType.REGULAR, 70);
        repository.addListing(l);

        Map<String, Map<String, Integer>> purchase1 = new HashMap<>();
        Map<String, Integer> bag1 = new HashMap<>();
        bag1.put(l.getListingId(), 3);
        purchase1.put("storeW", bag1);

        Map<String, Map<String, Integer>> purchase2 = new HashMap<>();
        Map<String, Integer> bag2 = new HashMap<>();
        bag2.put(l.getListingId(), 3);
        purchase2.put("storeW", bag2);

        boolean first = repository.updateStockForPurchasedItems(purchase1);
        boolean second = false;
        try {
            second = repository.updateStockForPurchasedItems(purchase2);
        } catch (RuntimeException e) {
            // expected: not enough stock
        }

        assertTrue(first);
        assertFalse(second);
        assertEquals(2, l.getQuantityAvailable());
    }



    @Test
    void testConcurrentPurchases_MixedItems_SomeSharedSomeExclusive() throws InterruptedException {
        int numThreads = 5;
        CountDownLatch latch = new CountDownLatch(numThreads);
        AtomicInteger successCount = new AtomicInteger(0);

        // Shared item: only 2 in stock
        Listing shared = new Listing("storeM", "prodS", "SharedItem", "cat", "desc", 2, PurchaseType.REGULAR, 99);
        // Exclusive item: enough stock for all
        Listing exclusive = new Listing("storeM", "prodE", "ExclusiveItem", "cat", "desc", 10, PurchaseType.REGULAR, 49);
        repository.addListing(shared);
        repository.addListing(exclusive);

        for (int i = 0; i < numThreads; i++) {
            new Thread(() -> {
                Map<String, Map<String, Integer>> cart = new HashMap<>();
                Map<String, Integer> bag = new HashMap<>();
                bag.put(shared.getListingId(), 1);   // All compete for this
                bag.put(exclusive.getListingId(), 1); // All can get this
                cart.put("storeM", bag);

                try {
                    if (repository.updateStockForPurchasedItems(cart)) {
                        successCount.getAndIncrement();
                    }
                } catch (RuntimeException e) {
                    // expected when shared item runs out
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();
        assertEquals(0, shared.getQuantityAvailable(), "Shared stock should go to 0");
        assertEquals(10 - successCount.get(), exclusive.getQuantityAvailable(), "Exclusive stock should match success count");
        assertEquals(2, successCount.get(), "Only 2 threads should succeed due to shared item");
    }


    @Test
    void testConcurrentPurchases_RaceConditionStress() throws InterruptedException {
        int numThreads = 50;
        int stock = 25;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numThreads);
        AtomicInteger successCount = new AtomicInteger();

        Listing l = new Listing("storeRace", "prodRace", "FastItem", "cat", "desc", stock, PurchaseType.REGULAR, 10);
        repository.addListing(l);

        for (int i = 0; i < numThreads; i++) {
            new Thread(() -> {
                try {
                    startLatch.await(); // all threads launch at once
                    Map<String, Map<String, Integer>> cart = new HashMap<>();
                    Map<String, Integer> bag = new HashMap<>();
                    bag.put(l.getListingId(), 1);
                    cart.put("storeRace", bag);

                    if (repository.updateStockForPurchasedItems(cart)) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception ignored) {
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown(); // boom, start them all
        endLatch.await();

        assertEquals(stock, successCount.get());
        assertEquals(0, l.getQuantityAvailable());
    }











    //councurrency tests!!!!!!!!!!!!!!!!!!!!!!

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