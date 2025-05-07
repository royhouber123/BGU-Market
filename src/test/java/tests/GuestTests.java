package tests;

import support.AcceptanceTestBase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import market.domain.purchase.Purchase;
import market.domain.user.ShoppingCart;
import market.domain.user.User;
import market.middleware.TokenUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class GuestTests extends AcceptanceTestBase {

    private static final String GUEST = "guest";
    private static final String MANAGER = "manager";
    private static final String PW = "1234";
    private String storeId;
    private String productId = "p1";
    private String productName = "Notebook";
    private String productDescription = "Simple notebook";
    private int quantity = 5;
    private double price = 25.0;


    @BeforeEach
    void setUp() throws Exception {
        userService.getUserRepository().register(MANAGER, PW);
        storeId = storeService.createStore("SchoolStore", MANAGER);
        storeService.addNewListing(
            MANAGER,
            storeId,
            productId,
            productName,
            productDescription,
            quantity,
            price
        );
        userService.getUserRepository().register(GUEST, PW); //need to replace in future to registerGuest
        when(paymentService.processPayment(anyString())).thenReturn(true);
        when(shipmentService.ship(anyString(), anyString(), anyDouble())).thenReturn("SHIP123");
    }


    @Test
    void guest_enters_system_initializes_cart() {
        ///
    }

    @Test
    void guest_exits_system_cart_deleted() {
        ///
    }

    @Test
    void guest_registers_with_valid_details() {
        ///
    }

    @Test
    void guest_registers_with_invalid_email() {
        ///
    }

    @Test
    void guest_registration_interrupted_before_submit() {
        ///
    }

    @Test
    void guest_login_with_valid_credentials() {
        ///
    }

    @Test
    void guest_login_with_wrong_password() {
        ///
    }

    @Test
    void guest_gets_store_and_product_info_when_available() {
        ///
    }

    @Test
    void guest_gets_store_info_when_no_stores_available() {
        ///
    }

    @Test
    void guest_gets_store_info_when_store_has_no_products() {
        ///
    }

    @Test
    void guest_can_search_all_stores_by_keyword() {
       ///
    }

    @Test
    void guest_search_returns_empty_when_no_matches() {
        ///
    }

    @Test
    void guest_searches_in_specific_store() {
        ///
    }

    @Test
    void guest_adds_product_to_cart() {
        ///
    }

    @Test
    void guest_updates_cart_quantity_and_removes_item() {
        ///
    }

    @Test
    void guest_purchases_cart_successfully() throws Exception {
        User guestUser = userService.getUserRepository().findById(GUEST);
        guestUser.addProductToCart(storeId, productId, 1);
        String shippingAddress = "123 Guest Street";
        String contactInfo = "guest@example.com";
        ShoppingCart guestCart = guestUser.getShoppingCart();
        Purchase purchase = purchaseService.executePurchase(GUEST, guestCart, shippingAddress, contactInfo);
        assertNotNull(purchase, "Purchase should not be null");
        assertEquals(GUEST, purchase.getUserId(), "Buyer ID should be guest");
    }

    @Test
    void guest_purchasing_cart_fails_due_to_stock() {
        ///
    }

    @Test
    void guest_purchasing_cart_fails_due_to_payment() {
        ///
    }
}  