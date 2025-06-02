package tests;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        // fresh repo & services already built in AcceptanceTestBase.setup()
        this.storeId = storeService.createStore(STORE_NAME, FOUNDER).getData().storeId();
        StoreDTO dto = storeService.getStore(STORE_NAME).getData();
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
@Test public void owner_addNewProductToStore_positive() throws Exception {
        StoreDTO dto = storeService.getStore(STORE_NAME).getData();
        ApiResponse<String> res = storeService.addNewListing(
                FOUNDER, dto.getStoreID(),
                "1", "Tablet", "Electronic", "Android tablet", 5, 899.0, "REGULAR");
        assertNotNull(res.getData());
        assertTrue(res.isSuccess());
    }


@Test public void owner_addNewProductToStore_negative_InvalidPrice() throws Exception {
        StoreDTO dto = storeService.getStore(STORE_NAME).getData();
        ApiResponse<String> res = storeService.addNewListing(
                FOUNDER, dto.getStoreID(),
                "1", "Tablet", "Electronic", "Android tablet", 5, -78.0, "REGULAR");
        assertEquals(null, res.getData());
        assertFalse(res.isSuccess());
    }

//@Test public void owner_addNewProductToStore_alternate_ProductWithSameName() {}

@Test public void owner_removeProductFromStore_positive() {
     String listingId= storeService.addNewListing(FOUNDER,
                                                  storeId,
                                          "p‑2",
                                      "Mouse",
                             "Electronic",
                            "Wireless",
                                               4,
                                              129.9, "REGULAR").getData();
                                       
        
        ApiResponse res = storeService.removeListing(FOUNDER, storeId, listingId);
        assertTrue(res.isSuccess());
        ApiResponse<Listing> removed = productService.getListing(listingId);
        assertFalse(removed.isSuccess());
    }



@Test public void owner_removeProductFromStore_negative_ProductNotFound() {
        ApiResponse res = storeService.removeListing(FOUNDER, storeId, "omer");
        assertFalse(res.isSuccess());
        assertTrue(res.getError().toLowerCase().contains("error"));
}


@Test public void owner_removeProductFromStore_alternate_InactiveStore() {
    String listingId= storeService.addNewListing(FOUNDER,
                                                  storeId,
                                          "p‑2",
                                      "Mouse",
                             "Electronic",
                            "Wireless",
                                               4,
                                              129.9, "REGULAR").getData();
                                       
        storeService.closeStore(storeId, FOUNDER);
        ApiResponse res = storeService.removeListing(FOUNDER, storeId, listingId);
        assertFalse(res.isSuccess());
        assertTrue(res.getError().toLowerCase().contains("error"));
        ApiResponse<Listing> removed = productService.getListing(listingId);
        assertTrue(removed.isSuccess());
}



@Test public void owner_appointAdditionalStoreOwner_positive() {
    ApiResponse res = storeService.addAdditionalStoreOwner(FOUNDER, OWNER_A, storeId);
    assertTrue(res.isSuccess());
    ApiResponse<Boolean> check = storeService.isOwner(storeId, OWNER_A);
    assertTrue(check.getData());
}


@Test public void owner_appointAdditionalStoreOwner_negative_AlreadyAnOwner() {
    storeService.addAdditionalStoreOwner(FOUNDER, OWNER_A, storeId);
    ApiResponse res = storeService.addAdditionalStoreOwner(FOUNDER, OWNER_A, storeId);
    assertFalse(res.isSuccess());
    assertTrue(res.getError().toLowerCase().contains("already an owner"));
}

//still doesnt support this functionality
@Test public void owner_appointAdditionalStoreOwner_alternate_TheSuppervisorDoesntAcceptTheAppointment() {}


@Test public void owner_removeStoreOwner_positive() {
        storeService.addAdditionalStoreOwner(FOUNDER, OWNER_A, storeId);
        ApiResponse<List<List<String>>> res = storeService.removeOwner(FOUNDER, OWNER_A, storeId);
        assertTrue(res.isSuccess());
        ApiResponse<Boolean> check = storeService.isOwner(storeId, OWNER_A);
        assertFalse(check.getData());
}


