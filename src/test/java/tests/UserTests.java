package tests;

import org.junit.jupiter.api.Test;

import support.AcceptanceTestBase;

import static org.junit.jupiter.api.Assertions.*;


public class UserTests extends AcceptanceTestBase {

    @Test
    void user_updates_email_successfully() {
        bridge.register("user1", "pass", "user1@email.com", "City");
        bridge.login("user1", "pass");
        String result = bridge.updateUserEmail("user1@email.com", "newuser1@email.com");
        assertEquals("Email updated", result);
    }

    @Test
    void user_changes_password_successfully() {
        bridge.register("user2", "oldpass", "user2@email.com", "City");
        bridge.login("user2", "oldpass");
        String result = bridge.changePassword("oldpass", "newpass");
        assertEquals("Password updated", result);
    }

    @Test
    void user_views_login_activity_log() {
        bridge.register("user3", "pass", "user3@email.com", "City");
        bridge.login("user3", "pass");
        String log = bridge.viewLoginHistory();
        assertTrue(log.contains("login"));
    }

    @Test
    void user_views_saved_payment_and_shipping_info() {
        bridge.register("user4", "pass", "user4@email.com", "City");
        bridge.login("user4", "pass");
        bridge.purchaseCart("City", "4111111111111111", "12/26", "123");
        String info = bridge.getSavedPaymentAndShippingInfo();
        assertTrue(info.contains("4111111111111111"));
        assertTrue(info.contains("City"));
    }

    @Test
    void user_logs_out_and_session_cleared() {
        bridge.register("user5", "pass", "user5@email.com", "City");
        bridge.login("user5", "pass");
        bridge.exitAsGuest();
        assertNull(userService.getCurrentUserCart());
    }
}
