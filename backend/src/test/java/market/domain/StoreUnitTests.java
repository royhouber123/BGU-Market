package market.domain;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import market.domain.store.IListingRepository;
import market.domain.store.Listing;
import market.domain.store.Store;
import market.infrastructure.ListingRepository;

class StoreUnitTests {

    private Store store;
    private final String founderID = "1";
    private final String ownerA = "2";
    private final String ownerB = "3";
    private final String ownerC = "4";

    // ------------------------- Setup -------------------------
    @BeforeEach
    void setUp() {
        IListingRepository repo = new ListingRepository();
        store = new Store("999", "TestStore", founderID, repo);
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
        assertTrue(exception.getMessage().contains("is not an owner"), "Expected error for non-owner appointer");
    }

    @Test
    void testAddAlreadyOwnerShouldFail() throws Exception {
        store.addNewOwner(founderID, ownerA);
        Exception exception = assertThrows(Exception.class, () -> {
            store.addNewOwner(founderID, ownerA);
        });
        assertTrue(exception.getMessage().contains("already an owner"), "Expected error for already an owner");
    }

    // ------------------------- Owner Removal Tests -------------------------

    @Test
    void testRemoveOwnerRecursiveSuccess() throws Exception {
        // Build hierarchy: founder → A → B → C
        store.addNewOwner(founderID, ownerA);
        store.addNewOwner(ownerA, ownerB);
        store.addNewOwner(ownerB, ownerC);

        List<List<String>> removed = store.removeOwner(founderID, ownerA);

        assertTrue(removed.get(0).contains(ownerA));
        assertTrue(removed.get(0).contains(ownerB));
        assertTrue(removed.get(0).contains(ownerC));

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

        assertTrue(ex.getMessage().contains("is the founder"));
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

        assertTrue(ex.getMessage().contains("is the founder"));
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

    // @Test
    // void testAddManagerByNonOwnerShouldFail() {
    //     Exception ex = assertThrows(Exception.class, () -> {
    //         store.addNewManager(ownerA, ownerB); // ownerA is not yet an owner
    //     });
    //     assertTrue(ex.getMessage().contains("is not a owner"));
    // }

    @Test
    void testAddAlreadyExistingManagerShouldFail() throws Exception {
        store.addNewOwner(founderID, ownerA);
        store.addNewManager(ownerA, ownerB);

        Exception ex = assertThrows(Exception.class, () -> {
            store.addNewManager(ownerA, ownerB); // ownerB is already a manager
        });
        assertTrue(ex.getMessage().contains("already a manager"));
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
        String added = store.addNewListing(ownerA, "p1", "Laptop", "Electronic", "Gaming laptop", 5, 2000.0, "REGULAR");

        // Assert
        assertFalse(added.contains("doesn't have permission"));
        assertEquals(1, store.getListingsByProductName("Laptop").size());
    }

    @Test
    void testAddNewListingByManagerWithPermissionSuccess() throws Exception {
        store.addNewOwner(founderID, ownerA);
        store.addNewManager(ownerA, ownerB);
        store.addPermissionToManager(ownerB, ownerA, Store.Permission.EDIT_PRODUCTS.getCode());

        String added = store.addNewListing(ownerB, "p2", "Phone","Electronic" , "iPhone", 10, 1000.0, "REGULAR");

        assert(!added.contains("doesn't have permission"));
        assertEquals(1, store.getListingsByProductName("Phone").size());
    }

    @Test
    void testAddNewListingByManagerWithoutPermissionFails() throws Exception {
        store.addNewOwner(founderID, ownerA);
        store.addNewManager(ownerA, ownerB);

        Exception ex = assertThrows(Exception.class, () -> {
            store.addNewListing(ownerB, "p3", "Tablet", "Electronic", "Samsung Tab", 7, 600.0, "REGULAR");
        });

        assertTrue(ex.getMessage().contains("doesn't have permission"));
    }

    @Test
    void testRemoveListingByOwnerSuccess() throws Exception {
        store.addNewOwner(founderID, ownerA);
        store.addNewListing(ownerA, "p4", "TV", "Electronic", "Smart TV", 3, 1500.0, "REGULAR");

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

        store.addNewListing(ownerA, "p5", "Speaker", "Electronic", "Bluetooth speaker", 8, 300.0, "REGULAR");
        String listingId = store.getListingsByProductName("Speaker").get(0).getListingId();

        boolean removed = store.removeListing(ownerB, listingId);

        assertTrue(removed);
        assertTrue(store.getListingsByProductName("Speaker").isEmpty());
    }

    @Test
    void testRemoveListingByManagerWithoutPermissionFails() throws Exception {
        store.addNewOwner(founderID, ownerA);
        store.addNewManager(ownerA, ownerB);

        store.addNewListing(ownerA, "p6", "Headphones", "Electronic", "Noise cancelling", 4, 250.0, "REGULAR");
        String listingId = store.getListingsByProductName("Headphones").get(0).getListingId();

        Exception ex = assertThrows(Exception.class, () -> {
            store.removeListing(ownerB, listingId);
        });

        assertTrue(ex.getMessage().contains("doesn't have permission"));
    }

    @Test
    void testPurchaseListingReducesQuantity() throws Exception {
        store.addNewOwner(founderID, ownerA);
        store.addNewListing(ownerA, "p7", "Monitor", "Electronic", "4K Monitor", 6, 800.0, "REGULAR");

        String listingId = store.getListingsByProductName("Monitor").get(0).getListingId();
        boolean purchased = store.purchaseFromListing(listingId, 2);

        assertTrue(purchased);
        assertEquals(4, store.getListing(listingId).getQuantityAvailable());
    }

    @Test
    void testPurchaseListingNotEnoughStockFails() throws Exception {
        store.addNewOwner(founderID, ownerA);
        store.addNewListing(ownerA, "p8", "Keyboard", "Electronic", "Mechanical keyboard", 2, 150.0, "REGULAR");

        String listingId = store.getListingsByProductName("Keyboard").get(0).getListingId();

        Exception ex = assertThrows(Exception.class, () -> {
            store.purchaseFromListing(listingId, 5); // try to buy more than available
        });

        assertTrue(ex.getMessage().contains("Not enough stock"));
    }

    @Test
    void testGetListingsByProductId() throws Exception {
        store.addNewOwner(founderID, ownerA);
        store.addNewListing(ownerA, "commonProductID", "Mouse", "Electronic", "Wireless Mouse", 2, 100.0, "REGULAR");
        store.addNewListing(ownerA, "commonProductID", "Mouse", "Electronic", "Gaming Mouse", 1, 120.0, "REGULAR");

        List<Listing> listings = store.getListingsByProductId("commonProductID");

        assertEquals(2, listings.size());
    }



    //concurrency tests!!!!!!!!!!!11
    @Test
    void testConcurrentAddSameOwnerByMultipleAppointers() throws Exception {
        store.addNewOwner(founderID, ownerA);
        store.addNewOwner(founderID, ownerB);

        String toAssign = "X";
        int threads = 10;
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            String appointer = (i % 2 == 0) ? ownerA : ownerB;
            new Thread(() -> {
                try {
                    store.addNewOwner(appointer, toAssign);
                    successCount.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();
        assertEquals(1, successCount.get(), "Only one assignment should succeed");
        assertTrue(store.isOwner(toAssign));
        assertNotNull(store.OwnerAssignedBy(toAssign));
    }


    @Test
    void testConcurrentRemoveSameOwner() throws Exception {
        store.addNewOwner(founderID, ownerA);

        int threads = 5;
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    store.removeOwner(founderID, ownerA);
                    successCount.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        assertEquals(1, successCount.get(), "Only one thread should successfully remove the owner");
        assertFalse(store.isOwner(ownerA), "ownerA should no longer be an owner");
    }

    //TODO: need to see why this test fail
    @Test
    void testTransitiveAssignWhileRemovingRoot() throws Exception {
        store.addNewOwner(founderID, ownerA);

        String toAssign = "newOwner";
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);
        AtomicBoolean assignSucceeded = new AtomicBoolean(false);
        AtomicBoolean removed = new AtomicBoolean(false);

        Thread assignThread = new Thread(() -> {
            try {
                startLatch.await();
                store.addNewOwner(ownerA, toAssign); // intermediate assignment
                assignSucceeded.set(true);
            } catch (Exception ignored) {
            } finally {
                doneLatch.countDown();
            }
        });

        Thread removeThread = new Thread(() -> {
            try {
                startLatch.await();
                store.removeOwner(founderID, ownerA); // cuts off the branch
                removed.set(true);
            } catch (Exception ignored) {
            } finally {
                doneLatch.countDown();
            }
        });

        assignThread.start();
        removeThread.start();
        startLatch.countDown();
        doneLatch.await();

        // The final ownership state should be consistent
        assertFalse(store.isOwner(ownerA));
        assertFalse(store.isOwner(toAssign), "If root was removed, the transitive assignment must also fail");
    }


    @Test
    void testConcurrentAddSameManagerByMultipleOwners() throws Exception {
        store.addNewOwner(founderID, ownerA);
        store.addNewOwner(founderID, ownerB);

        String toAssign = "newManager";
        int threads = 6;
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            String appointer = (i % 2 == 0) ? ownerA : ownerB;

            new Thread(() -> {
                try {
                    store.addNewManager(appointer, toAssign);
                    successCount.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        assertEquals(1, successCount.get(), "Only one thread should succeed in assigning the same manager");
        assertTrue(store.isManager(toAssign), "User should be assigned as manager");
    }


    @Test
    void testRemoveManagerByCorrectAppointer_success() throws Exception {
        store.addNewOwner(founderID, ownerA);
        store.addNewManager(ownerA, ownerB);

        assertTrue(store.isManager(ownerB), "User should be a manager before removal");

        boolean result = store.removeManager(ownerA, ownerB);

        assertTrue(result, "Removal should succeed");
        assertFalse(store.isManager(ownerB), "User should no longer be a manager");
    }

    @Test
    void testRemoveManagerByWrongAppointer_shouldFail() throws Exception {
        store.addNewOwner(founderID, ownerA);
        store.addNewOwner(founderID, ownerB);
        store.addNewManager(ownerA, ownerC); // ownerA assigned ownerC as manager

        Exception ex = assertThrows(Exception.class, () -> {
            store.removeManager(ownerB, ownerC); // ownerB tries to remove
        });

        assertTrue(ex.getMessage().contains("did not assign"));
    }



    @Test
    void testRemoveManager_userIsNotManager_shouldFail() throws Exception {
        store.addNewOwner(founderID, ownerA);

        Exception ex = assertThrows(Exception.class, () -> {
            store.removeManager(ownerA, ownerB); // ownerB was never assigned
        });

        assertTrue(ex.getMessage().contains("did not assign"));
    }


    @Test
    void testRemoveManager_appointerIsNotOwner_shouldFail() throws Exception {
        // ownerA is not an owner yet

        Exception ex = assertThrows(Exception.class, () -> {
            store.removeManager(ownerA, ownerB);
        });

        assertTrue(ex.getMessage().contains("is not an owner"));
    }


    @Test
    void testConcurrentRemoveSameManager() throws Exception {
        store.addNewOwner(founderID, ownerA);
        store.addNewManager(ownerA, ownerB);

        int threads = 5;
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    store.removeManager(ownerA, ownerB);
                    successCount.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        assertEquals(1, successCount.get(), "Only one thread should successfully remove the manager");
        assertFalse(store.isManager(ownerB), "Manager should be removed");
    }


    // @Test
    // void testConcurrentAssignAndRemoveManager() throws Exception {
    //     store.addNewOwner(founderID, ownerA);
    //     String managerID = "M";

    //     // Assign first (optional depending on test intent)
    //     store.addNewManager(ownerA, managerID);

    //     CountDownLatch startLatch = new CountDownLatch(1);
    //     CountDownLatch doneLatch = new CountDownLatch(2);
    //     AtomicBoolean assignSucceeded = new AtomicBoolean(false);
    //     AtomicBoolean removeSucceeded = new AtomicBoolean(false);

    //     Thread assignThread = new Thread(() -> {
    //         try {
    //             startLatch.await();
    //             store.addNewManager(ownerA, managerID);
    //             assignSucceeded.set(true);
    //         } catch (Exception ignored) {
    //         } finally {
    //             doneLatch.countDown();
    //         }
    //     });

    //     Thread removeThread = new Thread(() -> {
    //         try {
    //             startLatch.await();
    //             store.removeManager(ownerA, managerID);
    //             removeSucceeded.set(true);
    //         } catch (Exception ignored) {
    //         } finally {
    //             doneLatch.countDown();
    //         }
    //     });

    //     assignThread.start();
    //     removeThread.start();
    //     startLatch.countDown();
    //     doneLatch.await();

    //     boolean currentlyManager = store.isManager(managerID);
    //     assertFalse(assignSucceeded.get() && removeSucceeded.get(), "Should not both assign and remove concurrently");
    //     assertEquals(assignSucceeded.get(), currentlyManager, "Manager state should match assign result");
    // }


  @Test
    void concurrentAddSamePermissionToManager_onlyOneEffective() throws Exception {
        store.addNewOwner(founderID, ownerA);
        store.addNewManager(ownerA, ownerB);

        int threads = 10;
        CountDownLatch latch = new CountDownLatch(threads);
        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    store.addPermissionToManager(ownerB, ownerA, Store.Permission.EDIT_PRODUCTS.getCode());
                } catch (Exception ignored) {}
                finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        Set<Integer> permissions = store.getManagersPermmisions(ownerB, ownerA);
        assertEquals(2, permissions.size());
        assertTrue(permissions.contains(Store.Permission.EDIT_PRODUCTS.getCode()));
    }


    @Test
void concurrentRemoveSamePermission_onlyOneThreadSucceeds() throws Exception {
    store.addNewOwner(founderID, ownerA);
    store.addNewManager(ownerA, ownerB);
    store.addPermissionToManager(ownerB, ownerA, Store.Permission.EDIT_PRODUCTS.getCode());

    int threads = 10;
    CountDownLatch latch = new CountDownLatch(threads);
    AtomicInteger successCount = new AtomicInteger();

    for (int i = 0; i < threads; i++) {
        new Thread(() -> {
            try {
                boolean removed = store.removePermissionFromManager(ownerB, Store.Permission.EDIT_PRODUCTS.getCode(), ownerA);
                if (removed) successCount.incrementAndGet();
            } catch (Exception ignored) {}
            finally {
                latch.countDown();
            }
        }).start();
    }

    latch.await();

    assertEquals(1, successCount.get(), "Only one thread should successfully remove the permission");
    assertFalse(store.getManagersPermmisions(ownerB, ownerA).contains(Store.Permission.EDIT_PRODUCTS.getCode()));
}


@Test
void concurrentAddAndRemovePermission_permissionStateConsistent() throws Exception {
    store.addNewOwner(founderID, ownerA);
    store.addNewManager(ownerA, ownerB);

    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch finish = new CountDownLatch(2);

    Thread addThread = new Thread(() -> {
        try {
            start.await();
            store.addPermissionToManager(ownerB, ownerA, Store.Permission.EDIT_PRODUCTS.getCode());
        } catch (Exception ignored) {}
        finally {
            finish.countDown();
        }
    });

    Thread removeThread = new Thread(() -> {
        try {
            start.await();
            store.removePermissionFromManager(ownerB, Store.Permission.EDIT_PRODUCTS.getCode(), ownerA);
        } catch (Exception ignored) {}
        finally {
            finish.countDown();
        }
    });

    addThread.start();
    removeThread.start();
    start.countDown();
    finish.await();

    // Final state must be either contains or not — but consistent
    Set<Integer> permissions = store.getManagersPermmisions(ownerB, ownerA);
    assertTrue(permissions.size() <= 2);
}




}
