package tests;

import support.AcceptanceTestBase;
import utils.ApiResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import market.application.AuthService.AuthToken;
import market.domain.purchase.Purchase;
import market.domain.store.Listing;
import market.domain.user.IUserRepository;
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
        storeId = this.storeService.createStore("store1", "1").getData(); //manager1 create store
    }


    @Test
    void guest_enters_system_initializes_cart() {
        //Step 1: Enter the system as a guest and verify success
        String guestName = "guest";
        ApiResponse<Void> registerResponse = userService.register(guestName);
        assertTrue(registerResponse.isSuccess(), "Enter as a guest failed: " + registerResponse.getError());
        //Step 2: Retrieve the user repository and ensure it was retrieved successfully
        ApiResponse<IUserRepository> repoResp = userService.getUserRepository();
        assertTrue(repoResp.isSuccess(), "Failed to retrieve user repository: " + repoResp.getError());
        //Step 3: Extract the actual repository object from the response
        IUserRepository userRepository = repoResp.getData();
        //Step 4: Get the shopping cart for the guest user
        ShoppingCart cart = userRepository.getCart(guestName);
        //Step 5: Verify that the cart exists
        assertNotNull(cart, "Guest's shopping cart should be initialized");
        //Step 6: Verify that the cart is initially empty (no store bags)
        assertTrue(cart.getAllStoreBags().isEmpty(), "Guest's shopping cart should be empty on registration");
    }

    @Test
    void guest_exits_system_cart_deleted() {
        //Step 1: Enter the system as a guest
        String guestName = "guest";
        userService.register(guestName);
        //Step 2: Retrieve the user repository and ensure it was retrieved successfully
        ApiResponse<IUserRepository> repoResp = userService.getUserRepository();
        assertTrue(repoResp.isSuccess(), "Failed to retrieve user repository: " + repoResp.getError());
        //Step 3: Extract the actual repository object from the response
        IUserRepository userRepository = repoResp.getData();
        //Step 4: Get the guest's cart to verify it was initialized
        ShoppingCart cart = userRepository.getCart(guestName);
        assertNotNull(cart, "Cart should exist after guest registers");
        //Step 5: Simulate guest leaving the system (e.g., closing the browser)
        userRepository.delete(guestName);
        //Step 6: Attempt to access the guest's cart again — should throw an exception
        assertThrows(RuntimeException.class, () -> {
                userService.getUserRepository().getData().getCart(guestName);
                }, "Expected cart retrieval to fail after guest deletion");
    }

    @Test
    void guest_registers_with_valid_details() {
        //Step 1: Attempt to register a new subscriber with valid credentials
        String username = "new_subscriber";
        String password = "1234";
        ApiResponse<Void> registerResponse = userService.register(username, password);
        assertTrue(registerResponse.isSuccess(), "Registration failed: " + registerResponse.getError());
        //Step 2: Retrieve the user repository to verify the user was registered successfully
        ApiResponse<IUserRepository> repoResponse = userService.getUserRepository();
        assertTrue(repoResponse.isSuccess(), "Failed to retrieve repository: " + repoResponse.getError());
        //Step 3: Extract the actual repository from the response
        IUserRepository repo = repoResponse.getData();
        //Step 4: Check that the user exists in the repository
        User user = repo.findById(username);
        assertNotNull(user, "User should exist after registration");
    }



    @Test
    void guest_registers_with_short_password() { //we need to agree about valid password- and the register function should check it
        ///
    }

    @Test
    void guest_login_with_valid_credentials() {
        //Step 1: Login with valid credentials (user was registered in @BeforeEach)
        ApiResponse<AuthToken> loginResponse = authService.login("user1", "password1");
        assertTrue(loginResponse.isSuccess(), "Login failed: " + loginResponse.getError());
        //Step 2: Retrieve the token from the login response
        AuthToken auth = loginResponse.getData();
        String token = auth.token();
        //Step 3: Ensure the token is valid and not null
        assertNotNull(token, "Token should not be null after successful login");
    }

    @Test
    void guest_login_with_wrong_password() {
        //Step 1: Attempt to login with incorrect password
        ApiResponse<AuthToken> loginResponse = authService.login("user2", "wrongPassword");
        //Step 2: Assert that login failed and contains an appropriate error message
        assertFalse(loginResponse.isSuccess(), "Login should fail with wrong password");
        assertNotNull(loginResponse.getError(), "Error message should be provided");
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
            //Step 1: Register 3 managers
            assertTrue(userService.register("manager1", "1234").isSuccess(), "Failed to register manager1");
            assertTrue(userService.register("manager2", "1234").isSuccess(), "Failed to register manager2");
            assertTrue(userService.register("manager3", "1234").isSuccess(), "Failed to register manager3");
            //Step 2: Create 3 stores, each owned by a different manager
            String storeId1 = storeService.createStore("StoreA", "manager1").getData();
            String storeId2 = storeService.createStore("StoreB", "manager2").getData();
            String storeId3 = storeService.createStore("StoreC", "manager3").getData();
            //Step 3: Add notebook to first store
            storeService.addNewListing(
                "manager1", storeId1, "p1", "Notebook Classic", "Stationery",
                "Ruled notebook", 10, 15.0
            );
            //Step 4: Add notebook to second store
            storeService.addNewListing(
                "manager2", storeId2, "p2", "Notebook Deluxe", "Stationery",
                "Premium notebook with hard cover", 5, 25.0
            );
            //Step 5: Add pencil to third store
            storeService.addNewListing(
                "manager3", storeId3, "p3", "Yellow Pencil", "Stationery",
                "HB classic pencil", 30, 2.5
            );
            //Step 6: Search for "note" keyword (expect 2 results)
            String keyword1 = "note";
            ApiResponse<List<Listing>> noteSearchResponse = productService.searchByProductName(keyword1);
            assertTrue(noteSearchResponse.isSuccess(), "Failed to search products by keyword: " + keyword1);
            List<Listing> noteResults = noteSearchResponse.getData();
            assertEquals(2, noteResults.size(), "Expected exactly 2 notebook products");
            for (Listing listing : noteResults) {
                assertTrue(
                    listing.getProductName().toLowerCase().contains(keyword1),
                    "Each product name should contain the keyword 'note'"
                );
            }
            //Step 7: Search for "pencil" keyword (expect 1 result)
            String keyword2 = "pencil";
            ApiResponse<List<Listing>> pencilSearchResponse = productService.searchByProductName(keyword2);
            assertTrue(pencilSearchResponse.isSuccess(), "Failed to search products by keyword: " + keyword2);
            List<Listing> pencilResults = pencilSearchResponse.getData();
            assertEquals(1, pencilResults.size(), "Expected exactly 1 pencil product");
            assertTrue(
                pencilResults.get(0).getProductName().toLowerCase().contains(keyword2),
                "Pencil product name should contain the keyword 'pencil'"
            );
        });
    }


    @Test
    void guest_search_returns_empty_when_no_matches() {
        //Step 1: Register two store managers
        assertTrue(userService.register("manager1", "1234").isSuccess(), "Failed to register manager1");
        assertTrue(userService.register("manager2", "1234").isSuccess(), "Failed to register manager2");
        //Step 2: Create first store and add a notebook
        String storeId1 = storeService.createStore("StoreA", "manager1").getData();
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
        //Step 3: Create second store and add a pencil case
        String storeId2 = storeService.createStore("StoreB", "manager2").getData();
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
        //Step 4: Search for a keyword that does not exist
        String keyword = "unicorn-rainbow-sandwich";
        ApiResponse<List<Listing>> searchResp = productService.searchByProductName(keyword);
        assertTrue(searchResp.isSuccess(), "Search failed: " + searchResp.getError());
        List<Listing> results = searchResp.getData();
        //Step 5: Assert that no results were found
        assertTrue(results.isEmpty(), "Expected no products to match a completely unrelated keyword");
    }


    @Test
    void guest_searches_in_specific_store_exists() {
        //Step 1: Register a manager to own the store
        assertTrue(userService.register("m1", "1234").isSuccess(), "Failed to register m1");
        //Step 2: Create a store called StoreAlpha
        String storeIdAlpha = storeService.createStore("StoreAlpha", "m1").getData();
        //Step 3: Add two notebook-related products to StoreAlpha
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
        //Step 4: Add a product that is unrelated to the search keyword
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
        //Step 5: Search for the keyword "note" in StoreAlpha
        String keyword = "note";
        ApiResponse<List<Listing>> searchResp = productService.searchInStoreByName(storeIdAlpha, keyword);
        assertTrue(searchResp.isSuccess(), "Search in store failed: " + searchResp.getError());
        List<Listing> resultsAlpha = searchResp.getData();
        //Step 6: Validate that only notebook-related products are returned
        assertEquals(2, resultsAlpha.size(), "Expected 2 notebook products in StoreAlpha");
        for (Listing listing : resultsAlpha) {
            assertTrue(
                listing.getProductName().toLowerCase().contains(keyword),
                "Product name should contain the keyword 'note'"
            );
        }
    }


    @Test
    void guest_searches_in_specific_store_doesnt_exist() {
        //Step 1: Use an ID for a store that doesn't exist in the system
        String nonExistingStoreId = "9999";
        String keyword = "notebook";
        //Step 2: Attempt to search for products by keyword in the non-existing store
        ApiResponse<List<Listing>> searchResp = productService.searchInStoreByName(nonExistingStoreId, keyword);
        //Step 3: Verify that the search was successful and returned an empty list
        assertTrue(searchResp.isSuccess(), "Search operation failed unexpectedly: " + searchResp.getError());
        assertTrue(searchResp.getData().isEmpty(), "Expected no results from a non-existing store");
    }


    @Test
    void guest_searches_in_specific_store_no_matching_products() {
        //Step 1: Register a manager to be the owner of the store
        assertTrue(userService.register("managerX", "1234").isSuccess(), "Failed to register managerX");
        //Step 2: Create a store and add products that don't match the search keyword
        String storeId = storeService.createStore("UniqueStore", "managerX").getData();
        storeService.addNewListing("managerX", storeId, "p1", "Stapler", "Office Supplies", "Standard metal stapler", 5, 12.0);
        storeService.addNewListing("managerX", storeId, "p2", "Paper Clips", "Office Supplies", "Pack of 100 clips", 10, 3.0);
        //Step 3: Search for a product name that doesn't exist in the store
        String keyword = "notebook";
        ApiResponse<List<Listing>> searchResp = productService.searchInStoreByName(storeId, keyword);
        //Step 4: Assert the search was successful and returned no matching results
        assertTrue(searchResp.isSuccess(), "Search operation failed unexpectedly: " + searchResp.getError());
        assertTrue(searchResp.getData().isEmpty(), "Expected no results when no products match the keyword");
    }


    @Test
    void guest_adds_product_to_cart_valid() {
        assertDoesNotThrow(() -> {
            //Step 1: Add a product to the cart of user1
            ApiResponse<IUserRepository> repoResp1 = userService.getUserRepository();
            assertTrue(repoResp1.isSuccess(), "Failed to retrieve user repository: " + repoResp1.getError());
            IUserRepository userRepository1 = repoResp1.getData();
            userRepository1.findById("user1").addProductToCart(storeId, "gvina", 2);
            //Step 2: Retrieve the cart and verify that the product was added with correct quantity
            ApiResponse<IUserRepository> repoResp2 = userService.getUserRepository();
            assertTrue(repoResp2.isSuccess(), "Failed to retrieve user repository: " + repoResp2.getError());
            ShoppingCart cartBefore = repoResp2.getData().getCart("user1");
            assertEquals(2, cartBefore.getStoreBag(storeId).getProductQuantity("gvina"),
                "Expected quantity of 'gvina' in store bag should be 2");
            //Step 3: Remove the product from the cart
            userRepository1.findById("user1").removeProductFromCart(storeId, "gvina", 2);
            //Step 4: Verify the store bag was removed after deleting all products from it
            ApiResponse<IUserRepository> repoResp3 = userService.getUserRepository();
            assertTrue(repoResp3.isSuccess(), "Failed to retrieve user repository: " + repoResp3.getError());
            ShoppingCart cartAfter = repoResp3.getData().getCart("user1");
            assertNull(cartAfter.getStoreBag(storeId),
                "Store bag should be null after removing all quantities of the product");
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
    void guest_views_cart_contents_success() { //we didnt have a function- maybe just use the getCart?
        ///
    }

    @Test
    void guest_edit_cart_contents_success() { //we didnt have a function- look at useCases
        ///
    }

    @Test
    void guest_edit_cart_contents_product_not_in_cart() { //we didnt have a function- look at useCases
        ///
    }

    @Test
    void guest_edit_cart_contents_product_to_zero_stock() { //we didnt have a function- look at useCases
        ///
    }

    @Test
    void guest_purchases_cart_successfully(){
        //Step 1: Register a store manager and create a store
        assertTrue(userService.register("manager", "1234").isSuccess(), "Failed to register manager");
        String storeId1 = storeService.createStore("SchoolStore", "manager").getData();
        //Step 2: Add a product to the store
        int quantity=5;
        String listingId=storeService.addNewListing( 
            "manager",
            storeId1,
            "p1",
            "Notebook",
            "writing",
            "Simple notebook",
            quantity,
            25.0
        ).getData();
        //Step 3: Stub payment and shipment services to simulate success
        when(paymentService.processPayment(anyString())).thenReturn(ApiResponse.ok(true)); 
        when(shipmentService.ship(anyString(), anyString(), anyDouble())).thenReturn(ApiResponse.ok("SHIP123")); 
        //Step 4: Enter as a guest and add the product to their cart
        userService.register("guest");
        User guestUser=userService.getUserRepository().getData().findById("guest"); 
        guestUser.addProductToCart(storeId1, listingId, 1);
        //Step 5: Prepare shipping details and retrieve the guest's cart
        String shippingAddress = "123 Guest Street"; 
        String contactInfo = "guest@example.com"; 
        ShoppingCart guestCart = guestUser.getShoppingCart();
        //Step 6: Execute the purchase and verify success
        ApiResponse<Purchase> purchaseResponse = purchaseService.executePurchase("guest", guestCart, shippingAddress, contactInfo);
        assertTrue(purchaseResponse.isSuccess(), "Purchase failed: " + purchaseResponse.getError());
        //Step 7: Validate purchase object and buyer identity
        Purchase purchase = purchaseResponse.getData();
        assertNotNull(purchase, "Purchase should not be null");
        assertEquals("guest", purchase.getUserId(), "Buyer ID should be guest");
        //Step 8: Verify the guest's cart is now empty
        User refreshedGuest = userService.getUserRepository().getData().findById("guest");
        ShoppingCart refreshedCart = refreshedGuest.getShoppingCart();
        assertTrue(refreshedCart.getAllStoreBags().isEmpty(), "Shopping cart should be empty after purchase");
        //Step 9: Check that product stock was reduced accordingly
        int remainingStock = storeService.getListingRepository().getData().getListingById(listingId).getQuantityAvailable();
        assertEquals(quantity - 1, remainingStock, "Stock should decrease by purchased amount");
    }

    @Test
    void guest_purchasing_cart_fails_due_to_stock() throws Exception { //there is a stock when added to bag but not when purchase???
        //Step 1: Register a store manager and create a store
        assertTrue(userService.register("manager", "1234").isSuccess(), "Failed to register manager");
        String storeId1 = storeService.createStore("SchoolStore", "manager").getData();
        //Step 2: Add a product to the store with limited stock
        int quantity=5;
        String listingId=storeService.addNewListing( 
            "manager",
            storeId1,
            "p1",
            "Notebook",
            "writing",
            "Simple notebook",
            quantity,
            25.0
        ).getData();
        //Step 3: Stub payment and shipment services to always succeed
        when(paymentService.processPayment(anyString())).thenReturn(ApiResponse.ok(true)); 
        when(shipmentService.ship(anyString(), anyString(), anyDouble())).thenReturn(ApiResponse.ok("SHIP123")); 
        //Step 4: Register a guest and add a quantity exceeding available stock
        userService.register("guest"); 
        User guestUser = userService.getUserRepository().getData().findById("guest"); 
        guestUser.addProductToCart(storeId1, listingId, quantity + 1); 
        //Step 5: Attempt to purchase and verify failure due to stock limitation
        String shippingAddress = "123 Guest Street";
        String contactInfo = "guest@example.com";
        ShoppingCart guestCart = guestUser.getShoppingCart(); 
        ApiResponse<Purchase> response = purchaseService.executePurchase("guest", guestCart, shippingAddress, contactInfo);
        //Step 6: Check that the response indicates failure and the error is related to stock
        assertFalse(response.isSuccess(), "Expected purchase to fail due to stock limit");
        assertTrue(response.getError().toLowerCase().contains("stock"), "Expected stock-related error, but got: " + response.getError());
    }

    @Test
    void guest_purchasing_cart_fails_due_to_payment() throws Exception { //after payment failes- what to do with the stock- it already reduced???
        //Step 1: Register a store manager and create a store
        assertTrue(userService.register("manager", "1234").isSuccess(), "Failed to register manager");
        String storeId1 = storeService.createStore("SchoolStore", "manager").getData();
        //Step 2: Add a new listing to the store
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
        ).getData();
        //Step 3: Mock payment and shipment services
        //Simulate payment failure response from the payment service
        when(paymentService.processPayment(anyString())).thenReturn(ApiResponse.fail("Simulated payment failure"));
        //Simulate successful shipment response
        when(shipmentService.ship(anyString(), anyString(), anyDouble())).thenReturn(ApiResponse.ok("SHIP123"));
        //Step 4: Enter as a guest user
        userService.register("guest"); 
        User guestUser = userService.getUserRepository().getData().findById("guest"); 
        //Step 5: Add a product to the guest's shopping cart
        guestUser.addProductToCart(storeId1, listingId, 1); 
        //Step 6: Prepare guest's cart and purchase details
        String shippingAddress = "123 Guest Street";
        String contactInfo = "guest@example.com";
        ShoppingCart guestCart = guestUser.getShoppingCart();
        //Step 7: Attempt to execute the purchase 
        ApiResponse<Purchase> purchaseResponse = purchaseService.executePurchase("guest", guestCart, shippingAddress, contactInfo);
        //Step 8: Assert that the purchase failed due to payment
        assertFalse(purchaseResponse.isSuccess(), "Expected purchase to fail due to payment issue");
        assertTrue(purchaseResponse.getError().toLowerCase().contains("payment"), "Expected failure due to payment issue, but got: " + purchaseResponse.getError()); 
    }
}  