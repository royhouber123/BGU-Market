package market.domain;

import market.domain.store.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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
}
