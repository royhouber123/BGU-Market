package market.domain;
import  java.util.*;
import market.domain.store.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StoreUnitTests {

    private Store store;
    private final int founderID = 1;
    private final int ownerA = 2;
    private final int ownerB = 3;
// -------------------------begin----------- assign Owner ------------------
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
    // -------------------------end ---------------- assign Owner -----------------


    //-----------------------------begin--------------remove owner---------------------
        private final int ownerC = 4;

        @Test
        void testRemoveOwnerRecursiveSuccess() throws Exception {
            // Build hierarchy: founder → A → B → C
            store.addNewOwner(founderID, ownerA);
            store.addNewOwner(ownerA, ownerB);
            store.addNewOwner(ownerB, ownerC);

            List<Integer> removed = store.removeOwner(founderID, ownerA);

            assertTrue(removed.contains(ownerA));
            assertTrue(removed.contains(ownerB));
            assertTrue(removed.contains(ownerC));

            // All should no longer be owners
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
    }


