package tests;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import market.domain.store.Listing;
import market.domain.store.Store;
import market.domain.store.StoreDTO;
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
        this.storeId = storeService.createStore(STORE_NAME, FOUNDER).getData();
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
                "1", "Tablet", "Electronic", "Android tablet", 5, 899);
        assertNotNull(res.getData());
        assertTrue(res.isSuccess());
    }


@Test public void owner_addNewProductToStore_negative_InvalidPrice() throws Exception {
        StoreDTO dto = storeService.getStore(STORE_NAME).getData();
        ApiResponse<String> res = storeService.addNewListing(
                FOUNDER, dto.getStoreID(),
                "1", "Tablet", "Electronic", "Android tablet", 5, -78);
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
                                              129.9).getData();
                                       
        
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
                                              129.9).getData();
                                       
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

@Test public void owner_requestStoreRoles_positive() {}
@Test public void owner_requestStoreRoles_negative() {}
@Test public void owner_requestStoreRoles_alternate() {}


// still need to write the edit functionality!!!!!!!!!!!
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
            129.9
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
            229.0
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



@Test public void owner_editStorePurchasePolicy_positive() {}
@Test public void owner_editStorePurchasePolicy_negative() {}
@Test public void owner_editStorePurchasePolicy_alternate() {}


@Test public void owner_editStoreDiscountPolicy_positive() {}
@Test public void owner_editStoreDiscountPolicy_negative_InValidObjectToCreatePolicyTo() {}
@Test public void owner_editStoreDiscountPolicy_alternate_InActiveStore() {}
   
@Test public void owner_respondToUserMessages_positive() {}
@Test public void owner_respondToUserMessages_negative() {}
@Test public void owner_respondToUserMessages_alternate() {}

@Test public void owner_viewStorePurchaseHistory_positive() {}
@Test public void owner_viewStorePurchaseHistory_negative() {}
@Test public void owner_viewStorePurchaseHistory_alternate() {}

}
