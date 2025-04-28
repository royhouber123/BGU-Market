package tests;

import market.application.PurchaseService;
import market.application.AuthService.AuthTokens;
import market.domain.purchase.Purchase;
import market.domain.user.ShoppingCart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import support.AcceptanceTestBase;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Acceptance-level scenarios for a subscriber.
 */
class SubscriberTests extends AcceptanceTestBase {

    private static final String SUB = "sub01";
    private static final String PW  = "pw";

    private String  storeId; 
    private String productid;        // string id in StoreService
    

    @BeforeEach
    void setUp() throws Exception {

        // --- subscriber exists ------------------------------------------------
        userService.register(SUB, PW);


        // --- create store + one listing --------------------------------------
        storeService.createStore("GadgetStore", "100");          // founderId
        storeId = storeService.getStore("GadgetStore").getStoreID();
        storeService.addNewListing("100",
                                   storeId,
                                   "m1",
                                   "Mouse",
                                   "Wireless mouse",
                                   5,
                                   100);
        productid = "m1";
        // --- stub external ports ---------------------------------------------
        when(paymentService.processPayment(any())).thenReturn(true);
        when(shipmentService.ship(any(), any(), any())).thenReturn("123");

        // --- build a real PurchaseService using base-class repositories -------
    }

    /* ---------------------------------------------------------------------- */
    /* 1. successful purchase + personal history                              */
    /* ---------------------------------------------------------------------- */
    @Test
    void subscriber_can_view_personal_purchase_history() throws Exception {

        AuthTokens tok = authService.login(SUB, PW);

        // add to cart
        storeService.getProductListing(storeId,productid);
        //the parse int  will be change when roy and yair change the store id to string
        userService.addProductToCart(SUB, storeId, "Mouse", 1);
        ShoppingCart cart = userService.getCart(SUB);

        // execute purchase
        Purchase p = purchaseService.executePurchase(
                SUB, cart, "Beer-Sheva", "050-1234567");

        // history query
        List<Purchase> history = purchaseService.getPurchasesByUser(SUB);
        assertEquals(1, history.size());

        authService.logout(tok.refreshToken(), tok.accessToken());
    }

    /* ---------------------------------------------------------------------- */
    /* 2. cannot rate a product without purchasing it first                   */
    /* ---------------------------------------------------------------------- */
    // @Test
    // void subscriber_cannot_rate_without_purchase() throws Exception {

    //     AuthTokens tok = authService.login(SUB, PW);

    //     RuntimeException ex = assertThrows(RuntimeException.class,
    //         () -> userService.rateProduct(SUB, "Mouse", 4));

    //     assertTrue(ex.getMessage().contains("No matching purchase"));

    //     authService.logout(tok.refreshToken(), tok.accessToken());
    // }

    /* ---------------------------------------------------------------------- */
    /* 3. rating works after purchase                                          */
    /* ---------------------------------------------------------------------- */
    // @Test
    // void subscriber_can_rate_purchased_product() throws Exception {

    //     AuthTokens tok = authService.login(SUB, PW);
    //     userService.addProductToCart(SUB, storeId, "Mouse", 1);
    //     purchaseService.executePurchase(
    //             SUB, userService.getCart(SUB), "TLV", "050-1");

    //     String res = userService.rateProduct(SUB, "Mouse", 5);
    //     assertEquals("Rating submitted", res);

    //     authService.logout(tok.refreshToken(), tok.accessToken());
    // }

    /* ---------------------------------------------------------------------- */
    /* 4. send message to store owner                                          */
    /* ---------------------------------------------------------------------- */
    // @Test
    // void subscriber_sends_message_to_store() throws Exception {

    //     AuthTokens tok = authService.login(SUB, PW);

    //     String msg = storeService.sendMessageToStore(SUB, storeId,
    //                                                  "Do you have blue?");
    //     assertEquals("Message sent", msg);

    //     authService.logout(tok.refreshToken(), tok.accessToken());
    // }

    /* ---------------------------------------------------------------------- */
    /* 5. logout does NOT clear cart                                           */
    /* ---------------------------------------------------------------------- */
    // @Test
    // void subscriber_logs_out_and_cart_preserved() throws Exception {

    //     AuthTokens tok1 = authService.login(SUB, PW);
    //     userService.addProductToCart(SUB, storeId, "Mouse", 2);
    //     ShoppingCart before = userService.getCart(SUB);
    //     authService.logout(tok1.refreshToken(), tok1.accessToken());

    //     // new login
    //     AuthTokens tok2 = authService.login(SUB, PW);
    //     ShoppingCart after = userService.getCart(SUB);
    //     assertEquals(before.getTotalItems(), after.getTotalItems());

    //     authService.logout(tok2.refreshToken(), tok2.accessToken());
    // }
}
