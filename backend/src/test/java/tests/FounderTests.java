package tests;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import support.AcceptanceTestBase;
import utils.ApiResponse;

public class FounderTests extends AcceptanceTestBase {

    private final String founderID = "1";
    private final String ownerA = "2";
    private final String ownerB = "3";
    private final String ownerC = "4";
    private final String storeName = "TestStore";

    private String storeId;

    @BeforeEach
    void setUpTestData() throws Exception {
        // Register test users first
        userService.register(founderID, "password");
        userService.register(ownerA, "password");
        userService.register(ownerB, "password");
        userService.register(ownerC, "password");
        
        storeId = storeService.createStore(storeName, founderID).storeId();
    }

    @Test
    void founder_appoints_owner_successfully() {
        // Appoint ownerA as owner
        try {
            storeService.addAdditionalStoreOwner(founderID, ownerA, storeId);
        } catch (Exception e) {
            fail("Expected no exception, but got: " + e.getMessage(), e);
        }
        assertTrue(storeService.isOwner(storeId, ownerA));
    }

    @Test
    void founder_appoints_manager_successfully() {
        // Appoint ownerB as manager
        try {
            storeService.addNewManager(founderID,  ownerB,storeId);
        } catch (Exception e) {
            fail("Expected no exception, but got: " + e.getMessage(), e);
        }
        assertTrue(storeService.isManager(storeId, ownerB));
    }

    @Test
    void founder_cannot_be_removed_from_store() {
        try {
            storeService.removeOwner(founderID, founderID, storeId);
            fail("Expected an exception to be thrown when removing the founder.");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("founder"), "Error message should mention 'founder'.");
        }
    }


    @Test
    void founder_views_appointment_chain() {
        // Build hierarchy: founder -> ownerA -> ownerB -> ownerC
        storeService.addAdditionalStoreOwner(founderID, ownerA,storeId);
        storeService.addAdditionalStoreOwner(ownerA,ownerB, storeId);
        storeService.addAdditionalStoreOwner(ownerB, ownerC,storeId);
        try {
            List<List<String>> removedOwners = storeService.removeOwner(founderID, ownerA, storeId);
            // Map<String, List<String>> appointments = storeService.getAppointmentHierarchy(storeId);
            assertNotNull(removedOwners, "Error got Null");
            assertTrue(removedOwners.get(0).contains(ownerB), "ownerA should have appointed ownerB");
            assertTrue(removedOwners.get(0).contains(ownerC), "ownerA should have appointed ownerB which appointed ownerC");
        } catch (Exception e) {
            fail("Expected no exception, but got: " + e.getMessage(), e);
        }

    }

    @Test
    void founder_attempts_to_transfer_ownership_blocked() {
        try {
            storeService.addAdditionalStoreOwner(founderID, founderID, storeId);
            fail("Expected an exception to be thrown when trying to transfer ownership to the founder.");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("founder") || e.getMessage().contains("already an owner"),
                    "Error message should mention 'founder' or 'already an owner'.");
        }
    }

    @Test
    void founder_closes_store_success() {
        assertTrue(storeService.getStore(storeName).isActive());
        try{
            String result = storeService.closeStore(storeId, founderID);
            assertFalse(storeService.getStore(storeName).isActive());
            assertEquals(result, storeId);
        } catch (Exception e) {
            fail("Expected no exception, but got: " + e.getMessage(), e);
        }
    }

    @Test
    void founder_closes_store_fail_user_is_not_founder() {
        assertTrue(storeService.getStore(storeName).isActive());
        try {
            storeService.closeStore(storeId, ownerA);
            fail("Expected an exception to be thrown when a non-founder tries to close the store.");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("founder"), "Error message should mention 'founder'.");
            assertTrue(storeService.getStore(storeName).isActive());

        }
    }

    @Test
    void founder_closes_store_fail_store_is_already_inactive() {
        try {
            String result1 = storeService.closeStore(storeId, founderID);
            assertEquals(result1, storeId);
        } catch (Exception e) {
            fail("Expected no exception, but got: " + e.getMessage(), e);
        }
        try {
            storeService.closeStore(storeId, founderID);
            fail("Expected an exception to be thrown when trying to close an already closed store.");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("already closed"), "Error message should mention 'already closed'.");
        }
    }

}