@Test public void owner_removeStoreOwner_negative_TryingToRemoveOwnerNotApointedByHim() {
    storeService.addAdditionalStoreOwner(FOUNDER, OWNER_A, storeId);
    storeService.addAdditionalStoreOwner(FOUNDER, MANAGER, storeId);
    ApiResponse<List<List<String>>> res = storeService.removeOwner(MANAGER, OWNER_A, storeId);
    assertFalse(res.isSuccess());
    assertTrue(res.getError().toLowerCase().contains("didn't assign"));
}



@Test public void owner_removeStoreOwner_alternate_InactiveStore() {
    storeService.addAdditionalStoreOwner(FOUNDER, OWNER_A, storeId);
    storeService.closeStore(storeId, FOUNDER);
    ApiResponse<List<List<String>>> res = storeService.removeOwner(FOUNDER, OWNER_A, storeId);
    assertFalse(res.isSuccess());
    assertTrue(res.getError().toLowerCase().contains("is closed for now"));
}




@Test public void owner_appointStoreManager_positive() {
    ApiResponse res = storeService.addNewManager(FOUNDER, MANAGER, storeId);
    assertTrue(res.isSuccess());
    ApiResponse<Boolean> check = storeService.isManager(storeId, MANAGER);
    assertTrue(check.getData());
}

@Test public void owner_appointStoreManager_negative_alreadyManager() {
    storeService.addNewManager(FOUNDER, MANAGER, storeId);
    ApiResponse res = storeService.addNewManager(FOUNDER, MANAGER, storeId);
    assertFalse(res.isSuccess());
    assertTrue(res.getError().toLowerCase().contains("already a manager"));
    ApiResponse<Boolean> check = storeService.isManager(storeId, MANAGER);
    assertTrue(check.getData());
}

//still doesnt support that
@Test public void owner_appointStoreManager_alternate_apoointmentDicline() {}


@Test public void owner_editStoreManagerPermissions_positive() {
    storeService.addNewManager(FOUNDER, MANAGER, storeId);
    ApiResponse res = storeService.addPermissionToManager(MANAGER, FOUNDER, Store.Permission.EDIT_PRODUCTS.getCode(), storeId);
    assertTrue(res.isSuccess());
    Set<Integer> check = storeService.getManagersPermissions(MANAGER, FOUNDER, storeId).getData();
    assertTrue(()->check.contains(Store.Permission.EDIT_PRODUCTS.getCode()));
}


@Test public void owner_editStoreManagerPermissions_negative_NotManager() {
    ApiResponse res = storeService.addPermissionToManager(MANAGER, FOUNDER, Store.Permission.EDIT_PRODUCTS.getCode(), storeId);
    assertFalse(res.isSuccess());
    assertTrue(res.getError().toLowerCase().contains("not a manager"));
}


@Test public void owner_editStoreManagerPermissions_alternate_NotTheAppointerOfManager() {
    storeService.addNewManager(FOUNDER, MANAGER, storeId);
    storeService.addAdditionalStoreOwner(FOUNDER, OWNER_A, storeId);
    ApiResponse res = storeService.addPermissionToManager(MANAGER, OWNER_A, Store.Permission.EDIT_PRODUCTS.getCode(), storeId);
    assertFalse(res.isSuccess());
    assertTrue(res.getError().toLowerCase().contains("cant add permission"));
    Set<Integer> check = storeService.getManagersPermissions(MANAGER, FOUNDER, storeId).getData();
    assertFalse(()->check.contains(Store.Permission.EDIT_PRODUCTS.getCode()));
}




