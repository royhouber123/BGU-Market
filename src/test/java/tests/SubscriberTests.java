package tests;

import market.model.ShoppingCart;
import support.AcceptanceTestBase;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SubscriberTests extends AcceptanceTestBase {

    @Test
    void subscriber_logs_in_and_cart_is_restored() {
        bridge.register("sub1", "pass", "sub1@email.com", "City");
        bridge.login("sub1", "pass");
        bridge.addProductToCart("TechWorld", "Laptop", 2);
        ShoppingCart beforeLogout = userService.getCurrentUserCart();

        bridge.exitAsGuest(); // Simulate logout
        bridge.login("sub1", "pass"); // Login again

        ShoppingCart restoredCart = userService.getCurrentUserCart();
        assertEquals(beforeLogout.getTotalItems(), restoredCart.getTotalItems());
    }

    @Test
    void subscriber_can_view_personal_purchase_history() {
        bridge.register("sub2", "pass", "sub2@email.com", "City");
        bridge.login("sub2", "pass");
        bridge.addProductToCart("StoreA", "Mouse", 1);
        when(paymentService.process(any(), any(), any())).thenReturn(true);
        when(shipmentService.deliver(any())).thenReturn(true);

        bridge.purchaseCart("City", "4111111111111111", "12/26", "123");

        String history = bridge.viewPurchaseHistory();
        assertTrue(history.contains("Mouse"));
    }

    @Test
    void subscriber_can_rate_purchased_product() {
        bridge.register("sub3", "pass", "sub3@email.com", "City");
        bridge.login("sub3", "pass");
        bridge.addProductToCart("StoreA", "Keyboard", 1);
        when(paymentService.process(any(), any(), any())).thenReturn(true);
        when(shipmentService.deliver(any())).thenReturn(true);

        bridge.purchaseCart("City", "4111111111111111", "12/26", "123");

        String result = bridge.rateProduct("Keyboard", 5);
        assertEquals("Rating submitted", result);
    }

    @Test
    void subscriber_cannot_rate_without_purchase() {
        bridge.register("sub4", "pass", "sub4@email.com", "City");
        bridge.login("sub4", "pass");

        String result = bridge.rateProduct("UnboughtProduct", 4);
        assertEquals("No matching purchase found", result);
    }

    @Test
    void subscriber_sends_message_to_store() {
        bridge.register("sub5", "pass", "sub5@email.com", "City");
        bridge.login("sub5", "pass");

        String result = bridge.sendMessageToStore("StoreA", "Do you have SSDs?");
        assertEquals("Message sent", result);
    }

    @Test
    void subscriber_logs_out_and_cart_preserved() {
        bridge.register("sub6", "pass", "sub6@email.com", "City");
        bridge.login("sub6", "pass");
        bridge.addProductToCart("StoreB", "RAM", 2);

        ShoppingCart beforeLogout = userService.getCurrentUserCart();
        bridge.exitAsGuest();
        bridge.login("sub6", "pass");

        ShoppingCart afterLogin = userService.getCurrentUserCart();
        assertEquals(beforeLogout.getTotalItems(), afterLogin.getTotalItems());
    }
}
