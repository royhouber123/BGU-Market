package tests;

import market.application.StoreService;
import market.domain.store.StoreDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import support.AcceptanceTestBase;

import static org.junit.jupiter.api.Assertions.*;

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
        this.storeId = storeService.createStore(STORE_NAME, FOUNDER);
        StoreDTO dto = storeService.getStore(STORE_NAME);
        assertNotNull(dto, "store should exist after creation");
    }

    // ───────────────────────────────────────────────────────────────── store creation
    @Test
    void subscriber_opens_new_store_successfully() throws Exception {
        StoreDTO dto = storeService.getStore(STORE_NAME);
        assertEquals(STORE_NAME, dto.getName());
    }

    // ───────────────────────────────────────────────────────────────── listings
    @Test
    void owner_adds_new_product_to_store() throws Exception {
        StoreDTO dto = storeService.getStore(STORE_NAME);
        String res = storeService.addNewListing(
                FOUNDER, dto.getStoreID(),
                "1", "Tablet", "Android tablet", 5, 899);
        assertEquals("succeed", res);
    }

    // @Test
    // void owner_removes_product_from_store() {
    //     // arrange
    //     storeService.addNewListing(FOUNDER, storeId, "p‑2", "Mouse", "Wireless", 4, 129.9);
    //     String storeID = storeService.getStore(STORE_NAME).getStoreID(); //should return the listing id need to be update by omer and dayan

                               
    //     // act
    //     String res = storeService.removeListing(FOUNDER, storeId, listingId);
    //     // assert
    //     assertEquals("succeed", res);
    // }

    // ───────────────────────────────────────────────────────────────── owners/appointment
    @Test
    void owner_appoints_another_owner() {
        String res = storeService.addAdditionalStoreOwner(FOUNDER, OWNER_A, storeId);
        assertEquals("success", res);
    }

    @Test
    void owner_removes_owner_successfully() {
        storeService.addAdditionalStoreOwner(FOUNDER, OWNER_A, storeId);
        String res = storeService.removeOwner(FOUNDER, OWNER_A, storeId);
        assertEquals("succeed", res);
    }

    // ───────────────────────────────────────────────────────────────── managers & permissions
    @Test
    void owner_sets_purchase_policy_as_permission() {
        storeService.addAdditionalStoreOwner(FOUNDER, OWNER_A, storeId);
        storeService.addNewManager(OWNER_A, MANAGER, storeId);

        int EDIT_PRODUCTS = 1; // suppose permission code 1 is EDIT_PRODUCTS
        String res = storeService.addPermissionToManager(MANAGER, OWNER_A, EDIT_PRODUCTS, storeId);
        assertEquals("success", res);
        assertTrue(storeService.getManagersPermissions(MANAGER, OWNER_A, storeId).contains(EDIT_PRODUCTS));
    }

    // ───────────────────────────────────────────────────────────────── placeholder (no service API yet)




//--------------------------------------------- Negative tests -----------------------------------------------------//
@Test
void create_store_with_duplicate_name_fails() {
    Exception ex = assertThrows(Exception.class, () ->
            storeService.createStore(STORE_NAME, "999"));
    assertTrue(ex.getMessage().contains("already exists"));
}

@Test
void non_owner_cannot_add_listing() { //only one not working
    String res = storeService.addNewListing(
            OWNER_A, storeId, "1", "Keyboard", "Mech", 3, 199.0);
    assertTrue(res.contains("not a owner") || res.contains("doesn't exist"));
}

// @Test
// void manager_without_permission_cannot_remove_listing() {
//     // founder adds listing
//     storeService.addNewListing(FOUNDER, storeId, "p‑4", "Headset", "BT", 2, 299.0);
//     String listingId = storeService.getStore(STORE_NAME)
//                                    .getListingsByProductName("Headset").get(0).getListingId();
//     // owner promotes manager but gives **no** permissions
//     storeService.addAdditionalStoreOwner(FOUNDER, OWNER_A, storeId);
//     storeService.addNewManager(OWNER_A, MANAGER, storeId);

//     String res = storeService.removeListing(MANAGER, storeId, listingId);
//     assertTrue(res.contains("permission"));
// }  //also omer and dayan need to change the create listing to return listing id

@Test
void cannot_remove_founder() {
    String res = storeService.removeOwner(OWNER_A, FOUNDER, storeId);
    assertTrue(res.contains("FOUNDER") || res.contains("not a owner"));
}

@Test
void owner_cannot_add_same_owner_twice() {
    storeService.addAdditionalStoreOwner(FOUNDER, "OWNER_B", storeId);
    String res = storeService.addAdditionalStoreOwner(FOUNDER, "OWNER_B", storeId);
    assertTrue(res.contains("already"));
}
   
}
