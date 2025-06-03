package tests;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import market.domain.store.Listing;
import market.domain.store.Store;
import market.domain.store.StoreDTO;
import market.dto.PolicyDTO;
import support.AcceptanceTestBase;
import utils.ApiResponse;

/**
 * Acceptance‑level scenarios for store owners, exercised **through StoreService**
 * (the public application layer), so the signatures always match the latest
 * StoreService you provided (addNewListing / removeListing etc.).
 */
public class StoreOwnerTests extends AcceptanceTestBase {

    private static final String FOUNDER = "100";   // simple textual IDs for tests
    private static final String OWNER_A = "200";
    private static final String MANAGER = "300";

    private String storeId;            // actual ID assigned by the repository
    private final String STORE_NAME = "GadgetStore";

    @BeforeEach
    void init() throws Exception {
        // Register test users first
        userService.register(FOUNDER, "password");
        userService.register(OWNER_A, "password");
        userService.register(MANAGER, "password");
        
        // Register additional users used in concurrent tests
        userService.register("X", "password");
        userService.register("y", "password");
        userService.register("ownerB", "password");
        userService.register("newManager", "password");
        userService.register("managerY", "password");
        userService.register("newOwner", "password");
        
        // fresh repo & services already built in AcceptanceTestBase.setup()
        this.storeId = storeService.createStore(STORE_NAME, FOUNDER).storeId();
        StoreDTO dto = storeService.getStore(STORE_NAME);
        assertNotNull(dto, "store should exist after creation");
    }

    // ───────────────────────────────────────────────────────────────── managers & permissions
    // @Test
    // void owner_sets_purchase_policy_as_permission() {
    //     storeService.addAdditionalStoreOwner(FOUNDER, OWNER_A, storeId);
    //     storeService.addNewManager(OWNER_A, MANAGER, storeId);

    //     int EDIT_PRODUCTS = 1; // suppose permission code 1 is EDIT_PRODUCTS
    //     String res = storeService.addPermissionToManager(MANAGER, OWNER_A, EDIT_PRODUCTS, storeId);
    //     assertEquals("success", res);
    //     assertTrue(storeService.getManagersPermissions(MANAGER, OWNER_A, storeId).contains(EDIT_PRODUCTS));
    // }

    // ───────────────────────────────────────────────────────────────── placeholder (no service API yet)

    // ############################################################################
    @Test
    public void owner_addNewProductToStore_positive() throws Exception {
        try{
            StoreDTO dto = storeService.getStore(STORE_NAME);
            String res = storeService.addNewListing(
                    FOUNDER, dto.getStoreID(),
                    "1", "Tablet", "Electronic", "Android tablet", 5, 899, "REGULAR");
            assertNotNull(res);
        } catch (Exception e) {
            fail("Expected no exception, but got: " + e.getMessage(), e);
        }
    }


    @Test
    public void owner_addNewProductToStore_negative_InvalidPrice() throws Exception {
        try {
            StoreDTO dto = storeService.getStore(STORE_NAME);
            String res = storeService.addNewListing(
                    FOUNDER, dto.getStoreID(),
                    "1", "Tablet", "Electronic", "Android tablet", 5, -78, "REGULAR");
            fail("Expected an exception for invalid price, but got: " + res);
        } catch (Exception e) {
            assertTrue(e.getMessage().toLowerCase().contains("the price of a products needs to be possitive"));
        }
    }

    //@Test
    //public void owner_addNewProductToStore_alternate_ProductWithSameName() {}

    @Test
    public void owner_removeProductFromStore_positive() {
        String listingId= storeService.addNewListing(FOUNDER,
                                                        storeId,
                                                "p‑2",
                                            "Mouse",
                                    "Electronic",
                                    "Wireless",
                                                    4,
                                                    129.9,
                                                    "REGULAR");
        try {                         
            storeService.removeListing(FOUNDER, storeId, listingId);
        } catch (Exception e) {
            fail("Expected no exception, but got: " + e.getMessage(), e);
        }
        try {
            productService.getListing(listingId);
        } catch (Exception e) {
            assertTrue(e.getMessage().toLowerCase().contains("not found"));
        }
    }

