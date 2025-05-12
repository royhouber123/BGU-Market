package tests;

import support.AcceptanceTestBase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import market.application.AuthService.AuthToken;
import market.domain.purchase.Purchase;
import market.domain.store.Listing;
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

    @BeforeEach
    void setUp() throws Exception {
        userService.register("user1", "password1"); //user1
        userService.register("user2", "password2"); //user2
        storeId = this.storeService.createStore("store1", "1"); //manager1 create store
    }


    @Test
    void guest_enters_system_initializes_cart() {
        String guestName = "guest_test_1";
        assertDoesNotThrow(() -> {
            userService.register(guestName);
            ShoppingCart cart = userService.getUserRepository().getCart(guestName);
            assertNotNull(cart, "Guest's shopping cart should be initialized");
            assertTrue(cart.getAllStoreBags().isEmpty(), "Guest's shopping cart should be empty on registration");
        });
    }

    @Test
    void guest_exits_system_cart_deleted() {
        String guestName = "guest_test_2";
        assertDoesNotThrow(() -> {
            userService.register(guestName);
            ShoppingCart cart = userService.getUserRepository().getCart(guestName);
            assertNotNull(cart, "Cart should exist after guest registers");
            userService.getUserRepository().delete(guestName);
            assertThrows(RuntimeException.class, () -> {
                userService.getUserRepository().getCart(guestName);
                }, "Expected cart retrieval to fail after guest deletion");
        });
    }

    @Test
    void guest_registers_with_valid_details() {
        String username = "new_subscriber";
        String password = "securePass123";
        assertDoesNotThrow(() -> {
            userService.register(username, password);
            User user = userService.getUserRepository().findById(username);
            assertNotNull(user, "User should exist after registration");
        });
    }



    @Test
    void guest_registers_with_short_password() { //we need to agree about valid password- and the register function should check it
        ///
    }

    @Test
    void guest_login_with_valid_credentials() {
    assertDoesNotThrow(() -> {
        userService.register("user3", "password3");
        AuthToken auth = authService.login("user3", "password3");
        String token = auth.token();
        assertNotNull(token, "Token should not be null after successful login");
        });
    }

    @Test
    void guest_login_with_wrong_password() {
        assertThrows(Exception.class, () -> {
            authService.login("user2", "wrongPassword");
        });
    }

    @Test
    void guest_gets_store_and_product_info_when_available() { //omer needs to implement the function in service
        ///
    }

    @Test
    void guest_gets_store_info_when_no_stores_available() { //omer needs to implement the function in service
        ///
    }

    @Test
    void guest_gets_store_info_when_store_has_no_products() { //omer needs to implement the function in service
        ///
    }

    @Test
    void guest_can_search_products_across_all_stores_by_keyword() {
        assertDoesNotThrow(() -> {
            userService.register("manager1", "1234");
            userService.register("manager2", "1234");
            userService.register("manager3", "1234");
            //Create first store with a notebook
            String storeId1 = storeService.createStore("StoreA", "manager1");
            storeService.addNewListing(
                "manager1",
                storeId1,
                "p1",
                "Notebook Classic",
                "Stationery",
                "Ruled notebook",
                10,
                15.0
            );
            //Create second store with another notebook
            String storeId2 = storeService.createStore("StoreB", "manager2");
            storeService.addNewListing(
                "manager2",
                storeId2,
                "p2",
                "Notebook Deluxe",
                "Stationery",
                "Premium notebook with hard cover",
                5,
                25.0
            );
            //Create third store with a pencil
            String storeId3 = storeService.createStore("StoreC", "manager3");
            storeService.addNewListing(
                "manager3",
                storeId3,
                "p3",
                "Yellow Pencil",
                "Stationery",
                "HB classic pencil",
                30,
                2.5
            );
            //Search for "note" → expect 2 results
            String keyword1 = "note";
            List<Listing> noteResults = productService.searchByProductName(keyword1);
            assertEquals(2, noteResults.size(), "Expected exactly 2 notebook products");
            for (Listing listing : noteResults) {
                assertTrue(
                    listing.getProductName().toLowerCase().contains(keyword1.toLowerCase()),
                    "Each product name should contain the keyword 'note'"
                );
            }
            //Search for "pencil" → expect 1 result
            String keyword2 = "pencil";
            List<Listing> pencilResults = productService.searchByProductName(keyword2);
            assertEquals(1, pencilResults.size(), "Expected exactly 1 pencil product");
            assertTrue(
                pencilResults.get(0).getProductName().toLowerCase().contains(keyword2),
                "Pencil product name should contain the keyword 'pencil'"
            );
        });
    }


    @Test
    void guest_search_returns_empty_when_no_matches() {
        assertDoesNotThrow(() -> {
            userService.register("manager1", "1234");
            userService.register("manager2", "1234");
            //Create first store and add notebook
            String storeId1 = storeService.createStore("StoreA", "manager1");
            storeService.addNewListing(
                "manager1",
                storeId1,
                "p1",
                "Notebook",
                "Stationery",
                "Simple ruled notebook",
                10,
                12.5
            );
            //Create second store and add pencil case
            String storeId2 = storeService.createStore("StoreB", "manager2");
            storeService.addNewListing(
                "manager2",
                storeId2,
                "p2",
                "Pencil Case",
                "Stationery",
                "Blue fabric pencil case",
                8,
                9.99
            );
            //search for a keyword that doesnt exist
            String keyword = "unicorn-rainbow-sandwich";
            List<Listing> results = productService.searchByProductName(keyword);
            assertTrue(results.isEmpty(), "Expected no products to match a completely unrelated keyword");
        });
    }


    @Test
    void guest_searches_in_specific_store_exists() {
        assertDoesNotThrow(() -> {
            userService.register("m1", "1234");
            String storeIdAlpha = storeService.createStore("StoreAlpha", "m1");
            //Add two notebooks to Alpha
            storeService.addNewListing(
                "m1",
                storeIdAlpha,
                "n1",
                "Notebook Classic",
                "Stationery",
                "Basic notebook",
                10,
                12.5
            );
            storeService.addNewListing(
                "m1",
                storeIdAlpha,
                "n2",
                "Notebook Pro",
                "Stationery",
                "Premium notebook",
                8,
                18.0
            );
            //Add unrelated product to Alpha
            storeService.addNewListing(
                "m1",
                storeIdAlpha,
                "n3",
                "Marker Red",
                "Stationery",
                "Permanent red marker",
                5,
                4.0
            );
            //Search for "note" in StoreAlpha
            String keyword = "note";
            List<Listing> resultsAlpha = productService.searchInStoreByName(storeIdAlpha, keyword);
            assertEquals(2, resultsAlpha.size(), "Expected 2 notebook products in StoreAlpha");
            for (Listing listing : resultsAlpha) {
                assertTrue(
                    listing.getProductName().toLowerCase().contains(keyword),
                    "Product name should contain the keyword 'note'"
                );
            }
        });
    }


    @Test
    void guest_searches_in_specific_store_doesnt_exist() {
        assertDoesNotThrow(() -> {
            String nonExistingStoreId = "9999";
            String keyword = "notebook";
            List<Listing> results = productService.searchInStoreByName(nonExistingStoreId, keyword);
            assertTrue(results.isEmpty(), "Expected no results from a non-existing store");
        });
    }


    @Test
    void guest_searches_in_specific_store_no_matching_products() {
        assertDoesNotThrow(() -> {
            userService.register("managerX", "1234");
            String storeId = storeService.createStore("UniqueStore", "managerX");
            //Add unrelated products
            storeService.addNewListing(
                "managerX",
                storeId,
                "p1",
                "Stapler",
                "Office Supplies",
                "Standard metal stapler",
                5,
                12.0
            );
            storeService.addNewListing(
                "managerX",
                storeId,
                "p2",
                "Paper Clips",
                "Office Supplies",
                "Pack of 100 clips",
                10,
                3.0
            );
            //Search for a product name that doesn't exist
            String keyword = "notebook";  
            List<Listing> results = productService.searchInStoreByName(storeId, keyword);
            // Assert no results found
            assertTrue(results.isEmpty(), "Expected no results when no products match the keyword");
        });
    }


    @Test
    void guest_adds_product_to_cart_valid() {
        assertDoesNotThrow(() -> {
            //Add product
            userService.getUserRepository().findById("user1").addProductToCart(storeId, "gvina", 2);
            //Check added
            ShoppingCart cartBefore = userService.getUserRepository().getCart("user1");
            assertEquals(2, cartBefore.getStoreBag(storeId).getProductQuantity("gvina"));
            //Remove product
            userService.getUserRepository().findById("user1").removeProductFromCart(storeId, "gvina", 2);
            //Check removed — entire StoreBag should be gone
            ShoppingCart cartAfter = userService.getUserRepository().getCart("user1");
            assertNull(cartAfter.getStoreBag(storeId), "Store bag should be null after all products are removed");
        });
    }


    @Test
    void guest_adds_product_to_cart_product_doesnt_exist() { //we didnt check if the product is exist when add product
        ///
    }


    @Test
    void guest_adds_product_to_cart_not_enough_quantity_in_stock() { //we didnt check if the product in stock when add product
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
        userService.getUserRepository().register("manager", "1234"); //Register a store manager who will own the store and add products
        String storeId1 = storeService.createStore("SchoolStore", "manager"); //Create a new store named "SchoolStore" with the manager as the founder
        int quantity=5;
        String listingId=storeService.addNewListing( //Add a new product listing ("Notebook") to the created store
            "manager",
            storeId1,
            "p1",
            "Notebook",
            "writing",
            "Simple notebook",
            quantity,
            25.0
        );
        when(paymentService.processPayment(anyString())).thenReturn(true); //Stub the payment service to always return success
        when(shipmentService.ship(anyString(), anyString(), anyDouble())).thenReturn("SHIP123"); //Stub the shipment service to always return a fixed shipment ID
        userService.register("guest"); //Register the guest user
        User guestUser=userService.getUserRepository().findById("guest"); //Retrieve the guest user from the repository
        guestUser.addProductToCart(storeId1, listingId, 1); //Add one unit of the product to the guest's shopping cart
        String shippingAddress = "123 Guest Street"; 
        String contactInfo = "guest@example.com"; 
        ShoppingCart guestCart = guestUser.getShoppingCart(); //Retrieve the guest's current shopping cart
        Purchase purchase = purchaseService.executePurchase("guest", guestCart, shippingAddress, contactInfo);
        //Assert that the purchase was successfully completed (i.e., not null)
        assertNotNull(purchase, "Purchase should not be null");
        //Assert that the correct user is recorded in the purchase
        assertEquals("guest", purchase.getUserId(), "Buyer ID should be guest");
        //Reload the user from the repository to ensure updated state (after purchase)
        User refreshedGuest = userService.getUserRepository().findById("guest");
        ShoppingCart refreshedCart = refreshedGuest.getShoppingCart();
        //Assert that the cart is now empty after purchase
        assertTrue(refreshedCart.getAllStoreBags().isEmpty(), "Shopping cart should be empty after purchase");
        //Check that stock was reduced by 1 unit after purchase
        int remainingStock = storeService.getListingRepository().getListingById(listingId).getQuantityAvailable();
        assertEquals(quantity - 1, remainingStock, "Stock should decrease by purchased amount");
    }

    @Test
    void guest_purchasing_cart_fails_due_to_stock() throws Exception { //there is a stock when added to bag but not when purchase???
        userService.getUserRepository().register("manager", "1234"); //Register a store manager who will own the store and add products
        String storeId1 = storeService.createStore("SchoolStore", "manager"); //Create a new store named "SchoolStore" with the manager as the founder
        int quantity=5;
        String listingId=storeService.addNewListing( //Add a new product listing ("Notebook") to the created store
            "manager",
            storeId1,
            "p1",
            "Notebook",
            "writing",
            "Simple notebook",
            quantity,
            25.0
        );
        when(paymentService.processPayment(anyString())).thenReturn(true); //Stub the payment service to always return success
        when(shipmentService.ship(anyString(), anyString(), anyDouble())).thenReturn("SHIP123"); //Stub the shipment service to always return a fixed shipment ID
        userService.register("guest"); //Register the guest user
        User guestUser = userService.getUserRepository().findById("guest"); //Retrieve the guest user from the repository
        guestUser.addProductToCart(storeId1, listingId, quantity + 1); //Add a quantity larger than the available stock to the cart
        String shippingAddress = "123 Guest Street";
        String contactInfo = "guest@example.com";
        ShoppingCart guestCart = guestUser.getShoppingCart(); //Get the cart containing the excessive quantity
        //Attempt to execute the purchase — it should fail due to insufficient stock
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
            purchaseService.executePurchase("guest", guestCart, shippingAddress, contactInfo));
        //Verify that the error message indicates a stock issue
        assertTrue(thrown.getMessage().toLowerCase().contains("stock"), "Expected failure due to stock issue, but got: " + thrown.getMessage());
    }

    @Test
    void guest_purchasing_cart_fails_due_to_payment() throws Exception { //after payment failes- what to do with the stock- it already reduced???
        userService.getUserRepository().register("manager", "1234"); //Register a store manager who will own the store and add products
        String storeId1 = storeService.createStore("SchoolStore", "manager"); //Create a new store named "SchoolStore" with the manager as the founder
        int quantity=5;
        String listingId=storeService.addNewListing( //Add a new product listing ("Notebook") to the created store
            "manager",
            storeId1,
            "p1",
            "Notebook",
            "writing",
            "Simple notebook",
            quantity,
            25.0
        );
        when(paymentService.processPayment(anyString())).thenReturn(true); //Stub the payment service to always return success
        when(shipmentService.ship(anyString(), anyString(), anyDouble())).thenReturn("SHIP123"); //Stub the shipment service to always return a fixed shipment ID
        userService.register("guest"); //Register the guest user
        User guestUser = userService.getUserRepository().findById("guest"); //Retrieve the guest user from the repository
        guestUser.addProductToCart(storeId1, listingId, 1); //Add a valid product to the guest's cart
        String shippingAddress = "123 Guest Street";
        String contactInfo = "guest@example.com";
        when(paymentService.processPayment(anyString())).thenReturn(false); //Simulate a payment failure by mocking the payment service
        ShoppingCart guestCart = guestUser.getShoppingCart(); //Get the current state of the guest's shopping cart
        //Attempt to execute the purchase — it should fail due to payment issue
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
        purchaseService.executePurchase("guest", guestCart, shippingAddress, contactInfo));
        // Confirm the error message indicates a payment problem
        assertTrue(thrown.getMessage().toLowerCase().contains("payment"),
            "Expected failure due to payment issue, but got: " + thrown.getMessage());   
    }
}  