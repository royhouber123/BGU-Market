package tests;

import org.junit.jupiter.api.Test;

import support.AcceptanceTestBase;

import static org.junit.jupiter.api.Assertions.*;

public class AdminTests extends AcceptanceTestBase {

    @Test
    void admin_force_closes_store_successfully() {
        bridge.openStore("TempStore", "GENERAL");
        String result = bridge.forceCloseStore("TempStore");
        assertEquals("Store closed by admin", result);
    }

    @Test
    void admin_views_system_logs() {
        String logs = bridge.viewSystemLogs();
        assertTrue(logs.contains("activity"));
    }

    @Test
    void admin_reviews_user_report() {
        String result = bridge.reviewReport(101);
        assertEquals("Report reviewed", result);
    }

    @Test
    void admin_generates_summary_report() {
        String report = bridge.generateSystemReport();
        assertTrue(report.contains("Revenue"));
    }

    @Test
    void admin_locks_user_account() {
        bridge.register("baduser", "pass", "baduser@email.com", "City");
        String result = bridge.lockUserAccount("baduser");
        assertEquals("User account locked", result);
    }

    @Test
    void admin_unlocks_user_account() {
        bridge.lockUserAccount("baduser");
        String result = bridge.unlockUserAccount("baduser");
        assertEquals("User account unlocked", result);
    }

    @Test
    void admin_deletes_user_account() {
        bridge.register("tobedeleted", "pass", "del@email.com", "City");
        String result = bridge.deleteUserAccount("tobedeleted");
        assertEquals("User account deleted", result);
    }
}