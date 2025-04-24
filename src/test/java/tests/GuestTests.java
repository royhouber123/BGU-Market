package tests;

import market.model.ShoppingCart;
import support.AcceptanceTestBase;
import support.Bridge;
import market.model.Product;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GuestTests extends AcceptanceTestBase {

    @Test
    void guest_enters_system_initializes_cart() {
        bridge.enterAsGuest();
        ShoppingCart cart = userService.getGuestCart();
        assertNotNull(cart);
        assertTrue(cart.isEmpty());
    }

    @Test
    void guest_exits_system_cart_deleted() {
        bridge.enterAsGuest();
        bridge.exitAsGuest();
        ShoppingCart cart = userService.getGuestCart();
        assertNull(cart); // Cart should be removed on exit
    }

    @Test
    void guest_registers_with_valid_details() {
        String result = bridge.register("guest1", "pass123", "guest1@email.com", "Tel Aviv");
        assertEquals("Registration successful", result);
        assertTrue(userService.userExists("guest1"));
    }

    @Test
    void guest_registers_with_invalid_email() {
        String result = bridge.register("guest1", "pass123", "bademail", "Tel Aviv");
        assertEquals("Invalid email", result);
        assertFalse(userService.userExists("guest1"));
    }

    @Test
    void guest_registration_interrupted_before_submit() {
        assertFalse(userService.userExists("guest1"));
    }

    @Test
    void guest_login_with_valid_credentials() {
        userService.register("guest1", "pass123", "guest1@email.com", "Tel Aviv");
        String result = bridge.login("guest1", "pass123");
        assertEquals("Login successful", result);
    }

    @Test
    void guest_login_with_wrong_password() {
        userService.register("guest1", "pass123", "guest1@email.com", "Tel Aviv");
        String result = bridge.login("guest1", "wrongpass");
        assertEquals("Incorrect password", result);
    }

    @Test
    void guest_gets_store_and_product_info_when_available() {
        storeService.addStore("TechWorld");
        storeService.addProductToStore("TechWorld", "Laptop", 999.99);
        String result = bridge.getStoreAndProductInfo();
        assertEquals("List of stores and products", result);
    }

    @Test
    void guest_gets_store_info_when_no_stores_available() {
        storeService.clearStores();
        String result = bridge.getStoreAndProductInfo();
        assertEquals("No stores available at the moment.", result);
    }

    @Test
    void guest_gets_store_info_when_store_has_no_products() {
        storeService.addStore("EmptyStore");
        String result = bridge.getStoreAndProductInfo();
        assertEquals("No products available for this store.", result);
    }

    @Test
    void guest_can_search_all_stores_by_keyword() {
        storeService.addStore("TechWorld");
        storeService.addProductToStore("TechWorld", "Laptop", 999.99);
        List<Product> results = bridge.searchProductsGlobally("Laptop");
        assertEquals(1, results.size());
        assertEquals("Laptop", results.get(0).getName());
    }

    @Test
    void guest_search_returns_empty_when_no_matches() {
        storeService.clearStores();
        List<Product> results = bridge.searchProductsGlobally("Smartphone");
        assertTrue(results.isEmpty());
    }

    @Test
    void guest_searches_in_specific_store() {
        storeService.addStore("BookStore");
        storeService.addProductToStore("BookStore", "Data Structures Book", 79.90);
        List<Product> results = bridge.searchProductsInStore("BookStore", "Data Structures");
        assertEquals(1, results.size());
        assertEquals("Data Structures Book", results.get(0).getName());
    }

    @Test
    void guest_adds_product_to_cart() {
        storeService.addStore("GameStore");
        storeService.addProductToStore("GameStore", "Controller", 199.99);
        bridge.searchProductsInStore("GameStore", "Controller");
        bridge.addProductToCart("GameStore", "Controller", 2);

        ShoppingCart cart = userService.getGuestCart();
        assertNotNull(cart);
        assertEquals(1, cart.getStoreBags().size());
        assertEquals(2, cart.getStoreBags().get(0).getQuantity("Controller"));
    }

    @Test
    void guest_updates_cart_quantity_and_removes_item() {
        bridge.addProductToCart("TechWorld", "Laptop", 1);
        bridge.updateCartItem("TechWorld", "Laptop", 3);
        bridge.updateCartItem("TechWorld", "Laptop", 0); // this should remove it

        ShoppingCart cart = userService.getGuestCart();
        assertTrue(cart.getStoreBags().isEmpty());
    }

    @Test
    void guest_purchases_cart_successfully() {
        storeService.addStore("SuperStore");
        storeService.addProductToStore("SuperStore", "Mouse", 49.99);
        bridge.addProductToCart("SuperStore", "Mouse", 1);

        when(paymentService.process("4111111111111111", "12/26", "123")).thenReturn(true);
        when(shipmentService.deliver("Beer Sheva")).thenReturn(true);

        String result = bridge.purchaseCart("Beer Sheva", "4111111111111111", "12/26", "123");
        assertEquals("Purchase completed", result);

        ShoppingCart cart = userService.getGuestCart();
        assertTrue(cart.isEmpty());
    }

    @Test
    void guest_purchasing_cart_fails_due_to_stock() {
        storeService.addStore("LowStockStore");
        storeService.addProductToStore("LowStockStore", "SSD", 299.99, 0); // no stock
        bridge.addProductToCart("LowStockStore", "SSD", 1);

        when(paymentService.process("4111111111111111", "12/26", "123")).thenReturn(true);
        when(shipmentService.deliver("Beer Sheva")).thenReturn(true);

        String result = bridge.purchaseCart("Beer Sheva", "4111111111111111", "12/26", "123");
        assertEquals("Product unavailable", result);
    }

    @Test
    void guest_purchasing_cart_fails_due_to_payment() {
        storeService.addStore("PaymentFailStore");
        storeService.addProductToStore("PaymentFailStore", "Monitor", 699.99);
        bridge.addProductToCart("PaymentFailStore", "Monitor", 1);

        when(paymentService.process("invalid", "12/26", "123")).thenReturn(false);
        when(shipmentService.deliver("Beer Sheva")).thenReturn(true);

        String result = bridge.purchaseCart("Beer Sheva", "invalid", "12/26", "123");
        assertEquals("Payment failed", result);
    }
}  