package market.domain;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import market.domain.store.Listing;
import market.domain.store.Store;

class StoreUnitTests {

    private Store store;
    private final String founderID = "1";
    private final String ownerA = "2";
    private final String ownerB = "3";
    private final String ownerC = "4";

    // ------------------------- Setup -------------------------
    @BeforeEach
    void setUp() {
        store = new Store("999", "TestStore", founderID);
    }

    // ------------------------- Owner Assignment Tests -------------------------

    @Test
    void testFounderIsOwner() {
        assertTrue(store.isOwner(founderID), "Founder should be an owner");
    }

    @Test
    void testAddNewOwnerSuccess() throws Exception {
        boolean result = store.addNewOwner(founderID, ownerA);
        assertTrue(result);
        assertTrue(store.isOwner(ownerA), "Newly added owner should be recognized as owner");
        assertEquals(founderID, store.OwnerAssignedBy(ownerA), "Appointer should be the founder");
    }

    @Test
    void testAddOwnerByNonOwnerShouldFail() {
        Exception exception = assertThrows(Exception.class, () -> {
            store.addNewOwner(ownerA, ownerB);
        });
        assertTrue(exception.getMessage().contains("is not a owner"), "Expected error for non-owner appointer");
    }

    @Test
    void testAddAlreadyOwnerShouldFail() throws Exception {
        store.addNewOwner(founderID, ownerA);
        Exception exception = assertThrows(Exception.class, () -> {
            store.addNewOwner(founderID, ownerA);
        });
        assertTrue(exception.getMessage().contains("already a owner"), "Expected error for already an owner");
    }

    // ------------------------- Owner Removal Tests -------------------------

    @Test
    void testRemoveOwnerRecursiveSuccess() throws Exception {
        // Build hierarchy: founder → A → B → C
        store.addNewOwner(founderID, ownerA);
        store.addNewOwner(ownerA, ownerB);
        store.addNewOwner(ownerB, ownerC);

        List<String> removed = store.removeOwner(founderID, ownerA);

        assertTrue(removed.contains(ownerA));
        assertTrue(removed.contains(ownerB));
        assertTrue(removed.contains(ownerC));

        assertFalse(store.isOwner(ownerA));
        assertFalse(store.isOwner(ownerB));
        assertFalse(store.isOwner(ownerC));
    }

    @Test
    void testRemoveOwnerWhenIdNotOwner() throws Exception {
        store.addNewOwner(founderID, ownerA);

        Exception ex = assertThrows(Exception.class, () -> {
            store.removeOwner(ownerA, founderID);
        });

        assertTrue(ex.getMessage().contains("is the FOUNDER"));
    }

    @Test
    void testRemoveOwnerWhenToRemoveNotOwner() {
        Exception ex = assertThrows(Exception.class, () -> {
            store.removeOwner(founderID, ownerA); // ownerA hasn't been added yet
        });

        assertTrue(ex.getMessage().contains("is not a owner"));
    }

    @Test
    void testCannotRemoveFounder() {
        Exception ex = assertThrows(Exception.class, () -> {
            store.removeOwner(founderID, founderID);
        });

        assertTrue(ex.getMessage().contains("is the FOUNDER"));
    }

    @Test
    void testRemoveOwnerNotAssignedByCaller() throws Exception {
        store.addNewOwner(founderID, ownerA);
        store.addNewOwner(ownerA, ownerB);

        Exception ex = assertThrows(Exception.class, () -> {
            store.removeOwner(founderID, ownerB); // founder didn't assign B
        });

        assertTrue(ex.getMessage().contains("didn't assign"));
    }

    // ------------------------- Manager Assignment Tests -------------------------

    @Test
    void testAddNewManagerSuccess() throws Exception {
        store.addNewOwner(founderID, ownerA);
        boolean result = store.addNewManager(ownerA, ownerB);
        assertTrue(result);
        assertTrue(store.isManager(ownerB), "User should now be a manager");
    }

    @Test
    void testAddManagerByNonOwnerShouldFail() {
        Exception ex = assertThrows(Exception.class, () -> {
            store.addNewManager(ownerA, ownerB); // ownerA is not yet an owner
        });
        assertTrue(ex.getMessage().contains("is not a owner"));
    }

    @Test
    void testAddAlreadyExistingManagerShouldFail() throws Exception {
        store.addNewOwner(founderID, ownerA);
        store.addNewManager(ownerA, ownerB);

        Exception ex = assertThrows(Exception.class, () -> {
            store.addNewManager(ownerA, ownerB); // ownerB is already a manager
        });
        assertTrue(ex.getMessage().contains("already a owner"));
    }

    // ------------------------- Manager Permission Tests -------------------------

    @Test
    void testAddPermissionToManagerSuccess() throws Exception {
        store.addNewOwner(founderID, ownerA);
        store.addNewManager(ownerA, ownerB);
        boolean granted = store.addPermissionToManager(ownerB, ownerA, Store.Permission.EDIT_PRODUCTS.getCode());
        assertTrue(granted);

        Set<Integer> permissions = store.getPositionsInStore(ownerA).get(ownerB);
        assertNotNull(permissions);
        assertTrue(permissions.contains(Store.Permission.EDIT_PRODUCTS.getCode()), "Manager should have EDIT_PRODUCTS permission");
    }