    @Test
    public void owner_removeProductFromStore_negative_ProductNotFound() {
        try {
            storeService.removeListing(FOUNDER, storeId, "omer");
            fail("Expected an exception for non-existent product, but got success response");
        } catch (Exception e) {
            assertTrue(e.getMessage().toLowerCase().contains("error removing listing"));
        }
    }

    @Test
    public void owner_removeProductFromStore_alternate_InactiveStore() {        
        String listingId= storeService.addNewListing(FOUNDER,
                                                    storeId,
                                            "p‑2",
                                        "Mouse",
                                "Electronic",
                                "Wireless",
                                                4,
                                                129.9, "REGULAR");                                      
        try {
            storeService.closeStore(storeId, FOUNDER);
            storeService.removeListing(FOUNDER, storeId, listingId);
            fail("Expected an exception for removing from closed store, but got success response");
        } catch (Exception e) {
            assertTrue(e.getMessage().toLowerCase().contains("error"));
        } try {
            Listing removed = productService.getListing(listingId);
            assertNotNull(removed, "Listing should still exist in the repository");
        } catch (Exception e) {
            fail("Expected no exception when fetching removed listing, but got: " + e.getMessage(), e);
        }
    }

    @Test
    public void owner_appointAdditionalStoreOwner_positive() {
        try {
            storeService.addAdditionalStoreOwner(FOUNDER, OWNER_A, storeId);
            boolean check = storeService.isOwner(storeId, OWNER_A);
            assertTrue(check);
        } catch (Exception e) {
            fail("Expected no exception, but got: " + e.getMessage(), e);
        }
    }


    @Test
    public void owner_appointAdditionalStoreOwner_negative_AlreadyAnOwner() {
        try {
            storeService.addAdditionalStoreOwner(FOUNDER, OWNER_A, storeId);
            storeService.addAdditionalStoreOwner(FOUNDER, OWNER_A, storeId);
            fail("Expected an exception for appointing an already existing owner, but got success response");
        } catch (Exception e) {
            assertTrue(e.getMessage().toLowerCase().contains("already an owner"));
        }
    }

    //still doesnt support this functionality
    @Test
    public void owner_appointAdditionalStoreOwner_alternate_TheSuppervisorDoesntAcceptTheAppointment() {}


    @Test
    public void owner_removeStoreOwner_positive() {
        try {
            storeService.addAdditionalStoreOwner(FOUNDER, OWNER_A, storeId);
            List<List<String>> res = storeService.removeOwner(FOUNDER, OWNER_A, storeId);
            boolean check = storeService.isOwner(storeId, OWNER_A);
            assertFalse(check);
        } catch (Exception e) {
            fail("Expected no exception, but got: " + e.getMessage(), e);
        }
    }


    @Test
    public void owner_removeStoreOwner_negative_TryingToRemoveOwnerNotApointedByHim() {
        try {
            storeService.addAdditionalStoreOwner(FOUNDER, OWNER_A, storeId);
            storeService.addAdditionalStoreOwner(FOUNDER, MANAGER, storeId);
            List<List<String>> res = storeService.removeOwner(MANAGER, OWNER_A, storeId);
            fail("Expected an exception for removing an owner not appointed by the requester, but got success response");
        } catch (Exception e) {
            assertTrue(e.getMessage().toLowerCase().contains("didn't assign"));
        }
    }

    @Test
    public void owner_removeStoreOwner_alternate_InactiveStore() {
        storeService.addAdditionalStoreOwner(FOUNDER, OWNER_A, storeId);
        storeService.closeStore(storeId, FOUNDER);
        try {
            List<List<String>> res = storeService.removeOwner(FOUNDER, OWNER_A, storeId);
            fail("Expected an exception for removing owner from closed store, but got success response");
        } catch (Exception e) {
            assertTrue(e.getMessage().toLowerCase().contains("is closed for now"));
        }
    }

