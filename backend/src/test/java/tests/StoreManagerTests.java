package tests;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import market.domain.store.Listing;
import market.domain.store.Store;
import market.domain.store.StoreDTO;
import market.dto.PolicyDTO;
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
        this.storeId = storeService.createStore(STORE_NAME, FOUNDER).storeId();
        StoreDTO dto = storeService.getStore(STORE_NAME);
        assertNotNull(dto, "store should exist after creation");
    }
    
    @Test
    public void manager_addNewProductToStore_positive() {
        // Founder appoints manager
        storeService.addNewManager(FOUNDER, MANAGER, storeId);
        
        // Founder gives the EDIT_PRODUCTS permission to the manager
        storeService.addPermissionToManager(MANAGER, FOUNDER, Store.Permission.EDIT_PRODUCTS.getCode(), storeId);
        try {
            String res = storeService.addNewListing(
                MANAGER, storeId,
                "1", "Monitor", "Electronics", "HD Monitor", 10, 699.0);
            assertNotNull(res);
        } catch (Exception e) {
            fail("Expected no exception, but got: " + e.getMessage(), e);
        }
    }

    
    @Test 
    public void manager_addNewProductToStore_negative_noPermission() {
        try {
            // Founder appoints manager, but gives no permissions
            storeService.addNewManager(FOUNDER, MANAGER, storeId);

            String res = storeService.addNewListing(
                    MANAGER, storeId,
                    "2", "Webcam", "Electronics", "HD Webcam", 3, 199.0);
            fail("Expected an exception due to no permissions, but got: " + res);
        } catch (Exception e) {
            // Expected exception due to no permissions
            assertTrue(e.getMessage().toLowerCase().contains("permission"));
        }
    }



    @Test 
    public void manager_addNewProductToStore_negative_InvalidPrice() {
        // Founder appoints manager and grants permission
        storeService.addNewManager(FOUNDER, MANAGER, storeId);
        storeService.addPermissionToManager(MANAGER, FOUNDER, Store.Permission.EDIT_PRODUCTS.getCode(), storeId);
        try {
            String res = storeService.addNewListing(
                    MANAGER, storeId,
                    "3", "Speaker", "Audio", "Bluetooth Speaker", 5, -99.0);
            fail("Expected an exception due to invalid price, but got: " + res);
        } catch (Exception e) {
            // Expected exception due to invalid price
            assertTrue(e.getMessage().toLowerCase().contains("the price of a products needs to be possitive"));
        }
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
        );
        try{
            storeService.removeListing(MANAGER, storeId, listingId);
        } catch (Exception e) {
            fail("Expected no exception, but got: " + e.getMessage(), e);
        }
        try{
            productService.getListing(listingId);
            fail("Expected an exception since the listing should be removed, but got a listing");
        } catch (Exception e) {
            // Expected exception since the listing should be removed
            assertTrue(e.getMessage().toLowerCase().contains("not found"));
        }

    }

   @Test
    public void manager_removeProductFromStore_negative_noPermission() {
        // Appoint manager without granting permission
        storeService.addNewManager(FOUNDER, MANAGER, storeId);

        // Add a product that manager will attempt to remove
        String listingId = storeService.addNewListing(
                FOUNDER, storeId,
                "rm-2", "Microphone", "Audio", "Studio mic", 4, 179.0
        );
        try {
            storeService.removeListing(MANAGER, storeId, listingId);
            fail("Expected an exception due to no permission, but got success response");
        } catch (Exception e) {
            // Expected exception due to no permission
            assertTrue(e.getMessage().toLowerCase().contains("permission"));
        }
    }

    @Test
    public void manager_removeProductFromStore_alternate_ProductNotFound() {
        // Appoint manager and grant permission
        storeService.addNewManager(FOUNDER, MANAGER, storeId);
        storeService.addPermissionToManager(MANAGER, FOUNDER, Store.Permission.EDIT_PRODUCTS.getCode(), storeId);
        try {
        // Attempt to remove a product that doesn't exist
            storeService.removeListing(MANAGER, storeId, "non-existent-id-xyz");
            fail("Expected an exception since the listing does not exist, but got success response");
        } catch (Exception e) {
            // Expected exception since the listing does not exist
            assertTrue(e.getMessage().toLowerCase().contains("error removing listing"));
        }
    }




    @Test
    public void manager_editProductFromStore_positive() {
        try {
            // Appoint manager and grant EDIT_PRODUCTS permission
            storeService.addNewManager(FOUNDER, MANAGER, storeId);
            storeService.addPermissionToManager(MANAGER, FOUNDER, Store.Permission.EDIT_PRODUCTS.getCode(), storeId);

            // Add a product
            String listingId = storeService.addNewListing(
                    FOUNDER, storeId,
                    "edit‑1", "Camera", "Photography", "DSLR Camera", 2, 1500.0
            );

            // Manager edits price
            boolean res = storeService.editListingPrice(MANAGER, storeId, listingId, 1350.0);
            assertTrue(res);

            // Verify update
            Listing updated = productService.getListing(listingId);
            assertEquals(1350.0, updated.getPrice());
        } catch (Exception e) {
            fail("Expected no exception, but got: " + e.getMessage(), e);
        }
    }

    @Test
    public void manager_editProductFromStore_negative_NoPermission() {
        // Appoint manager without permission
        storeService.addNewManager(FOUNDER, MANAGER, storeId);

        // Add a product
        String listingId = storeService.addNewListing(
                FOUNDER, storeId,
                "edit‑2", "Tripod", "Photography", "Adjustable", 3, 249.0
        );
        try {
            // Manager tries to edit price without permission
            boolean res = storeService.editListingPrice(MANAGER, storeId, listingId, 200.0);
            fail("Expected an exception due to no permission, but got success response: " + res);
        } catch (Exception e) {
            // Expected exception due to no permission
            assertTrue(e.getMessage().toLowerCase().contains("permission"));
        }
    }

    @Test
    public void manager_editProductFromStore_alternate_ProductNotFound() {
        // Appoint manager and grant permission
        storeService.addNewManager(FOUNDER, MANAGER, storeId);
        storeService.addPermissionToManager(MANAGER, FOUNDER, Store.Permission.EDIT_PRODUCTS.getCode(), storeId);

        // Try to edit non-existent listing
        String fakeListingId = "nonexistent-999";
        try {
            boolean res = storeService.editListingPrice(MANAGER, storeId, fakeListingId, 100.0);
            fail("Expected an exception since the listing does not exist, but got success response: " + res);
        } catch (Exception e) {
            // Expected exception since the listing does not exist
            assertTrue(e.getMessage().toLowerCase().contains("not found"));
        }
    }

    @Test
    public void manager_editStorePurchasePolicy_positive() {
        try {
            String storeId = storeService.createStore("PolicyStore", FOUNDER).storeId();
            storeService.addNewManager(FOUNDER, MANAGER, storeId);
            storeService.addPermissionToManager(MANAGER, FOUNDER, Store.Permission.EDIT_POLICIES.getCode(), storeId);
            PolicyDTO.AddPurchasePolicyRequest policy = new PolicyDTO.AddPurchasePolicyRequest("MINITEMS", 2);
            boolean res = storePoliciesService.addPurchasePolicy(storeId, MANAGER, policy);
            assertTrue(res);
        } catch (Exception e) {
            fail("Expected no exception, but got: " + e.getMessage(), e);
        }
    }

    @Test
    public void manager_editStorePurchasePolicy_negative_NoPermission() {
        String storeId = storeService.createStore("PolicyStore", FOUNDER).storeId();
        storeService.addNewManager(FOUNDER, MANAGER, storeId); // No permission granted
        PolicyDTO.AddPurchasePolicyRequest policy = new PolicyDTO.AddPurchasePolicyRequest("MINPRICE", 300);
        try {
            boolean res = storePoliciesService.addPurchasePolicy(storeId, MANAGER, policy);
            fail("Expected an exception due to no permission, but got success response: " + res);
        } catch (Exception e) {
            // Expected exception due to no permission
            assertTrue(e.getMessage().toLowerCase().contains("permission"));
        }
    }


    @Test
    public void manager_editStorePurchasePolicy_alternate_InActiveStore() {
        String storeId = storeService.createStore("PolicyStore", FOUNDER).storeId();
        storeService.addNewManager(FOUNDER, MANAGER, storeId);
        storeService.addPermissionToManager(MANAGER, FOUNDER, Store.Permission.EDIT_POLICIES.getCode(), storeId);
        storeService.closeStore(storeId, FOUNDER);
        PolicyDTO.AddPurchasePolicyRequest policy = new PolicyDTO.AddPurchasePolicyRequest("MAXITEMS", 15);
        try {
            boolean res = storePoliciesService.addPurchasePolicy(storeId, MANAGER, policy);
            fail("Expected an exception since the store is closed, but got success response: " + res);
        } catch (Exception e) {
            // Expected exception since the store is closed
            assertTrue(e.getMessage().toLowerCase().contains("closed"));
        }
    }

    @Test
    public void manager_editStoreDiscountPolicy_positive() {
        try {
            String storeId = storeService.createStore("DiscountStore", FOUNDER).storeId();

            storeService.addNewListing(FOUNDER, storeId, "p1", "Speaker", "Audio", "Bluetooth", 5, 300);

            storeService.addNewManager(FOUNDER, MANAGER, storeId);
            storeService.addPermissionToManager(MANAGER, FOUNDER, Store.Permission.EDIT_POLICIES.getCode(), storeId);

            PolicyDTO.AddDiscountRequest dto = new PolicyDTO.AddDiscountRequest(
                "PERCENTAGE", "PRODUCT", "p1", 0.25, null, null, List.of(), "SUM"
            );

            boolean res = storePoliciesService.addDiscount(storeId, MANAGER, dto);
            assertTrue(res, "Expected discount to be added successfully");
        } catch (Exception e) {
            fail("Expected no exception, but got: " + e.getMessage(), e);
        }

    }

    @Test
    public void manager_editStoreDiscountPolicy_negative_NoPermission() {
        String storeId = storeService.createStore("DiscountStore", FOUNDER).storeId();
        storeService.addNewListing(FOUNDER, storeId, "p1", "Camera", "Electronics", "DSLR", 3, 2500);

        storeService.addNewManager(FOUNDER, MANAGER, storeId); // No permission granted

        PolicyDTO.AddDiscountRequest dto = new PolicyDTO.AddDiscountRequest(
            "PERCENTAGE", "PRODUCT", "p1", 0.1, null, null, List.of(), "SUM"
        );
        try {
            boolean res = storePoliciesService.addDiscount(storeId, MANAGER, dto);
            fail("Expected an exception due to no permission, but got success response: " + res);
        } catch (Exception e) {
            // Expected exception due to no permission
            assertTrue(e.getMessage().toLowerCase().contains("permission"));
        }
    }

    @Test
    public void manager_editStoreDiscountPolicy_alternate_InValidObjectToCreatePolicyTo() {
        try {
            String storeId = storeService.createStore("DiscountStore", FOUNDER).storeId();
            storeService.addNewListing(FOUNDER, storeId, "p1", "Monitor", "Electronics", "4K Monitor", 7, 1200);

            storeService.addNewManager(FOUNDER, MANAGER, storeId);
            storeService.addPermissionToManager(MANAGER, FOUNDER, Store.Permission.EDIT_POLICIES.getCode(), storeId);

            PolicyDTO.AddDiscountRequest invalid = new PolicyDTO.AddDiscountRequest(
                null, "PRODUCT", "p1", -0.4, null, null, List.of(), "SUM" // ❌ invalid
            );

            boolean res = storePoliciesService.addDiscount(storeId, MANAGER, invalid);
            fail("Expected an exception due to invalid discount policy, but got success response: " + res);
        } catch (Exception e) {
            // Expected exception due to invalid discount policy
            assertTrue(e.getMessage().toLowerCase().contains("null"));
        }
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
