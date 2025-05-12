package tests;
import market.application.AuthService.AuthToken;
import market.domain.purchase.Purchase;
import market.domain.user.ShoppingCart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import support.AcceptanceTestBase;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
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
        userService.getUserRepository().register(SUB, PW);


        // --- create store + one listing --------------------------------------
        storeService.createStore("GadgetStore", "100");          // founderId
        storeId = storeService.getStore("GadgetStore").getStoreID();
        storeService.addNewListing("sub01",
                                   storeId,
                                   "m1",
                                   "Mouse",
                                   "Electronic",
                                   "Wireless mouse",
                                   5,
                                   100);
        productid = "m1";
        // --- stub external ports ---------------------------------------------
        when(paymentService.processPayment(anyString())).thenReturn(true);
        when(shipmentService.ship(anyString(), anyString(), anyDouble())).thenReturn("123");

        // --- build a real PurchaseService using base-class repositories -------
    }

    /* ---------------------------------------------------------------------- */
    /* 1. successful purchase + personal history                              */
    /* ---------------------------------------------------------------------- */
    @Test
    void subscriber_can_view_personal_purchase_history() throws Exception {

        AuthToken auth = authService.login(SUB, PW);
        String token = auth.token();

        // add to cart
        storeService.getProductListing(storeId,productid);
        //the parse int  will be change when roy and yair change the store id to string
        userService.getUserRepository().findById(SUB).addProductToCart(storeId, productid, 1);
        ShoppingCart cart = userService.getUserRepository().getCart(SUB);

        // execute purchase
        Purchase p = purchaseService.executePurchase(
                SUB, cart, "Beer-Sheva", "050-1234567");

        // history query
        List<Purchase> history = purchaseService.getPurchasesByUser(SUB);
        assertEquals(1, history.size());

        authService.logout(token);
    }

    /* ---------------------------------------------------------------------- */
    /* 2. cannot rate a product without purchasing it first                   */
    /* ---------------------------------------------------------------------- */
    // @Test
    // void subscriber_cannot_rate_without_purchase() throws Exception {

    //     AuthToken auth = authService.login(SUB, PW);
    //     String token = auth.token();

    //     RuntimeException ex = assertThrows(RuntimeException.class,
    //         () -> userService.rateProduct(SUB, "Mouse", 4));

    //     assertTrue(ex.getMessage().contains("No matching purchase"));

    //     authService.logout(token);
    // }

    /* ---------------------------------------------------------------------- */
    /* 3. rating works after purchase                                          */
    /* ---------------------------------------------------------------------- */
    // @Test
    // void subscriber_can_rate_purchased_product() throws Exception {

    //     AuthToken auth = authService.login(SUB, PW);
    //     String token = auth.token();
    //     userService.addProductToCart(SUB, storeId, "Mouse", 1);
    //     purchaseService.executePurchase(
    //             SUB, userService.getCart(SUB), "TLV", "050-1");

    //     String res = userService.rateProduct(SUB, "Mouse", 5);
    //     assertEquals("Rating submitted", res);

    //     authService.logout(token);
    // }

    /* ---------------------------------------------------------------------- */
    /* 4. send message to store owner                                          */
    /* ---------------------------------------------------------------------- */
    // @Test
    // void subscriber_sends_message_to_store() throws Exception {

    //     AuthToken auth = authService.login(SUB, PW);
    //     String token = auth.token();

    //     String msg = storeService.sendMessageToStore(SUB, storeId,
    //                                                  "Do you have blue?");
    //     assertEquals("Message sent", msg);

    //     authService.logout(token);
    // }

    /* ---------------------------------------------------------------------- */
    /* 5. logout does NOT clear cart                                           */
    /* ---------------------------------------------------------------------- */
    // The commented out code block you provided is a test case method named
    // `subscriber_logs_out_and_cart_preserved()`. This test case is checking the behavior of the
    // shopping cart when a subscriber logs out and then logs back in. Here is a breakdown of what the
    // test case is doing:
    @Test
    void subscriber_logs_out_and_cart_preserved() throws Exception {

        AuthToken auth1 = authService.login(SUB, PW);
        String token1 = auth1.token();
        userService.addProductToCart(storeId, "Mouse", 2);
        ShoppingCart before = userService.getCart();
        authService.logout(token1);

        // new login
        AuthToken auth2 = authService.login(SUB, PW);
        String token2 = auth2.token();
        ShoppingCart after = userService.getCart();
        assertEquals(before.getAllStoreBags().size(), after.getAllStoreBags().size());

        authService.logout(token2);
    }
}