    @Test
    public void owner_appointStoreManager_positive() {
        try {
            storeService.addNewManager(FOUNDER, MANAGER, storeId);
            boolean check = storeService.isManager(storeId, MANAGER);
            assertTrue(check);
        } catch (Exception e) {
            fail("Expected no exception, but got: " + e.getMessage(), e);
        }
    }

    @Test
    public void owner_appointStoreManager_negative_alreadyManager() {
        storeService.addNewManager(FOUNDER, MANAGER, storeId);
        try {
            storeService.addNewManager(FOUNDER, MANAGER, storeId);
        } catch (Exception e) {
            assertTrue(e.getMessage().toLowerCase().contains("already a manager"));
        }
        boolean check = storeService.isManager(storeId, MANAGER);
        assertTrue(check);
    }


    @Test
    public void owner_editStoreManagerPermissions_positive() {
        try {
            storeService.addNewManager(FOUNDER, MANAGER, storeId);
            storeService.addPermissionToManager(MANAGER, FOUNDER, Store.Permission.EDIT_PRODUCTS.getCode(), storeId);
            Set<Integer> check = storeService.getManagersPermissions(MANAGER, FOUNDER, storeId);
            assertTrue(()->check.contains(Store.Permission.EDIT_PRODUCTS.getCode()));
        } catch (Exception e) {
            fail("Expected no exception, but got: " + e.getMessage(), e);
        }
    }

    @Test
    public void owner_editStoreManagerPermissions_negative_NotManager() {
        try {
            storeService.addPermissionToManager(MANAGER, FOUNDER, Store.Permission.EDIT_PRODUCTS.getCode(), storeId);
        } catch (Exception e) {
            // Expected exception since MANAGER is not a manager yet
            assertTrue(e.getMessage().toLowerCase().contains("not a manager"));
        }
    }

    @Test
    public void owner_editStoreManagerPermissions_alternate_NotTheAppointerOfManager() {
        storeService.addNewManager(FOUNDER, MANAGER, storeId);
        storeService.addAdditionalStoreOwner(FOUNDER, OWNER_A, storeId);
        try {
            storeService.addPermissionToManager(MANAGER, OWNER_A, Store.Permission.EDIT_PRODUCTS.getCode(), storeId);
            fail("Expected an exception for trying to edit permissions by a non-appointer, but got success response");
        } catch (Exception e) {
            assertTrue(e.getMessage().toLowerCase().contains("cant add permission"));
        }
        Set<Integer> check = storeService.getManagersPermissions(MANAGER, FOUNDER, storeId);
        assertFalse(()->check.contains(Store.Permission.EDIT_PRODUCTS.getCode()));
    }

    @Test
    public void owner_editProductFromStore_positive() {
        try {
            String listingId = storeService.addNewListing(
                    FOUNDER,
                    storeId,
                    "p‑2",
                    "Mouse",
                    "Electronic",
                    "Wireless",
                    4,
                    129.9, "REGULAR"
            );

            boolean res = storeService.editListingPrice(FOUNDER, storeId, listingId, 99.9);
            assertTrue(res, "Expected successful price update");
            Listing updated = productService.getListing(listingId);
            assertEquals(99.9, updated.getPrice());
        } catch (Exception e) {
            fail("Expected no exception, but got: " + e.getMessage(), e);
        }
    }

    @Test
    public void owner_editProductFromStore_negative_InValidPrice() {
        String listingId = storeService.addNewListing(
                FOUNDER,
                storeId,
                "p‑3",
                "Keyboard",
                "Electronic",
                "Mechanical",
                5,
                229.0, "REGULAR"
        );
        try {
            boolean res = storeService.editListingPrice(FOUNDER, storeId, listingId, -10.0);
            fail("Expected an exception for invalid price, but got success response: " + res);
        } catch (Exception e) {
            // Expected exception for invalid price
            assertTrue(e.getMessage().toLowerCase().contains("illegal price"));
        }
    }

    @Test
    public void owner_editProductFromStore_alternate_ProductNotFound() {
        String fakeListingId = "non-existent-id";
        try {
            storeService.editListingPrice(FOUNDER, storeId, fakeListingId, 150.0);
            fail("Expected an exception for non-existent product, but got success response");
        } catch (Exception e) {
            // Expected exception for non-existent product
            assertTrue(e.getMessage().toLowerCase().contains("not found"));
        }
    }