@Test
public void owner_editProductFromStore_positive() {
    String listingId = storeService.addNewListing(
            FOUNDER,
            storeId,
            "p‑2",
            "Mouse",
            "Electronic",
            "Wireless",
            4,
            129.9, "REGULAR"
    ).getData();

    ApiResponse<Boolean> res = storeService.editListingPrice(FOUNDER, storeId, listingId, 99.9);
    assertTrue(res.isSuccess());

    ApiResponse<Listing> updated = productService.getListing(listingId);
    assertTrue(updated.isSuccess());
    assertEquals(99.9, updated.getData().getPrice());
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
    ).getData();

    ApiResponse<Boolean> res = storeService.editListingPrice(FOUNDER, storeId, listingId, -10.0);
    assertFalse(res.isSuccess());
    assertTrue(res.getError().toLowerCase().contains("illegal price"));
}



@Test
public void owner_editProductFromStore_alternate_ProductNotFound() {
    String fakeListingId = "non-existent-id";

    ApiResponse<Boolean> res = storeService.editListingPrice(FOUNDER, storeId, fakeListingId, 150.0);
    assertFalse(res.isSuccess());
    assertTrue(res.getError().toLowerCase().contains("not found"));
}


@Test public void owner_addStoreDiscountPolicy_positive() {
    String storeId = storeService.createStore("DiscountStore", FOUNDER).getData().storeId();

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
    ApiResponse<Boolean> res = storePoliciesService.addDiscount(storeId, FOUNDER, dto);

    assertTrue(res.isSuccess());
    assertTrue(res.getData());
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
    String storeId = storeService.createStore("DiscountStore", FOUNDER).getData().storeId();

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

    ApiResponse<Boolean> res = storePoliciesService.addDiscount(storeId, FOUNDER, dto);

    assertFalse(res.isSuccess());
    assertTrue(res.getError().toLowerCase().contains("closed"));  // adjust message as needed
}

@Test
public void owner_editStoreDiscountPolicy_positive() {
    String storeId = storeService.createStore("DiscountStore", FOUNDER).getData().storeId();
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
    ApiResponse<Boolean> res = storePoliciesService.addDiscount(storeId, FOUNDER, newDiscount);

    assertTrue(res.isSuccess());
    assertTrue(res.getData());
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

@Test public void owner_editStoreDiscountPolicy_alternate_InActiveStore() {}


@Test
public void owner_editStorePurchasePolicy_positive() {
    String storeId = storeService.createStore("PolicyStore", FOUNDER).getData().storeId();

    PolicyDTO.AddPurchasePolicyRequest oldPolicy = new PolicyDTO.AddPurchasePolicyRequest("MINITEMS", 2);
    storePoliciesService.addPurchasePolicy(storeId, FOUNDER, oldPolicy);

    // Remove the old policy and add a new one (edit)
    storePoliciesService.removePurchasePolicy(storeId, FOUNDER, oldPolicy);
    PolicyDTO.AddPurchasePolicyRequest newPolicy = new PolicyDTO.AddPurchasePolicyRequest("MINITEMS", 5);

    ApiResponse<Boolean> res = storePoliciesService.addPurchasePolicy(storeId, FOUNDER, newPolicy);
    assertTrue(res.isSuccess());
    assertTrue(res.getData());
}

@Test
public void owner_editStorePurchasePolicy_negative_InValidObjectToCreatePolicyTo() {
    String storeId = storeService.createStore("PolicyStore", FOUNDER).getData().storeId();

    // Invalid policy: null type, negative value
    PolicyDTO.AddPurchasePolicyRequest invalidPolicy = new PolicyDTO.AddPurchasePolicyRequest(null, -3);

    ApiResponse<Boolean> res = storePoliciesService.addPurchasePolicy(storeId, FOUNDER, invalidPolicy);

    assertFalse(res.isSuccess());
    assertTrue(res.getError().toLowerCase().contains("type")); 
}



@Test
public void owner_editStorePurchasePolicy_alternate_InActiveStore() {
    String storeId = storeService.createStore("PolicyStore", FOUNDER).getData().storeId();

    storeService.closeStore(storeId, FOUNDER);

    PolicyDTO.AddPurchasePolicyRequest policy = new PolicyDTO.AddPurchasePolicyRequest("MAXITEMS", 10);

    ApiResponse<Boolean> res = storePoliciesService.addPurchasePolicy(storeId, FOUNDER, policy);

    assertFalse(res.isSuccess());
    assertTrue(res.getError().toLowerCase().contains("closed")); // Adjust based on actual message
}

@Test public void acceptance_concurrentAddSameOwnerByMultipleAppointers() throws Exception {
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
            ApiResponse<Void> res = storeService.addAdditionalStoreOwner(appointer, toAssign, storeId);
            if (res.isSuccess()) {
                successCount.incrementAndGet();
            }
            latch.countDown();
        }).start();
    }

    latch.await();

    assertEquals(1, successCount.get(), "Only one thread should succeed in assigning the same user");
    assertTrue(storeService.isOwner(storeId, toAssign).getData());
}


   

