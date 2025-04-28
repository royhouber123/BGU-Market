package market.infrastructure;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import market.domain.store.Store;

class StoreRepositoryTests {

    private StoreRepository storeRepository;
    private Store store1;
    private Store store2;

    @BeforeEach
    void setup() throws Exception {
        storeRepository = new StoreRepository();
        store1 = new Store("1", "TestStore1", "founder1");
        store2 = new Store("2", "TestStore2", "founder2");
        storeRepository.addStore(store1);
        storeRepository.addStore(store2);
    }

    // ---------- Add and Get Tests ----------

    @Test
    void testAddAndRetrieveStoreByIdSuccess() {
        Store retrieved = storeRepository.getStoreByID("1");
        assertNotNull(retrieved);
        assertEquals("TestStore1", retrieved.getName());
    }

    @Test
    void testAddAndRetrieveStoreByNameSuccess() {
        Store retrieved = storeRepository.getStoreByName("TestStore2");
        assertNotNull(retrieved);
        assertEquals("2", retrieved.getStoreID());
    }

    @Test
    void testGetStoreByIdNotFound() {
        assertNull(storeRepository.getStoreByID("999"));
    }

    @Test
    void testGetStoreByNameNotFound() {
        assertNull(storeRepository.getStoreByName("NonExistingStore"));
    }

    // ---------- Duplicate Store Tests ----------

    @Test
    void testAddDuplicateStoreNameThrows() {
        Exception e = assertThrows(Exception.class, () -> {
            storeRepository.addStore(new Store("999", "TestStore1", "founderX"));
        });
        assertTrue(e.getMessage().contains("already exists"));
    }

    @Test
    void testAddDuplicateStoreIdThrows() {
        Exception e = assertThrows(Exception.class, () -> {
            storeRepository.addStore(new Store("1", "AnotherStore", "founderX"));
        });
        assertTrue(e.getMessage().contains("already exists"));
    }

    // ---------- Remove Store Tests ----------

    @Test
    void testRemoveStoreSuccess() throws Exception {
        storeRepository.removeStore("TestStore1");
        assertNull(storeRepository.getStoreByName("TestStore1"));
        assertNull(storeRepository.getStoreByID("1"));
    }

    @Test
    void testRemoveStoreNotFoundThrows() {
        Exception e = assertThrows(Exception.class, () -> {
            storeRepository.removeStore("FakeStore");
        });
        assertTrue(e.getMessage().contains("doesn't exist"));
    }

    // ---------- Contains Store Tests ----------

    @Test
    void testContainsStoreSuccess() {
        assertTrue(storeRepository.containsStore("TestStore1"));
        assertTrue(storeRepository.containsStore("TestStore2"));
    }

    @Test
    void testContainsStoreFailure() {
        assertFalse(storeRepository.containsStore("NonExistingStore"));
    }

    // ---------- Get Next Store ID Tests ----------

    @Test
    void testGetNextStoreId() {
        String nextId = storeRepository.getNextStoreID();
        assertEquals("3", nextId); // because we added store IDs "1" and "2"
    }

    @Test
    void testGetNextStoreIdEmptyRepository() {
        StoreRepository emptyRepo = new StoreRepository();
        assertEquals("1", emptyRepo.getNextStoreID());
    }

    // ---------- updateStockForPurchasedItems Tests ----------

    @Test
    void testUpdateStockForPurchasedItemsSuccess() throws Exception {
        // Prepare data: add a listing manually
        store1.addNewListing("founder1", "prod1", "Product1", "desc", 10, 100);
        store2.addNewListing("founder2", "prod2", "Product2", "desc", 5, 200);

        Map<String, Map<String, Integer>> shoppingBags = new HashMap<>();
        Map<String, Integer> store1Bag = new HashMap<>();
        Map<String, Integer> store2Bag = new HashMap<>();

        store1Bag.put(store1.getAllListings().get(0).getListingId(), 2);
        store2Bag.put(store2.getAllListings().get(0).getListingId(), 3);

        shoppingBags.put("1", store1Bag);
        shoppingBags.put("2", store2Bag);

        boolean result = storeRepository.updateStockForPurchasedItems(shoppingBags);

        assertTrue(result);
        assertEquals(8, store1.getAllListings().get(0).getQuantityAvailable());
        assertEquals(2, store2.getAllListings().get(0).getQuantityAvailable());
    }

    @Test
    void testUpdateStockForPurchasedItemsFailureInsufficientStock() throws Exception {
        store1.addNewListing("founder1", "prod1", "Product1", "desc", 1, 100);

        Map<String, Map<String, Integer>> shoppingBags = new HashMap<>();
        Map<String, Integer> store1Bag = new HashMap<>();
        store1Bag.put(store1.getAllListings().get(0).getListingId(), 5);

        shoppingBags.put("1", store1Bag);

        boolean result = storeRepository.updateStockForPurchasedItems(shoppingBags);

        assertFalse(result);
    }
}