    @Test
    public void owner_addStoreDiscountPolicy_positive() {
        try {
            String storeId = storeService.createStore("DiscountStore", FOUNDER).storeId();
            PolicyDTO.AddDiscountRequest dto = new PolicyDTO.AddDiscountRequest(
                "PERCENTAGE",                 // type
                "PRODUCT",                 // scope
                "p1",                      // scopeId (dummy product ID)
                0.2,                       // value = 20% discount
                null,                     // couponCode
                null,                     // condition
                List.of(),                // subDiscounts
                "SUM"                     // combinationType
            );
            storeService.addNewListing(FOUNDER, storeId, "p1", "TV", "Electronics", "Smart TV", 10, 2000.0, "REGULAR");
            boolean res = storePoliciesService.addDiscount(storeId, FOUNDER, dto);
            assertTrue(res);
        } catch (Exception e) {
            fail("Expected no exception, but got: " + e.getMessage(), e);
        }
    }

    //need to check valid value of discount!!!!
    // @Test
    // public void owner_addStoreDiscountPolicy_negative_invalidValue() {
    //     String storeId = storeService.createStore("DiscountStore", FOUNDER).getData();

    //     AddDiscountDTO dto = new AddDiscountDTO(
    //         "PERCENTAGE",              // type
    //         "PRODUCT",                 // scope
    //         "p1",                      // scopeId
    //         78,                      //  invalid negative discount
    //         null,
    //         null,
    //         List.of(),
    //         "SUM"
    //     );

    //     storeService.addNewListing(FOUNDER, storeId, "p1", "Laptop", "Electronics", "Gaming Laptop", 3, 3500);

    //     ApiResponse<Boolean> res = storePoliciesService.addDiscount(storeId, FOUNDER, dto);

    //     assertFalse(res.isSuccess());
    //     assertTrue(res.getError().toLowerCase().contains("invalid"));  // adjust message as needed
    // }

    @Test
    public void owner_addStoreDiscountPolicy_alternate_inactiveStore() {
        String storeId = storeService.createStore("DiscountStore", FOUNDER).storeId();
        storeService.addNewListing(FOUNDER, storeId, "p1", "Fridge", "Appliances", "Energy Saver", 5, 1200.0, "REGULAR");
        // Close the store before adding discount
        storeService.closeStore(storeId, FOUNDER);
        PolicyDTO.AddDiscountRequest dto = new PolicyDTO.AddDiscountRequest(
            "PERCENTAGE",
            "PRODUCT",
            "p1",
            0.1,
            null,
            null,
            List.of(),
            "SUM"
        );
        try {
            storePoliciesService.addDiscount(storeId, FOUNDER, dto);
            fail("Expected an exception for trying to add discount to a closed store, but got success response");
        } catch (Exception e) {
            // Expected exception for trying to add discount to a closed store
            assertTrue(e.getMessage().toLowerCase().contains("closed"));
        }
    }

    @Test
    public void owner_editStoreDiscountPolicy_positive() {
        try {
            String storeId = storeService.createStore("DiscountStore", FOUNDER).storeId();
            storeService.addNewListing(FOUNDER, storeId, "p1", "Tablet", "Electronics", "Android Tablet", 5, 1000.0, "REGULAR");
            // Original discount
            PolicyDTO.AddDiscountRequest oldDiscount = new PolicyDTO.AddDiscountRequest(
                "PERCENTAGE", "PRODUCT", "p1", 0.1, null, null, List.of(), "SUM"
            );
            storePoliciesService.addDiscount(storeId, FOUNDER, oldDiscount);
            // Remove old and add new (edit)
            storePoliciesService.removeDiscount(storeId, FOUNDER, oldDiscount);
            PolicyDTO.AddDiscountRequest newDiscount = new PolicyDTO.AddDiscountRequest(
                "PERCENTAGE", "PRODUCT", "p1", 0.2, null, null, List.of(), "SUM"
            );
            boolean res = storePoliciesService.addDiscount(storeId, FOUNDER, newDiscount);
            assertTrue(res);
        } catch (Exception e) {
            fail("Expected no exception, but got: " + e.getMessage(), e);
        }
    }

