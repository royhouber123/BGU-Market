package tests;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import market.domain.store.Listing;
import market.domain.store.Store;
import market.domain.store.StoreDTO;
import market.dto.AddDiscountDTO;
import market.dto.AddPurchasePolicyDTO;
import support.AcceptanceTestBase;
import utils.ApiResponse;


public class StoreManagerTests extends AcceptanceTestBase {
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
    
    @Test
    public void manager_addNewProductToStore_positive() {
        // Founder appoints manager
        storeService.addNewManager(FOUNDER, MANAGER, storeId);
        
        // Founder gives the EDIT_PRODUCTS permission to the manager
        storeService.addPermissionToManager(MANAGER, FOUNDER, Store.Permission.EDIT_PRODUCTS.getCode(), storeId);

        ApiResponse<String> res = storeService.addNewListing(
                MANAGER, storeId,
                "1", "Monitor", "Electronics", "HD Monitor", 10, 699.0);

        assertTrue(res.isSuccess());
        assertNotNull(res.getData());
    }

    
    @Test 
    public void manager_addNewProductToStore_negative_noPermission() {
        // Founder appoints manager, but gives no permissions
    storeService.addNewManager(FOUNDER, MANAGER, storeId);

    ApiResponse<String> res = storeService.addNewListing(
            MANAGER, storeId,
            "2", "Webcam", "Electronics", "HD Webcam", 3, 199.0);

    assertFalse(res.isSuccess());
    assertTrue(res.getError().toLowerCase().contains("permission"));
    }



    @Test 
    public void manager_addNewProductToStore_negative_InvalidPrice() {
        // Founder appoints manager and grants permission
    storeService.addNewManager(FOUNDER, MANAGER, storeId);
    storeService.addPermissionToManager(MANAGER, FOUNDER, Store.Permission.EDIT_PRODUCTS.getCode(), storeId);

    ApiResponse<String> res = storeService.addNewListing(
            MANAGER, storeId,
            "3", "Speaker", "Audio", "Bluetooth Speaker", 5, -99.0);

    assertFalse(res.isSuccess());
    assertTrue(res.getError().toLowerCase().contains("possitive"));
    }

    @Test
    public void manager_removeProductFromStore_positive() {
        // Appoint manager and grant EDIT_PRODUCTS permission
        storeService.addNewManager(FOUNDER, MANAGER, storeId);
        storeService.addPermissionToManager(MANAGER, FOUNDER, Store.Permission.EDIT_PRODUCTS.getCode(), storeId);

        // Add a product to remove
        String listingId = storeService.addNewListing(
                FOUNDER, storeId,
                "rm-1", "Headphones", "Audio", "Noise cancelling", 7, 299.0
        ).getData();

        ApiResponse res = storeService.removeListing(MANAGER, storeId, listingId);
        assertTrue(res.isSuccess());

        ApiResponse<Listing> lookup = productService.getListing(listingId);
        assertFalse(lookup.isSuccess());
    }




   @Test
    public void manager_removeProductFromStore_negative_noPermission() {
        // Appoint manager without granting permission
        storeService.addNewManager(FOUNDER, MANAGER, storeId);

        // Add a product that manager will attempt to remove
        String listingId = storeService.addNewListing(
                FOUNDER, storeId,
                "rm-2", "Microphone", "Audio", "Studio mic", 4, 179.0
        ).getData();

        ApiResponse res = storeService.removeListing(MANAGER, storeId, listingId);
        assertFalse(res.isSuccess());
        assertTrue(res.getError().toLowerCase().contains("permission"));
    }



    @Test
    public void manager_removeProductFromStore_alternate_ProductNotFound() {
        // Appoint manager and grant permission
        storeService.addNewManager(FOUNDER, MANAGER, storeId);
        storeService.addPermissionToManager(MANAGER, FOUNDER, Store.Permission.EDIT_PRODUCTS.getCode(), storeId);

        // Attempt to remove a product that doesn't exist
        ApiResponse res = storeService.removeListing(MANAGER, storeId, "non-existent-id-xyz");
        assertFalse(res.isSuccess());
        assertTrue(res.getError().toLowerCase().contains("error removing listing"));
    }




