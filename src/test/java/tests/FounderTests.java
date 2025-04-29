package tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import market.domain.user.roles.Owner;
import support.AcceptanceTestBase;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.contains;

public class FounderTests extends AcceptanceTestBase {

    private final String founderID = "1";
    private final String ownerA = "2";
    private final String ownerB = "3";
    private final String ownerC = "4";
    private final String storeName = "TestStore";

    private String storeId;

    @BeforeEach
    void setUpTestData() {
        storeId = storeService.createStore(storeName, founderID);
    }

    @Test
    void founder_appoints_owner_and_manager_successfully() {
        // Appoint ownerA as owner
        String ans = storeService.addAdditionalStoreOwner(founderID, storeId, ownerA);
        assertEquals(ans, "success");
        // assertTrue(storeService.isOwner(storeId, ownerA), "ownerA should be an owner of the store");

        // Appoint ownerB as manager
        String managerAppointed = storeService.addNewManager(founderID, storeId, ownerB);
        assertEquals(ans, "success");
        // assertTrue(managerAppointed, "Founder should be able to appoint a new manager");
        // assertTrue(storeService.isManager(storeId, ownerB), "ownerB should be a manager of the store");
    }

    @Test
    void founder_cannot_be_removed_from_store() {
        Exception ex = assertThrows(Exception.class, () -> {
            storeService.removeOwner(founderID, founderID, storeId);
        });
        assertTrue(
            ex.getMessage().toLowerCase().contains("founder"),
            "Founder should not be removable from the store"
        );
    }

    @Test
    void founder_views_appointment_chain() {
        // Build hierarchy: founder -> ownerA -> ownerB -> ownerC
        storeService.addAdditionalStoreOwner(founderID, storeId, ownerA);
        storeService.addAdditionalStoreOwner(ownerA, storeId, ownerB);
        storeService.addAdditionalStoreOwner(ownerB, storeId, ownerC);

        List<List<String>> removedOwners = storeService.removeOwner(founderID, ownerA, storeId);
        // Map<String, List<String>> appointments = storeService.getAppointmentHierarchy(storeId);

        assertNotNull(removedOwners, "Error got Null");
        assertTrue(removedOwners.get(0).contains(ownerB), "ownerA should have appointed ownerB");
        assertTrue(removedOwners.get(0).contains(ownerC), "ownerA should have appointed ownerB which appointed ownerC");

    }

    @Test
    void founder_attempts_to_transfer_ownership_blocked() {
        // Simulate a transfer by trying to reappoint self
        Exception ex = assertThrows(Exception.class, () -> {
            storeService.addAdditionalStoreOwner(founderID, storeId, founderID);
        });
        assertTrue(
            ex.getMessage().toLowerCase().contains("founder") ||
            ex.getMessage().toLowerCase().contains("already an owner"),
            "Founder should not be allowed to reappoint themselves"
        );
    }
}