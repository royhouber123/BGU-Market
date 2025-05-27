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

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class GuestTests extends AcceptanceTestBase {

    private final String MANAGER1 = "manager1";
    private final String MANAGER2 = "manager2";
    private final String MANAGER3 = "manager3";
    private final String MANAGER_PASSWORD = "1234";

    private final String GUEST = "guest";
    private final String SHIPPING_ADDRESS = "123 Guest Street";
    private final String CONTACT_INFO = "guest@example.com";

    private String storeId;

    @BeforeEach
    void setUp() throws Exception {        
        userService.register(MANAGER1, MANAGER_PASSWORD);
        userService.register(MANAGER2, MANAGER_PASSWORD);
        userService.register(MANAGER3, MANAGER_PASSWORD);
        userService.register(GUEST);
        storeId = storeService.createStore("store1", MANAGER1).getData().storeId();
    }


    @Test
    void guest_enters_system_initializes_cart() { 
        //Step 1: Retrieve the user repository and ensure it was retrieved successfully
        ApiResponse<IUserRepository> repoResp = userService.getUserRepository();
        assertTrue(repoResp.isSuccess(), "Failed to retrieve user repository: " + repoResp.getError());
        //Step 2: Extract the actual repository object from the response
        IUserRepository userRepository = repoResp.getData();
        //Step 3: Get the shopping cart for the guest user
        ShoppingCart cart = userRepository.getCart(GUEST);
        //Step 4: Verify that the cart exists
        assertNotNull(cart, "Guest's shopping cart should be initialized");
        //Step 5: Verify that the cart is initially empty (no store bags)
        assertTrue(cart.getAllStoreBags().isEmpty(), "Guest's shopping cart should be empty on registration");
    }

    @Test
    void guest_exits_system_cart_deleted() {
        //Step 1: Retrieve the user repository and ensure it was retrieved successfully
        ApiResponse<IUserRepository> repoResp = userService.getUserRepository();
        assertTrue(repoResp.isSuccess(), "Failed to retrieve user repository: " + repoResp.getError());
        //Step 2: Extract the actual repository object from the response
        IUserRepository userRepository = repoResp.getData();
        //Step 3: Get the guest's cart to verify it was initialized
        ShoppingCart cart = userRepository.getCart(GUEST);
        assertNotNull(cart, "Cart should exist after guest registers");
        //Step 4: Simulate guest leaving the system (e.g., closing the browser)
        ApiResponse<Void> deleteResponse = userService.deleteUser(GUEST);
        assertTrue(deleteResponse.isSuccess(), "Failed to delete guest user: " + deleteResponse.getError());
        //Step 5: Attempt to access the guest's cart again — should throw an exception
        assertThrows(RuntimeException.class, () -> {
                userService.getUserRepository().getData().getCart(GUEST);
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
    void guest_login_with_valid_credentials() {
        //Step 1: Login with valid credentials (manager1 was registered in @BeforeEach)
        ApiResponse<AuthToken> loginResponse = authService.login(MANAGER1, MANAGER_PASSWORD);
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
        ApiResponse<AuthToken> loginResponse = authService.login(MANAGER1, "wrongPassword");
        //Step 2: Assert that login failed and contains an appropriate error message
        assertFalse(loginResponse.isSuccess(), "Login should fail with wrong password");
        assertNotNull(loginResponse.getError(), "Error message should be provided");
    }

    @Test
    void guest_gets_stores_and_product_info_when_available() { 
        //Step 1: Each manager creates a store
        String storeId1 = storeService.createStore("MyStore", MANAGER1).getData().storeId();
        String storeId2 = storeService.createStore("AnotherStore", MANAGER2).getData().storeId();
        //Step 2: Each store gets a product
        storeService.addNewListing(
            "manager1",
            storeId1,
            "p1",
            "Blue Notebook",
            "Stationery",
            "A ruled blue notebook",
            10,
            15.0
        ).getData();
        storeService.addNewListing(
            "manager2",
            storeId2,
            "p2",
            "Red Pencil",
            "Stationery",
            "A bright red pencil",
            20,
            2.0
        ).getData();
        //Step 3: Call the method to retrieve store and product info
        ApiResponse<List<Map<String, Object>>> response = storeService.getInformationAboutStoresAndProducts();
        assertTrue(response.isSuccess(), "Failed to retrieve store and product information: " + response.getError());
        //Step 4: Extract and validate store data
        List<Map<String, Object>> storeInfo = response.getData();
        assertFalse(storeInfo.isEmpty(), "Expected at least one store in the result");
        //Step 5: Check that both stores are included
        boolean store1Found = storeInfo.stream().anyMatch(storeData -> {
            Map<String, Object> store = (Map<String, Object>) storeData.get("store");
            return "MyStore".equals(store.get("storeName"));
        });
        boolean store2Found = storeInfo.stream().anyMatch(storeData -> {
            Map<String, Object> store = (Map<String, Object>) storeData.get("store");
            return "AnotherStore".equals(store.get("storeName"));
        });
        assertTrue(store1Found, "Expected to find 'MyStore' in the returned data");
        assertTrue(store2Found, "Expected to find 'AnotherStore' in the returned data");
        //Step 6: Check that both products are listed
        boolean product1Found = storeInfo.stream()
            .flatMap(storeData -> ((List<Listing>) storeData.get("listings")).stream())
            .anyMatch(listing -> listing.getProductName().equals("Blue Notebook"));
        boolean product2Found = storeInfo.stream()
            .flatMap(storeData -> ((List<Listing>) storeData.get("listings")).stream())
            .anyMatch(listing -> listing.getProductName().equals("Red Pencil"));
        assertTrue(product1Found, "Expected to find the product 'Blue Notebook' in the returned listings");
        assertTrue(product2Found, "Expected to find the product 'Red Pencil' in the returned listings");
    }

    @Test
    void guest_gets_store_info_when_no_stores_active() { 
        //Step 1: Close the store using MANAGER1 ID from setup
        ApiResponse<String> closeStoreResponse = storeService.closeStore(storeId, MANAGER1);
        assertTrue(closeStoreResponse.isSuccess(), "Store closure failed");
        //Step 2: Guest requests store info
        ApiResponse<List<Map<String, Object>>> infoResponse = storeService.getInformationAboutStoresAndProducts();
        assertTrue(infoResponse.isSuccess(), "Failed to get store information");
        //Step 3: Assert no active stores are returned
        List<Map<String, Object>> data = infoResponse.getData();
        assertNotNull(data, "Returned data should not be null");
        assertTrue(data.isEmpty(), "Expected no active stores, but some were returned");
    }

    @Test
    void guest_gets_store_info_when_store_has_no_products() { 
        //Step 1: Guest requests information about stores and their products
        ApiResponse<List<Map<String, Object>>> infoResponse = storeService.getInformationAboutStoresAndProducts();
        assertTrue(infoResponse.isSuccess(), "Failed to retrieve store information");
        //Step 2: Check that at least one store is returned
        List<Map<String, Object>> data = infoResponse.getData();
        assertNotNull(data, "Response data should not be null");
        assertFalse(data.isEmpty(), "Expected at least one store");
        //Step 3: Find the store with the expected ID and check its listings
        boolean storeFoundWithNoProducts = data.stream()
            .anyMatch(storeData -> {
                Map<String, Object> store = (Map<String, Object>) storeData.get("store");
                return store.get("storeID").equals(storeId) && ((List<Listing>) storeData.get("listings")).isEmpty();
            });
        assertTrue(storeFoundWithNoProducts, "Expected the store to have no products listed");
    }

    @Test
    void guest_can_search_products_across_all_stores_by_keyword() {
        //Step 1: Create 3 stores, each owned by a different manager
        String storeId1 = storeService.createStore("StoreA", MANAGER1).getData().storeId();
        String storeId2 = storeService.createStore("StoreB", MANAGER2).getData().storeId();
        String storeId3 = storeService.createStore("StoreC", MANAGER3).getData().storeId();
        //Step 2: Add notebook to first store
        storeService.addNewListing(MANAGER1, storeId1, "p1", "Notebook Classic", "Stationery","Ruled notebook", 10, 15.0);
        //Step 3: Add notebook to second store
        storeService.addNewListing(MANAGER2, storeId2, "p2", "Notebook Deluxe", "Stationery","Premium notebook with hard cover", 5, 25.0);
        //Step 4: Add pencil to third store
        storeService.addNewListing(MANAGER3, storeId3, "p3", "Yellow Pencil", "Stationery","HB classic pencil", 30, 2.5);
        //Step 5: Search for "note" keyword (expect 2 results)
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
        //Step 6: Search for "pencil" keyword (expect 1 result)
        String keyword2 = "pencil";
        ApiResponse<List<Listing>> pencilSearchResponse = productService.searchByProductName(keyword2);
        assertTrue(pencilSearchResponse.isSuccess(), "Failed to search products by keyword: " + keyword2);
        List<Listing> pencilResults = pencilSearchResponse.getData();
        assertEquals(1, pencilResults.size(), "Expected exactly 1 pencil product");
        assertTrue(
            pencilResults.get(0).getProductName().toLowerCase().contains(keyword2),
            "Pencil product name should contain the keyword 'pencil'"
        );
    }


    @Test
    void guest_search_returns_empty_when_no_matches() {
        //Step 1: Create two stores using pre-registered managers
        String storeId1 = storeService.createStore("StoreA", MANAGER1).getData().storeId();
        String storeId2 = storeService.createStore("StoreB", MANAGER2).getData().storeId();
        //Step 2: Add products to the stores
        storeService.addNewListing(MANAGER1, storeId1, "p1", "Notebook", "Stationery", "Simple ruled notebook", 10, 12.5);
        storeService.addNewListing(MANAGER2, storeId2, "p2", "Pencil Case", "Stationery", "Blue fabric pencil case", 8, 9.99);
        //Step 3: Search for a keyword that does not exist
        String keyword = "unicorn-rainbow-sandwich";
        ApiResponse<List<Listing>> searchResp = productService.searchByProductName(keyword);
        assertTrue(searchResp.isSuccess(), "Search failed: " + searchResp.getError());
        List<Listing> results = searchResp.getData();
        //Step 4: Assert that no results were found
        assertTrue(results.isEmpty(), "Expected no products to match a completely unrelated keyword");
    }


    @Test
    void guest_searches_in_specific_store_exists() {
        //Step 1: Add two notebook-related products to the setup-created store
        storeService.addNewListing(MANAGER1, storeId, "n1", "Notebook Classic", "Stationery", "Basic notebook", 10, 12.5);
        storeService.addNewListing(MANAGER1, storeId, "n2", "Notebook Pro", "Stationery", "Premium notebook", 8, 18.0);
        //Step 2: Add an unrelated product
        storeService.addNewListing(MANAGER1, storeId, "n3", "Marker Red", "Stationery", "Permanent red marker", 5, 4.0);
        //Step 3: Search for the keyword "note" in the store
        String keyword = "note";
        ApiResponse<List<Listing>> searchResp = productService.searchInStoreByName(storeId, keyword);
        assertTrue(searchResp.isSuccess(), "Search in store failed: " + searchResp.getError());
        List<Listing> results = searchResp.getData();
        //Step 4: Validate that only notebook-related products are returned
        assertEquals(2, results.size(), "Expected 2 notebook products in StoreAlpha");
        for (Listing listing : results) {
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
        //Step 1: Add unrelated products to the store created in setup
        storeService.addNewListing(MANAGER1, storeId, "p1", "Stapler", "Office Supplies", "Standard metal stapler", 5, 12.0);
        storeService.addNewListing(MANAGER1, storeId, "p2", "Paper Clips", "Office Supplies", "Pack of 100 clips", 10, 3.0);
        //Step 2: Search for a product name that doesn't exist in the store
        String keyword = "notebook";
        ApiResponse<List<Listing>> searchResp = productService.searchInStoreByName(storeId, keyword);
        //Step 3: Assert the search was successful and returned no matching results
        assertTrue(searchResp.isSuccess(), "Search operation failed unexpectedly: " + searchResp.getError());
        assertTrue(searchResp.getData().isEmpty(), "Expected no results when no products match the keyword");
    }


    @Test 
    void guest_adds_product_to_cart_valid() {
        //Step 1: Retrieve the user repository and get the User object
        IUserRepository userRep = userService.getUserRepository().getData();
        User guest=userRep.findById(GUEST);
        //Step 2: Generate a token for the guest and set it as the current mock token
        String token = authService.generateToken(guest).getData();
        TokenUtils.setMockToken(token);
        //Step 3: Add a new product listing (Gvina) to the store as the manager1
        String listingIdOfGvina=storeService.addNewListing(MANAGER1, storeId, "123", "Gvina", "food", "Gvina", 10, 5.0).getData();
        //Step 4: Guest adds 2 units of the product to their cart
        ApiResponse<Void> addProductResponse = userService.addProductToCart(storeId, listingIdOfGvina, 2);
        assertTrue(addProductResponse.isSuccess(), "Failed to add product to cart: " + addProductResponse.getError());
        //Step 5: Retrieve the guest's shopping cart and verify product quantity
        ShoppingCart cart = userRep.getCart(GUEST); 
        assertEquals(2, cart.getStoreBag(storeId).getProductQuantity(listingIdOfGvina),
            "Expected quantity of 'gvina' in store bag should be 2");
        //Step 6: Guest removes the same quantity of the product from the cart
        ApiResponse<Void> removeProductResponse = userService.removeProductFromCart(storeId, listingIdOfGvina, 2);
        assertTrue(removeProductResponse.isSuccess(), "Failed to remove product from cart: " + removeProductResponse.getError());
        //Step 7: Verify that the store bag was removed (cart is empty for that store)
        assertNull(cart.getStoreBag(storeId),
            "Store bag should be null after removing all quantities of the product");
        //Step 8: Clear the mock token
        TokenUtils.clearMockToken();
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
        //Step 1: Add a new listing (Notebook) to the existing store
        int quantity=5;
        String listingId=storeService.addNewListing(MANAGER1, storeId, "p1", "Notebook", "writing", "Simple notebook", quantity, 25.0).getData();
        //Step 2: Stub the payment and shipment services to simulate success
        when(paymentService.processPayment(anyString())).thenReturn(ApiResponse.ok(true)); 
        when(shipmentService.ship(anyString(), anyString(), anyDouble())).thenReturn(ApiResponse.ok("SHIP123")); 
        //Step 3: Generate a token for GUEST and set it as the current token
        IUserRepository userRep = userService.getUserRepository().getData();
        User guestUser=userRep.findById(GUEST); 
        String token = authService.generateToken(guestUser).getData();
        TokenUtils.setMockToken(token);
        //Step 6: Add one notebook to the guest's shopping cart
        ApiResponse<Void> addProductResponse = userService.addProductToCart(storeId, listingId, 1);
        assertTrue(addProductResponse.isSuccess(), "Failed to add product to cart: " + addProductResponse.getError());
        ShoppingCart guestCart = guestUser.getShoppingCart();
        //Step 7: Execute the purchase
        ApiResponse<Purchase> purchaseResponse = purchaseService.executePurchase(GUEST, guestCart, SHIPPING_ADDRESS, CONTACT_INFO);
        assertTrue(purchaseResponse.isSuccess(), "Purchase failed: " + purchaseResponse.getError());
        //Step 8: Validate that the purchase object is correct and belongs to the guest
        Purchase purchase = purchaseResponse.getData();
        assertNotNull(purchase, "Purchase should not be null");
        assertEquals(GUEST, purchase.getUserId(), "Buyer ID should be guest");
        //Step 9: Verify that the cart is empty after successful purchase
        User refreshedGuest = userService.getUserRepository().getData().findById(GUEST);
        ShoppingCart refreshedCart = refreshedGuest.getShoppingCart();
        assertTrue(refreshedCart.getAllStoreBags().isEmpty(), "Shopping cart should be empty after purchase");
        //Step 10: Verify that the store stock was reduced by the purchased quantity
        int remainingStock = storeService.getListingRepository().getData().getListingById(listingId).getQuantityAvailable();
        assertEquals(quantity - 1, remainingStock, "Stock should decrease by purchased amount");
        //Step 11: Clear the mock token
        TokenUtils.clearMockToken();
    }

    @Test 
    void guest_purchasing_cart_fails_due_to_stock() { //there is a stock when added to bag but not when purchase???
        //Step 1: Add a product to the existing store with limited stock (5 units)
        int quantity=5;
        String listingId=storeService.addNewListing(MANAGER1, storeId, "p1", "Notebook", "writing", "Simple notebook", quantity, 25.0).getData();
        //Step 2: Stub the payment and shipment services to simulate success (even though stock will fail)
        when(paymentService.processPayment(anyString())).thenReturn(ApiResponse.ok(true)); 
        when(shipmentService.ship(anyString(), anyString(), anyDouble())).thenReturn(ApiResponse.ok("SHIP123"));
        //Step 3: Generate a token for GUEST and set it as the current token
        IUserRepository userRep = userService.getUserRepository().getData();
        User guestUser=userRep.findById(GUEST); 
        String token = authService.generateToken(guestUser).getData();
        TokenUtils.setMockToken(token);
        //Step 4: Add more items to the cart than are available in stock (6 > 5)
        ApiResponse<Void> addProductResponse = userService.addProductToCart(storeId, listingId, quantity + 1); 
        assertTrue(addProductResponse.isSuccess(), "Failed to add product to cart: " + addProductResponse.getError());
        ShoppingCart guestCart = guestUser.getShoppingCart(); 
        //Step 5: Attempt to execute purchase — should fail due to insufficient stock
        ApiResponse<Purchase> response = purchaseService.executePurchase(GUEST, guestCart, SHIPPING_ADDRESS, CONTACT_INFO);
        //Step 6: Validate that the purchase failed and the error message is stock-related
        assertFalse(response.isSuccess(), "Expected purchase to fail due to stock limit");
        assertTrue(response.getError().toLowerCase().contains("stock"), "Expected stock-related error, but got: " + response.getError());
        //Step 7: Clear the mock token to clean up
        TokenUtils.clearMockToken();
    }

    @Test 
    void guest_purchasing_cart_fails_due_to_payment_restore_stock() { //after payment failes- the stock needs to be restored
        //Step 1: Add a product listing ("Notebook") with a stock of 5 to the existing store
        int quantity=5;
        String listingId=storeService.addNewListing(MANAGER1, storeId, "p1", "Notebook", "writing", "Simple notebook", quantity, 25.0).getData();
        //Step 2: Stub services: simulate payment failure but allow shipment (to isolate payment failure)
        when(paymentService.processPayment(anyString())).thenReturn(ApiResponse.fail("Simulated payment failure"));
        when(shipmentService.ship(anyString(), anyString(), anyDouble())).thenReturn(ApiResponse.ok("SHIP123"));
        //Step 3: Generate a token for GUEST and set it as the current token
        IUserRepository userRep = userService.getUserRepository().getData();
        User guestUser=userRep.findById(GUEST); 
        String token = authService.generateToken(guestUser).getData();
        TokenUtils.setMockToken(token);
        //Step 4: Add 1 unit of the notebook to the guest's shopping cart
        ApiResponse<Void> addProductResponse = userService.addProductToCart(storeId, listingId, 1); 
        assertTrue(addProductResponse.isSuccess(), "Failed to add product to cart: " + addProductResponse.getError()); 
        ShoppingCart guestCart = guestUser.getShoppingCart();
        //Step 5: Attempt to execute purchase — should fail due to payment error
        ApiResponse<Purchase> purchaseResponse = purchaseService.executePurchase(GUEST, guestCart, SHIPPING_ADDRESS, CONTACT_INFO);
        //Step 6: Verify that the purchase failed and the error is payment-related
        assertFalse(purchaseResponse.isSuccess(), "Expected purchase to fail due to payment issue");
        assertTrue(purchaseResponse.getError().toLowerCase().contains("payment"), "Expected failure due to payment issue, but got: " + purchaseResponse.getError());
        int remainingStock = storeService.getListingRepository().getData().getListingById(listingId).getQuantityAvailable();
        //Step 7: Verify that the stock was restored to its original amount (5)
        assertEquals(quantity, remainingStock, "Stock should be restored to original amount after payment failure");
        //Step 8: Verify that the guest's shopping cart is still intact (not cleared)
        ShoppingCart refreshedCart = userRep.getCart(GUEST);
        assertNotNull(refreshedCart, "Shopping cart should still exist after failed purchase");
        assertEquals(1, refreshedCart.getStoreBag(storeId).getProductQuantity(listingId),
            "Shopping cart should still contain the notebook after payment failure");
        //Step 7: Clear the mock token to clean up
        TokenUtils.clearMockToken(); 
    }
}  