    @Test
    public void manager_editProductFromStore_positive() {
        // Appoint manager and grant EDIT_PRODUCTS permission
        storeService.addNewManager(FOUNDER, MANAGER, storeId);
        storeService.addPermissionToManager(MANAGER, FOUNDER, Store.Permission.EDIT_PRODUCTS.getCode(), storeId);

        // Add a product
        String listingId = storeService.addNewListing(
                FOUNDER, storeId,
                "edit‑1", "Camera", "Photography", "DSLR Camera", 2, 1500.0
        ).getData();

        // Manager edits price
        ApiResponse<Boolean> res = storeService.editListingPrice(MANAGER, storeId, listingId, 1350.0);
        assertTrue(res.isSuccess());

        // Verify update
        ApiResponse<Listing> updated = productService.getListing(listingId);
        assertTrue(updated.isSuccess());
        assertEquals(1350.0, updated.getData().getPrice());
    }

    @Test
    public void manager_editProductFromStore_negative_NoPermission() {
        // Appoint manager without permission
        storeService.addNewManager(FOUNDER, MANAGER, storeId);

        // Add a product
        String listingId = storeService.addNewListing(
                FOUNDER, storeId,
                "edit‑2", "Tripod", "Photography", "Adjustable", 3, 249.0
        ).getData();

        // Manager tries to edit price without permission
        ApiResponse<Boolean> res = storeService.editListingPrice(MANAGER, storeId, listingId, 200.0);
        assertFalse(res.isSuccess());
        assertTrue(res.getError().toLowerCase().contains("permission"));
    }

    @Test
    public void manager_editProductFromStore_alternate_ProductNotFound() {
        // Appoint manager and grant permission
        storeService.addNewManager(FOUNDER, MANAGER, storeId);
        storeService.addPermissionToManager(MANAGER, FOUNDER, Store.Permission.EDIT_PRODUCTS.getCode(), storeId);

        // Try to edit non-existent listing
        String fakeListingId = "nonexistent-999";
        ApiResponse<Boolean> res = storeService.editListingPrice(MANAGER, storeId, fakeListingId, 100.0);
        assertFalse(res.isSuccess());
        assertTrue(res.getError().toLowerCase().contains("not found"));
    }

    @Test
    public void manager_editStorePurchasePolicy_positive() {
        String storeId = storeService.createStore("PolicyStore", FOUNDER).getData();

        storeService.addNewManager(FOUNDER, MANAGER, storeId);
        storeService.addPermissionToManager(MANAGER, FOUNDER, Store.Permission.EDIT_POLICIES.getCode(), storeId);

        AddPurchasePolicyDTO policy = new AddPurchasePolicyDTO("MINITEMS", 2);

        ApiResponse<Boolean> res = storePoliciesService.addPurchasePolicy(storeId, MANAGER, policy);
        assertTrue(res.isSuccess());
        assertTrue(res.getData());
    }



    @Test
    public void manager_editStorePurchasePolicy_negative_NoPermission() {
        String storeId = storeService.createStore("PolicyStore", FOUNDER).getData();

        storeService.addNewManager(FOUNDER, MANAGER, storeId); // No permission granted

        AddPurchasePolicyDTO policy = new AddPurchasePolicyDTO("MINPRICE", 300);

        ApiResponse<Boolean> res = storePoliciesService.addPurchasePolicy(storeId, MANAGER, policy);
        assertFalse(res.isSuccess());
        assertTrue(res.getError().toLowerCase().contains("permission"));
    }