    @Test
    void testAddPermissionByNonAppointerShouldFail() throws Exception {
        store.addNewOwner(founderID, ownerA);
        store.addNewOwner(founderID, ownerC);
        store.addNewManager(ownerA, ownerB);

        Exception ex = assertThrows(Exception.class, () -> {
            store.addPermissionToManager(ownerB, ownerC, Store.Permission.EDIT_PRODUCTS.getCode());
        });

        assertTrue(ex.getMessage().contains("not his appointer"));
    }

    @Test
    void testManagerPermissionDeniedInitially() throws Exception {
        store.addNewOwner(founderID, ownerA);
        store.addNewManager(ownerA, ownerB);

        Set<Integer> permissions = store.getPositionsInStore(ownerA).get(ownerB);
        assertNotNull(permissions);
        assertFalse(permissions.contains(Store.Permission.EDIT_PRODUCTS.getCode()), "Manager should not have EDIT_PRODUCTS by default");
    }


    // ------------------------- Listing Management Tests -------------------------

    @Test
    void testAddNewListingByOwnerSuccess() throws Exception {
        // Setup
        store.addNewOwner(founderID, ownerA);

        // Act
        boolean added = store.addNewListing(ownerA, "p1", "Laptop", "Gaming laptop", 5, 2000);

        // Assert
        assertTrue(added);
        assertEquals(1, store.getListingsByProductName("Laptop").size());
    }

    @Test
    void testAddNewListingByManagerWithPermissionSuccess() throws Exception {
        store.addNewOwner(founderID, ownerA);
        store.addNewManager(ownerA, ownerB);
        store.addPermissionToManager(ownerB, ownerA, Store.Permission.EDIT_PRODUCTS.getCode());

        boolean added = store.addNewListing(ownerB, "p2", "Phone", "iPhone", 10, 1000);

        assertTrue(added);
        assertEquals(1, store.getListingsByProductName("Phone").size());
    }

    @Test
    void testAddNewListingByManagerWithoutPermissionFails() throws Exception {
        store.addNewOwner(founderID, ownerA);
        store.addNewManager(ownerA, ownerB);

        Exception ex = assertThrows(Exception.class, () -> {
            store.addNewListing(ownerB, "p3", "Tablet", "Samsung Tab", 7, 600);
        });

        assertTrue(ex.getMessage().contains("doesn't have permission"));
    }

    @Test
    void testRemoveListingByOwnerSuccess() throws Exception {
        store.addNewOwner(founderID, ownerA);
        store.addNewListing(ownerA, "p4", "TV", "Smart TV", 3, 1500);

        String listingId = store.getListingsByProductName("TV").get(0).getListingId();
        boolean removed = store.removeListing(ownerA, listingId);

        assertTrue(removed);
        assertTrue(store.getListingsByProductName("TV").isEmpty());
    }

    @Test
    void testRemoveListingByManagerWithPermissionSuccess() throws Exception {
        store.addNewOwner(founderID, ownerA);
        store.addNewManager(ownerA, ownerB);
        store.addPermissionToManager(ownerB, ownerA, Store.Permission.EDIT_PRODUCTS.getCode());

        store.addNewListing(ownerA, "p5", "Speaker", "Bluetooth speaker", 8, 300);
        String listingId = store.getListingsByProductName("Speaker").get(0).getListingId();

        boolean removed = store.removeListing(ownerB, listingId);

        assertTrue(removed);
        assertTrue(store.getListingsByProductName("Speaker").isEmpty());
    }

    @Test
    void testRemoveListingByManagerWithoutPermissionFails() throws Exception {
        store.addNewOwner(founderID, ownerA);
        store.addNewManager(ownerA, ownerB);

        store.addNewListing(ownerA, "p6", "Headphones", "Noise cancelling", 4, 250);
        String listingId = store.getListingsByProductName("Headphones").get(0).getListingId();

        Exception ex = assertThrows(Exception.class, () -> {
            store.removeListing(ownerB, listingId);
        });

        assertTrue(ex.getMessage().contains("doesn't have permission"));
    }

    @Test
    void testPurchaseListingReducesQuantity() throws Exception {
        store.addNewOwner(founderID, ownerA);
        store.addNewListing(ownerA, "p7", "Monitor", "4K Monitor", 6, 800);

        String listingId = store.getListingsByProductName("Monitor").get(0).getListingId();
        boolean purchased = store.purchaseFromListing(listingId, 2);

        assertTrue(purchased);
        assertEquals(4, store.getListing(listingId).getQuantityAvailable());
    }

    @Test
    void testPurchaseListingNotEnoughStockFails() throws Exception {
        store.addNewOwner(founderID, ownerA);
        store.addNewListing(ownerA, "p8", "Keyboard", "Mechanical keyboard", 2, 150);

        String listingId = store.getListingsByProductName("Keyboard").get(0).getListingId();

        Exception ex = assertThrows(Exception.class, () -> {
            store.purchaseFromListing(listingId, 5); // try to buy more than available
        });

        assertTrue(ex.getMessage().contains("Not enough stock"));
    }

    @Test
    void testGetListingsByProductId() throws Exception {
        store.addNewOwner(founderID, ownerA);
        store.addNewListing(ownerA, "commonProductID", "Mouse", "Wireless Mouse", 2, 100);
        store.addNewListing(ownerA, "commonProductID", "Mouse", "Gaming Mouse", 1, 120);

        List<Listing> listings = store.getListingsByProductId("commonProductID");

        assertEquals(2, listings.size());
    }
}
