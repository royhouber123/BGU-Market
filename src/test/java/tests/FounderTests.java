package tests;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import support.AcceptanceTestBase;

public class FounderTests extends AcceptanceTestBase {

    private final String founderID = "1";
    private final String ownerA = "2";
    private final String ownerB = "3";
    private final String ownerC = "4";
    private final String storeName = "TestStore";

    private String storeId;

    @BeforeEach
    void setUpTestData() throws Exception {
        storeId = storeService.createStore(storeName, founderID);
    }

    @Test
    void founder_appoints_owner_and_manager_successfully() {
        // Appoint ownerA as owner
        String ans = storeService.addAdditionalStoreOwner(founderID, ownerA,storeId);
        assertEquals(ans, "success");
        // assertTrue(storeService.isOwner(storeId, ownerA), "ownerA should be an owner of the store");

        // Appoint ownerB as manager
        String managerAppointed = storeService.addNewManager(founderID,  ownerB,storeId);
        assertEquals(ans, "success");
        // assertTrue(managerAppointed, "Founder should be able to appoint a new manager");
        // assertTrue(storeService.isManager(storeId, ownerB), "ownerB should be a manager of the store");
    }

    @Test
    void founder_cannot_be_removed_from_store() {
        List<List<String>> result = storeService.removeOwner(founderID, founderID, storeId);

        // Check that an error message was returned
        assertFalse(result.get(0).isEmpty(), "Expected an error message when attempting to remove the founder");

        String errorMessage = result.get(0).get(0).toLowerCase();
        assertTrue(
            errorMessage.contains("founder") || errorMessage.contains("cannot be removed"),
            "Expected error message to mention that the founder cannot be removed"
        );
    }


    @Test
    void founder_views_appointment_chain() {
        // Build hierarchy: founder -> ownerA -> ownerB -> ownerC
        storeService.addAdditionalStoreOwner(founderID, ownerA,storeId);
        storeService.addAdditionalStoreOwner(ownerA,ownerB, storeId);
        storeService.addAdditionalStoreOwner(ownerB, ownerC,storeId);

        List<List<String>> removedOwners = storeService.removeOwner(founderID, ownerA, storeId);
        // Map<String, List<String>> appointments = storeService.getAppointmentHierarchy(storeId);

        assertNotNull(removedOwners, "Error got Null");
        assertTrue(removedOwners.get(0).contains(ownerB), "ownerA should have appointed ownerB");
        assertTrue(removedOwners.get(0).contains(ownerC), "ownerA should have appointed ownerB which appointed ownerC");

    }

    @Test
    void founder_attempts_to_transfer_ownership_blocked() {
        String result = storeService.addAdditionalStoreOwner(founderID, founderID, storeId);

        assertNotEquals("success", result, "Expected failure when founder attempts to reappoint themselves as owner");

        String message = result.toLowerCase();
        assertTrue(
            message.contains("founder") || message.contains("already an owner"),
            "Expected error message to mention founder restriction or duplicate ownership"
        );
    }

    @Test
    void founder_closes_store_success() {
        assertTrue(storeService.getStore(storeName).isActive());
        String result = storeService.closeStore(storeId, founderID);
        assertEquals("success", result);
        assertFalse(storeService.getStore(storeName).isActive());
    }

    @Test
    void founder_closes_store_fail_user_is_not_founder() {
        assertTrue(storeService.getStore(storeName).isActive());
        String result = storeService.closeStore(storeId, ownerA);
        assertNotEquals("success", result, "Expected to fail because user is not the store's founder");
        assertTrue(storeService.getStore(storeName).isActive());
    }

    @Test
    void founder_closes_store_fail_store_is_already_inactive() {
        String result1 = storeService.closeStore(storeId, founderID);
        assertEquals("success", result1);
        String result2 = storeService.closeStore(storeId, founderID);
        assertNotEquals("success", result2, "Expected to fail because store is already closed");
    }

}