@Test
public void acceptance_concurrentRemoveSameOwner() throws Exception {
    storeService.addAdditionalStoreOwner(FOUNDER, OWNER_A, storeId);

    int threads = 5;
    CountDownLatch latch = new CountDownLatch(threads);
    AtomicInteger successCount = new AtomicInteger();

    for (int i = 0; i < threads; i++) {
        new Thread(() -> {
            ApiResponse<List<List<String>>> res = storeService.removeOwner(FOUNDER, OWNER_A, storeId);
            if (res.isSuccess()) {
                successCount.incrementAndGet();
            }
            latch.countDown();
        }).start();
    }

    latch.await();

    assertEquals(1, successCount.get(), "Only one thread should successfully remove the owner");
    assertFalse(storeService.isOwner(storeId, OWNER_A).getData(), "OWNER_A should no longer be an owner");
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
            ApiResponse<Void> res = storeService.addAdditionalStoreOwner(OWNER_A, toAssign, storeId);
            assignSucceeded.set(res.isSuccess());
        } catch (Exception ignored) {
        } finally {
            doneLatch.countDown();
        }
    });

    Thread removeThread = new Thread(() -> {
        try {
            startLatch.await();
            ApiResponse<List<List<String>>> res = storeService.removeOwner(FOUNDER, OWNER_A, storeId);
            removeSucceeded.set(res.isSuccess());
        } catch (Exception ignored) {
        } finally {
            doneLatch.countDown();
        }
    });

    assignThread.start();
    removeThread.start();
    startLatch.countDown();
    doneLatch.await();

    boolean finalIsOwner = storeService.isOwner(storeId, toAssign).getData();
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
            ApiResponse<Void> res = storeService.addNewManager(appointer, MANAGER_ID, storeId);
            if (res.isSuccess()) {
                successCount.incrementAndGet();
            }
            latch.countDown();
        }).start();
    }

    latch.await();

    assertEquals(1, successCount.get(), "Only one thread should succeed in assigning the manager");
    assertTrue(storeService.isManager(storeId, MANAGER_ID).getData(), "User should be assigned as manager");
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
            ApiResponse<Void> res = storeService.removeManager(OWNER_A, MANAGER_ID, storeId);
            if (res.isSuccess()) {
                successCount.incrementAndGet();
            }
            latch.countDown();
        }).start();
    }

    latch.await();

    assertEquals(1, successCount.get(), "Only one thread should successfully remove the manager");
    assertFalse(storeService.isManager(storeId, MANAGER_ID).getData(), "Manager should no longer exist");
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

    Set<Integer> perms = storeService.getManagersPermissions(MANAGER, OWNER_A, storeId).getData();
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
            ApiResponse<Void> res = storeService.removePermissionFromManager(MANAGER, Store.Permission.EDIT_PRODUCTS.getCode(), OWNER_A, storeId);
            if (res.isSuccess()) {
                successCount.incrementAndGet();
            }
            latch.countDown();
        }).start();
    }

    latch.await();

    assertEquals(1, successCount.get(), "Only one thread should succeed in removing the permission");
    Set<Integer> perms = storeService.getManagersPermissions(MANAGER, OWNER_A, storeId).getData();
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

    Set<Integer> perms = storeService.getManagersPermissions(MANAGER, OWNER_A, storeId).getData();
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