    //also need to check valid values!!!!!!!!!!! 
    // @Test
    // public void owner_editStoreDiscountPolicy_negative_InValidObjectToCreatePolicyTo() {
    //     String storeId = storeService.createStore("DiscountStore", FOUNDER).getData();
    //     storeService.addNewListing(FOUNDER, storeId, "p1", "TV", "Electronics", "Smart TV", 4, 2500);

    //     // Invalid discount object (missing type, invalid value)
    //     AddDiscountDTO invalid = new AddDiscountDTO(
    //         null,           // ❌ invalid type
    //         "PRODUCT",
    //         "p1",
    //         -0.3,           // ❌ invalid value
    //         null,
    //         null,
    //         List.of(),
    //         "SUM"
    //     );

    //     ApiResponse<Boolean> res = storePoliciesService.addDiscount(storeId, FOUNDER, invalid);

    //     assertFalse(res.isSuccess());
    //     assertTrue(res.getError().toLowerCase().contains("unsupported discount type"));  // Adjust as needed
    // }

    // need to check valid values!!!!!!!!!!!
    // @Test public void owner_editStoreDiscountPolicy_alternate_InActiveStore() {}


    @Test
    public void owner_editStorePurchasePolicy_positive() {
        try {
            String storeId = storeService.createStore("PolicyStore", FOUNDER).storeId();
            PolicyDTO.AddPurchasePolicyRequest oldPolicy = new PolicyDTO.AddPurchasePolicyRequest("MINITEMS", 2);
            boolean res1 = storePoliciesService.addPurchasePolicy(storeId, FOUNDER, oldPolicy);
            assertTrue(res1, "Expected successful addition of old policy");
            // Remove the old policy and add a new one (edit)
            storePoliciesService.removePurchasePolicy(storeId, FOUNDER, oldPolicy);
            PolicyDTO.AddPurchasePolicyRequest newPolicy = new PolicyDTO.AddPurchasePolicyRequest("MINITEMS", 5);
            boolean res = storePoliciesService.addPurchasePolicy(storeId, FOUNDER, newPolicy);
            assertTrue(res);
        } catch (Exception e) {
            fail("Expected no exception, but got: " + e.getMessage(), e);
        }
    }

    @Test
    public void owner_editStorePurchasePolicy_negative_InValidObjectToCreatePolicyTo() {
        String storeId = storeService.createStore("PolicyStore", FOUNDER).storeId();
        // Invalid policy: null type, negative value
        PolicyDTO.AddPurchasePolicyRequest invalidPolicy = new PolicyDTO.AddPurchasePolicyRequest(null, -3);
        try {
            storePoliciesService.addPurchasePolicy(storeId, FOUNDER, invalidPolicy);
            fail("Expected an exception for invalid policy, but got success response");
        } catch (Exception e) {
            // Expected exception for invalid policy
            assertTrue(e.getMessage().toLowerCase().contains("type"));
        }
    }

    @Test
    public void owner_editStorePurchasePolicy_alternate_InActiveStore() {
        String storeId = storeService.createStore("PolicyStore", FOUNDER).storeId();
        storeService.closeStore(storeId, FOUNDER);
        PolicyDTO.AddPurchasePolicyRequest policy = new PolicyDTO.AddPurchasePolicyRequest("MAXITEMS", 10);
        try {
            storePoliciesService.addPurchasePolicy(storeId, FOUNDER, policy);
            fail("Expected an exception for trying to add policy to a closed store, but got success response");
        } catch (Exception e) {
            // Expected exception for trying to add policy to a closed store
            assertTrue(e.getMessage().toLowerCase().contains("closed"));
        }
    }