    @Test
    public void manager_editStorePurchasePolicy_alternate_InActiveStore() {
        String storeId = storeService.createStore("PolicyStore", FOUNDER).getData();

        storeService.addNewManager(FOUNDER, MANAGER, storeId);
        storeService.addPermissionToManager(MANAGER, FOUNDER, Store.Permission.EDIT_POLICIES.getCode(), storeId);

        storeService.closeStore(storeId, FOUNDER);

        AddPurchasePolicyDTO policy = new AddPurchasePolicyDTO("MAXITEMS", 15);

        ApiResponse<Boolean> res = storePoliciesService.addPurchasePolicy(storeId, MANAGER, policy);
        assertFalse(res.isSuccess());
        assertTrue(res.getError().toLowerCase().contains("closed"));
    }




    @Test
    public void manager_editStoreDiscountPolicy_positive() {
        String storeId = storeService.createStore("DiscountStore", FOUNDER).getData();

        storeService.addNewListing(FOUNDER, storeId, "p1", "Speaker", "Audio", "Bluetooth", 5, 300);

        storeService.addNewManager(FOUNDER, MANAGER, storeId);
        storeService.addPermissionToManager(MANAGER, FOUNDER, Store.Permission.EDIT_POLICIES.getCode(), storeId);

        AddDiscountDTO dto = new AddDiscountDTO(
            "PERCENTAGE", "PRODUCT", "p1", 0.25, null, null, List.of(), "SUM"
        );

        ApiResponse<Boolean> res = storePoliciesService.addDiscount(storeId, MANAGER, dto);
        assertTrue(res.isSuccess());
        assertTrue(res.getData());
    }

    @Test
    public void manager_editStoreDiscountPolicy_negative_NoPermission() {
        String storeId = storeService.createStore("DiscountStore", FOUNDER).getData();
        storeService.addNewListing(FOUNDER, storeId, "p1", "Camera", "Electronics", "DSLR", 3, 2500);

        storeService.addNewManager(FOUNDER, MANAGER, storeId); // No permission granted

        AddDiscountDTO dto = new AddDiscountDTO(
            "PERCENTAGE", "PRODUCT", "p1", 0.1, null, null, List.of(), "SUM"
        );

        ApiResponse<Boolean> res = storePoliciesService.addDiscount(storeId, MANAGER, dto);
        assertFalse(res.isSuccess());
        assertTrue(res.getError().toLowerCase().contains("permission"));
    }



    @Test
    public void manager_editStoreDiscountPolicy_alternate_InValidObjectToCreatePolicyTo() {
        String storeId = storeService.createStore("DiscountStore", FOUNDER).getData();
        storeService.addNewListing(FOUNDER, storeId, "p1", "Monitor", "Electronics", "4K Monitor", 7, 1200);

        storeService.addNewManager(FOUNDER, MANAGER, storeId);
        storeService.addPermissionToManager(MANAGER, FOUNDER, Store.Permission.EDIT_POLICIES.getCode(), storeId);

        AddDiscountDTO invalid = new AddDiscountDTO(
            null, "PRODUCT", "p1", -0.4, null, null, List.of(), "SUM" // ❌ invalid
        );

        ApiResponse<Boolean> res = storePoliciesService.addDiscount(storeId, MANAGER, invalid);
        assertFalse(res.isSuccess());
        assertTrue(res.getError().toLowerCase().contains("null")); // Adjust as needed
    }

    

    @Test public void manager_requestStoreRoles_positive() {}
    @Test public void manager_requestStoreRoles_negative() {}
    @Test public void manager_requestStoreRoles_alternate() {}

    @Test public void manager_respondToUserMessages_positive() {}
    @Test public void manager_respondToUserMessages_negative() {}
    @Test public void manager_respondToUserMessages_alternate() {}

    @Test public void manager_viewStorePurchaseHistory_positive() {}
    @Test public void manager_viewStorePurchaseHistory_negative() {}
    @Test public void manager_viewStorePurchaseHistory_alternate() {}





}
