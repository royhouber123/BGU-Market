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
        storeId = storeService.createStore(storeName, founderID).getData();
    }

    @Test
    void founder_appoints_owner_successfully() {
        // Appoint ownerA as owner
        assertTrue(storeService.addAdditionalStoreOwner(founderID, ownerA,storeId).isSuccess());
        assertTrue(storeService.isOwner(storeId, ownerA).getData());
    }

    @Test
    void founder_appoints_manager_successfully() {
        // Appoint ownerB as manager
        assertTrue(storeService.addNewManager(founderID,  ownerB,storeId).isSuccess());
        assertTrue(storeService.isManager(storeId, ownerB).getData());
    }

    @Test
    void founder_cannot_be_removed_from_store() {
        ApiResponse<List<List<String>>> result = storeService.removeOwner(founderID, founderID, storeId);
        assertFalse(result.isSuccess());
        System.out.println(result.getError());
        assertTrue(result.getError().contains("founder"));
    }


    @Test
    void founder_views_appointment_chain() {
        // Build hierarchy: founder -> ownerA -> ownerB -> ownerC
        storeService.addAdditionalStoreOwner(founderID, ownerA,storeId);
        storeService.addAdditionalStoreOwner(ownerA,ownerB, storeId);
        storeService.addAdditionalStoreOwner(ownerB, ownerC,storeId);

        ApiResponse<List<List<String>>> removedOwners = storeService.removeOwner(founderID, ownerA, storeId);
        // Map<String, List<String>> appointments = storeService.getAppointmentHierarchy(storeId);
        assertTrue(removedOwners.isSuccess());
        assertNotNull(removedOwners, "Error got Null");
        assertTrue(removedOwners.getData().get(0).contains(ownerB), "ownerA should have appointed ownerB");
        assertTrue(removedOwners.getData().get(0).contains(ownerC), "ownerA should have appointed ownerB which appointed ownerC");

    }

    @Test
    void founder_attempts_to_transfer_ownership_blocked() {
        ApiResponse<Void> result = storeService.addAdditionalStoreOwner(founderID, founderID, storeId);
        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("founder") || result.getError().contains("already an owner"));
    }

    @Test
    void founder_closes_store_success() {
        assertTrue(storeService.getStore(storeName).getData().isActive());
        ApiResponse<String> result = storeService.closeStore(storeId, founderID);
        assertTrue(result.isSuccess());
        assertFalse(storeService.getStore(storeName).getData().isActive());
        assertEquals(result.getData(), storeId);
    }

    @Test
    void founder_closes_store_fail_user_is_not_founder() {
        assertTrue(storeService.getStore(storeName).getData().isActive());
        ApiResponse<String> result = storeService.closeStore(storeId, ownerA);
        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("founder"));
        assertTrue(storeService.getStore(storeName).getData().isActive());
    }

    @Test
    void founder_closes_store_fail_store_is_already_inactive() {
        ApiResponse<String> result1 = storeService.closeStore(storeId, founderID);
        assertTrue(result1.isSuccess());
        assertEquals(result1.getData(), storeId);
        ApiResponse<String> result2 = storeService.closeStore(storeId, founderID);
        assertFalse(result2.isSuccess());
        assertTrue(result2.getError().contains("already closed"));
    }

}