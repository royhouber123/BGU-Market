package market.domain;

import market.domain.store.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StoreUnitTests {

    private Store store;
    private final int founderID = 1;
    private final int ownerA = 2;
    private final int ownerB = 3;
// begin - assign Owner tests
    @BeforeEach
    void setUp() {
        store = new Store(100, "TestStore", founderID);
    }

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
    // end - assign Owner tests
}