    @Test
    public void acceptance_concurrentAddSameOwnerByMultipleAppointers() throws Exception {
        String OWNER_B = "y";
        storeService.addAdditionalStoreOwner(FOUNDER, OWNER_A, storeId);
        storeService.addAdditionalStoreOwner(FOUNDER, OWNER_B, storeId);

        String toAssign = "X";
        int threads = 10;
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            String appointer = (i % 2 == 0) ? OWNER_A : OWNER_B;

            new Thread(() -> {
                try {
                    storeService.addAdditionalStoreOwner(appointer, toAssign, storeId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // Expected exception for concurrent assignment
                    assertTrue(e.getMessage().toLowerCase().contains("already an owner"));
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        assertEquals(1, successCount.get(), "Only one thread should succeed in assigning the same user");
        assertTrue(storeService.isOwner(storeId, toAssign));
    }


   

    @Test
    public void acceptance_concurrentRemoveSameOwner() throws Exception {
        storeService.addAdditionalStoreOwner(FOUNDER, OWNER_A, storeId);

        int threads = 5;
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    List<List<String>> res = storeService.removeOwner(FOUNDER, OWNER_A, storeId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // Expected exception for concurrent removal
                    assertTrue(e.getMessage().toLowerCase().contains("not an owner"));
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        assertEquals(1, successCount.get(), "Only one thread should successfully remove the owner");
        assertFalse(storeService.isOwner(storeId, OWNER_A), "OWNER_A should no longer be an owner");
    }

    @Test
    public void acceptance_transitiveAssignWhileRemovingRoot() throws Exception {
        storeService.addAdditionalStoreOwner(FOUNDER, OWNER_A, storeId);

        String toAssign = "newOwner";
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);
        AtomicBoolean assignSucceeded = new AtomicBoolean(false);
        AtomicBoolean removeSucceeded = new AtomicBoolean(false);

        Thread assignThread = new Thread(() -> {
            try {
                startLatch.await();
                try {
                    storeService.addAdditionalStoreOwner(OWNER_A, toAssign, storeId);
                    assignSucceeded.set(true);
                } catch (Exception e) {
                    // Expected exception if OWNER_A is removed before assignment
                    assertTrue(e.getMessage().toLowerCase().contains("not an owner"));
                    assignSucceeded.set(false);
                }
            } catch (Exception ignored) {
            } finally {
                doneLatch.countDown();
            }
        });

        Thread removeThread = new Thread(() -> {
            try {
                startLatch.await();
                try {
                    List<List<String>> res = storeService.removeOwner(FOUNDER, OWNER_A, storeId);
                    removeSucceeded.set(true);
                } catch (Exception e) {
                    // Expected exception if OWNER_A is not an owner
                    assertTrue(e.getMessage().toLowerCase().contains("not an owner"));
                    removeSucceeded.set(false);
                }
            } catch (Exception ignored) {
            } finally {
                doneLatch.countDown();
            }
        });

        assignThread.start();
        removeThread.start();
        startLatch.countDown();
        doneLatch.await();

        boolean finalIsOwner = storeService.isOwner(storeId, toAssign);
        assertFalse(finalIsOwner, "If root owner was removed, transitive assignment must not persist");
    }


    @Test
    public void acceptance_concurrentAddSameManagerByMultipleOwners() throws Exception {
        String OWNER_B = "ownerB";
        String MANAGER_ID = "newManager";
        storeService.addAdditionalStoreOwner(FOUNDER, OWNER_A, storeId);
        storeService.addAdditionalStoreOwner(FOUNDER, OWNER_B, storeId);

        int threads = 6;
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            String appointer = (i % 2 == 0) ? OWNER_A : OWNER_B;

            new Thread(() -> {
                try {
                    storeService.addNewManager(appointer, MANAGER_ID, storeId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // Expected exception for concurrent assignment
                    assertTrue(e.getMessage().toLowerCase().contains("already a manager"));
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        assertEquals(1, successCount.get(), "Only one thread should succeed in assigning the manager");
        assertTrue(storeService.isManager(storeId, MANAGER_ID), "User should be assigned as manager");
    }


    @Test
    public void acceptance_concurrentRemoveSameManager() throws Exception {
        // Setup: FOUNDER → OWNER_A → MANAGER_ID
        String MANAGER_ID = "managerY";
        storeService.addAdditionalStoreOwner(FOUNDER, OWNER_A, storeId);
        storeService.addNewManager(OWNER_A, MANAGER_ID, storeId);

        int threads = 5;
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    storeService.removeManager(OWNER_A, MANAGER_ID, storeId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // Expected exception for concurrent removal
                    assertTrue(e.getMessage().toLowerCase().contains("not a manager"));
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        assertEquals(1, successCount.get(), "Only one thread should successfully remove the manager");
        assertFalse(storeService.isManager(storeId, MANAGER_ID), "Manager should no longer exist");
    }

    @Test
    public void manager_concurrentAddSamePermission_onlyOneEffective() throws Exception {
        storeService.addAdditionalStoreOwner(FOUNDER, OWNER_A, storeId);
        storeService.addNewManager(OWNER_A, MANAGER, storeId);

        int threads = 10;
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                storeService.addPermissionToManager(MANAGER, OWNER_A, Store.Permission.EDIT_PRODUCTS.getCode(), storeId);
                latch.countDown();
            }).start();
        }

        latch.await();

        Set<Integer> perms = storeService.getManagersPermissions(MANAGER, OWNER_A, storeId);
        assertEquals(2, perms.size());
        assertTrue(perms.contains(Store.Permission.EDIT_PRODUCTS.getCode()));
    }

    @Test
    public void manager_concurrentRemoveSamePermission_onlyOneSuccess() throws Exception {
        storeService.addAdditionalStoreOwner(FOUNDER, OWNER_A, storeId);
        storeService.addNewManager(OWNER_A, MANAGER, storeId);
        storeService.addPermissionToManager(MANAGER, OWNER_A, Store.Permission.EDIT_PRODUCTS.getCode(), storeId);

        int threads = 10;
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    storeService.removePermissionFromManager(MANAGER, Store.Permission.EDIT_PRODUCTS.getCode(), OWNER_A, storeId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // Expected exception for concurrent removal
                    assertTrue(e.getMessage().toLowerCase().contains("not a manager"));
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        assertEquals(1, successCount.get(), "Only one thread should succeed in removing the permission");
        Set<Integer> perms = storeService.getManagersPermissions(MANAGER, OWNER_A, storeId);
        assertFalse(perms.contains(Store.Permission.EDIT_PRODUCTS.getCode()));
    }

    @Test
    public void manager_concurrentAddAndRemovePermission_finalStateConsistent() throws Exception {
        storeService.addAdditionalStoreOwner(FOUNDER, OWNER_A, storeId);
        storeService.addNewManager(OWNER_A, MANAGER, storeId);

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch finish = new CountDownLatch(2);

        Thread addThread = new Thread(() -> {
            try {
                start.await();
                storeService.addPermissionToManager(MANAGER, OWNER_A, Store.Permission.EDIT_PRODUCTS.getCode(), storeId);
            } catch (Exception ignored) {
            } finally {
                finish.countDown();
            }
        });

        Thread removeThread = new Thread(() -> {
            try {
                start.await();
                storeService.removePermissionFromManager(MANAGER, Store.Permission.EDIT_PRODUCTS.getCode(), OWNER_A, storeId);
            } catch (Exception ignored) {
            } finally {
                finish.countDown();
            }
        });

        addThread.start();
        removeThread.start();
        start.countDown();
        finish.await();

        Set<Integer> perms = storeService.getManagersPermissions(MANAGER, OWNER_A, storeId);
        assertTrue(perms.size() <= 2);
    }

    //doesnt support this functionality!!!!
    @Test public void owner_respondToUserMessages_positive() {}
    @Test public void owner_respondToUserMessages_negative() {}
    @Test public void owner_respondToUserMessages_alternate() {}

    @Test public void owner_viewStorePurchaseHistory_positive() {}
    @Test public void owner_viewStorePurchaseHistory_negative() {}
    @Test public void owner_viewStorePurchaseHistory_alternate() {}

    @Test public void owner_requestStoreRoles_positive() {}
    @Test public void owner_requestStoreRoles_negative() {}
    @Test public void owner_requestStoreRoles_alternate() {}
}
