package tests;

import org.junit.jupiter.api.Test;

import support.AcceptanceTestBase;

import static org.junit.jupiter.api.Assertions.*;

public class FounderTests extends AcceptanceTestBase {

    @Test
    void founder_appoints_owner_and_manager_successfully() {
        bridge.register("founder", "pass", "founder@email.com", "City");
        bridge.login("founder", "pass");
        bridge.openStore("EliteStore", "GENERAL");

        bridge.register("newOwner", "pass", "owner@email.com", "City");
        bridge.register("newManager", "pass", "manager@email.com", "City");

        String appointOwnerResult = bridge.appointOwner("EliteStore", "newOwner");
        String appointManagerResult = bridge.appointManager("EliteStore", "newManager");

        assertEquals("Appointment request sent", appointOwnerResult);
        assertEquals("Manager appointed", appointManagerResult);
    }

    @Test
    void founder_cannot_be_removed_from_store() {
        bridge.register("founder", "pass", "founder@email.com", "City");
        bridge.login("founder", "pass");
        bridge.openStore("EliteStore", "GENERAL");

        String result = bridge.removeOwner("EliteStore", "founder");
        assertEquals("Cannot remove founder", result);
    }

    @Test
    void founder_views_appointment_chain() {
        bridge.register("founder", "pass", "founder@email.com", "City");
        bridge.login("founder", "pass");
        bridge.openStore("EliteStore", "GENERAL");

        bridge.register("owner1", "pass", "owner1@email.com", "City");
        bridge.appointOwner("EliteStore", "owner1");

        String chain = bridge.viewAppointmentChain("EliteStore");
        assertTrue(chain.contains("founder -> owner1"));
    }

    @Test
    void founder_attempts_to_transfer_ownership_blocked() {
        bridge.register("founder", "pass", "founder@email.com", "City");
        bridge.login("founder", "pass");
        bridge.openStore("EliteStore", "GENERAL");

        bridge.register("newFounder", "pass", "newfounder@email.com", "City");
        String result = bridge.transferFounderRole("EliteStore", "newFounder");

        assertEquals("Founder role cannot be transferred", result);
    }
}