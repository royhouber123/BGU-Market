package tests;

import support.AcceptanceTestBase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import market.domain.purchase.Purchase;
import market.domain.user.ShoppingCart;
import market.domain.user.User;
import market.middleware.TokenUtils;

import java.io.EOFException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class GuestTests extends AcceptanceTestBase {

    private String storeId;
    
    
    private static final String GUEST = "guest";
    private static final String MANAGER = "manager";
    private String storeId1;
    private String listingId;
    private int quantity = 5;

    @BeforeEach
    void setUp() throws Exception {
        userService.register("user1", "password1");
        userService.register("user2", "password2");
        storeId = this.storeService.createStore("store1", "1");





        userService.getUserRepository().register(MANAGER, "1234"); //Register a store manager who will own the store and add products
        storeId1 = storeService.createStore("SchoolStore", MANAGER); //Create a new store named "SchoolStore" with the manager as the founder
        listingId=storeService.addNewListing( //Add a new product listing ("Notebook") to the created store
            MANAGER,
            storeId1,
            "p1",
            "Notebook",
            "Simple notebook",
            quantity,
            25.0
        );
        when(paymentService.processPayment(anyString())).thenReturn(true); //Stub the payment service to always return success
        when(shipmentService.ship(anyString(), anyString(), anyDouble())).thenReturn("SHIP123"); //Stub the shipment service to always return a fixed shipment ID
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
    void guest_registers_with_short_password() {
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
    void guest_searches_in_specific_store_exists() {
        ///
    }

    @Test
    void guest_searches_in_specific_store_doesnt_exist() {
        ///
    }

    @Test
    void guest_searches_in_specific_store_no_matching_products() {
        ///
    }

    @Test
    void guest_adds_product_to_cart_valid() {
        ///
    }

    @Test
    void guest_adds_product_to_cart_product_doesnt_exist() {
        ///
    }

    @Test
    void guest_adds_product_to_cart_not_enough_quantity_in_stock() {
        ///
    }

    @Test
    void guest_views_cart_contents_success() {
        ///
    }

    @Test
    void guest_edit_cart_contents_success() {
        ///
    }

    @Test
    void guest_edit_cart_contents_product_not_in_cart() {
        ///
    }

    @Test
    void guest_edit_cart_contents_product_to_zero_stock() {
        ///
    }

    @Test
    void guest_purchases_cart_successfully() throws Exception {
        userService.register(GUEST); //Register the guest user
        User guestUser=userService.getUserRepository().findById(GUEST); //Retrieve the guest user from the repository
        guestUser.addProductToCart(storeId1, listingId, 1); //Add one unit of the product to the guest's shopping cart
        String shippingAddress = "123 Guest Street"; 
        String contactInfo = "guest@example.com"; 
        ShoppingCart guestCart = guestUser.getShoppingCart(); //Retrieve the guest's current shopping cart
        Purchase purchase = purchaseService.executePurchase(GUEST, guestCart, shippingAddress, contactInfo);
        //Assert that the purchase was successfully completed (i.e., not null)
        assertNotNull(purchase, "Purchase should not be null");
        //Assert that the correct user is recorded in the purchase
        assertEquals(GUEST, purchase.getUserId(), "Buyer ID should be guest");
        //Reload the user from the repository to ensure updated state (after purchase)
        User refreshedGuest = userService.getUserRepository().findById(GUEST);
        ShoppingCart refreshedCart = refreshedGuest.getShoppingCart();
        //Assert that the cart is now empty after purchase
        assertTrue(refreshedCart.getAllStoreBags().isEmpty(), "Shopping cart should be empty after purchase");
        //Check that stock was reduced by 1 unit after purchase
        int remainingStock = storeService.getListingRepository().getListingById(listingId).getQuantityAvailable();
        assertEquals(quantity - 1, remainingStock, "Stock should decrease by purchased amount");
    }

    @Test
    void guest_purchasing_cart_fails_due_to_stock() throws Exception { //there is a stock when added to bag but not when purchase???
        userService.register(GUEST); //Register the guest user
        User guestUser = userService.getUserRepository().findById(GUEST); //Retrieve the guest user from the repository
        guestUser.addProductToCart(storeId1, listingId, quantity + 1); //Add a quantity larger than the available stock to the cart
        String shippingAddress = "123 Guest Street";
        String contactInfo = "guest@example.com";
        ShoppingCart guestCart = guestUser.getShoppingCart(); //Get the cart containing the excessive quantity
        //Attempt to execute the purchase — it should fail due to insufficient stock
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
            purchaseService.executePurchase(GUEST, guestCart, shippingAddress, contactInfo));
        //Verify that the error message indicates a stock issue
        assertTrue(thrown.getMessage().toLowerCase().contains("stock"), "Expected failure due to stock issue, but got: " + thrown.getMessage());
    }

    @Test
    void guest_purchasing_cart_fails_due_to_payment() throws Exception { //after payment failes- what to do with the stock- it already reduced???
        userService.register(GUEST, ""); //Register the guest user
        User guestUser = userService.getUserRepository().findById(GUEST); //Retrieve the guest user from the repository
        guestUser.addProductToCart(storeId1, listingId, 1); //Add a valid product to the guest's cart
        String shippingAddress = "123 Guest Street";
        String contactInfo = "guest@example.com";
        when(paymentService.processPayment(anyString())).thenReturn(false); //Simulate a payment failure by mocking the payment service
        ShoppingCart guestCart = guestUser.getShoppingCart(); //Get the current state of the guest's shopping cart
        //Attempt to execute the purchase — it should fail due to payment issue
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
            purchaseService.executePurchase(GUEST, guestCart, shippingAddress, contactInfo));
        // Confirm the error message indicates a payment problem
        assertTrue(thrown.getMessage().toLowerCase().contains("payment"),
            "Expected failure due to payment issue, but got: " + thrown.getMessage());   
    }
}  