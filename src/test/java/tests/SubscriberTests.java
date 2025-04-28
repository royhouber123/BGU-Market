package tests;

import market.application.AuthService.AuthTokens;
import market.domain.user.ShoppingCart;
import support.AcceptanceTestBase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SubscriberTests extends AcceptanceTestBase {


    private static final String SUB = "subscriber1";
    private static final String PW  = "password1";


    private int storeId;


    @BeforeEach
    void init() throws Exception {
        // ① register the subscriber
        userService.register(SUB, PW);
        // ② create a store with a single product so the tests can interact
        storeService.createStore("GadgetStore", 1 /*founderId*/);
        storeId = storeService.getStore("GadgetStore").getStoreID();
        storeService.addNewProduct(1, storeId, "Keyboard", "PERIPHERALS", 10, 250);
    }

    @Test
    void subscriber_logs_in_and_cart_is_restored() throws Exception {
        // subscriber adds a product while logged in
        AuthTokens firstLogin = authService.login(SUB, PW);
        userService.addProductToCart(SUB, storeId, "Keyboard", 2);
        ShoppingCart before = userService.getCart(SUB);
        authService.logout(firstLogin.refreshToken(), firstLogin.accessToken());

        // subscriber "re‑logs" (simulate a new session)
        // for our simple in‑memory services we just call login again
        AuthTokens secondLogin = authService.login(SUB, PW);
        ShoppingCart after = userService.getCart(SUB);

        assertEquals(before.getAllStoreBags().size(), after.getAllStoreBags().size(),
                     "Cart items should persist between log‑ins in the same repository instance");
        authService.logout(secondLogin.refreshToken(), secondLogin.accessToken());
        
    }

   

    @Test
    void subscriber_can_view_personal_purchase_history() {
        ///
    }

    @Test
    void subscriber_can_rate_purchased_product() {
        ///
    }

    @Test
    void subscriber_cannot_rate_without_purchase() {
        ///
    }

    @Test
    void subscriber_sends_message_to_store() {
        ///
    }

    @Test
    void subscriber_logs_out_and_cart_preserved() {
       ///
    }
}
