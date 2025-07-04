package tests;

import support.AcceptanceTestBase;
import utils.ApiResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import market.application.AuthService.AuthToken;
import market.domain.store.Store;
import market.domain.purchase.Purchase;
import market.domain.store.Listing;
import market.domain.store.Policies.DiscountPolicy;
import market.domain.store.Policies.Discounts.CouponDiscountPolicy;
import market.domain.store.Policies.Discounts.DiscountCombinationType;
import market.domain.store.Policies.Discounts.DiscountPolicyFactory;
import market.domain.store.Policies.Discounts.DiscountTargetType;
import market.domain.user.IUserRepository;
import market.domain.user.ShoppingCart;
import market.domain.user.User;
import market.dto.PolicyDTO;
import market.middleware.TokenUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
        storeId = storeService.createStore("store1", MANAGER1).storeId();
    }


    @Test
    void guest_enters_system_initializes_cart() { 
        try {
            //Step 1: Retrieve the user repository and ensure it was retrieved successfully
            IUserRepository repoResp = userService.getUserRepository();
            //Step 2: Get the shopping cart for the guest user
            ShoppingCart cart = repoResp.getCart(GUEST);
            //Step 3: Verify that the cart exists
            assertNotNull(cart, "Guest's shopping cart should be initialized");
            //Step 4: Verify that the cart is initially empty (no store bags)
            assertTrue(cart.getAllStoreBags().isEmpty(), "Guest's shopping cart should be empty on registration");
        } catch (Exception e) {
            fail("Failed to retrieve user repository: " + e.getMessage());  
        }
    }

    @Test
    void guest_exits_system_cart_deleted() {
        try{
            //Step 1: Retrieve the user repository and ensure it was retrieved successfully
            IUserRepository repoResp = userService.getUserRepository();
            //Step 2: Get the guest's cart to verify it was initialized
            ShoppingCart cart = repoResp.getCart(GUEST);
            assertNotNull(cart, "Cart should exist after guest registers");
        } catch (Exception e) {
            fail("Failed to retrieve user repository: " + e.getMessage());
        }
        try {
            //Step 5: Simulate guest leaving the system (e.g., closing the browser)
            userService.deleteUser(GUEST);
        } catch (Exception e) {
            fail("Failed to delete guest user: " + e.getMessage());
        }
        //Step 4: Attempt to access the guest's cart again — should throw an exception
        assertThrows(RuntimeException.class, () -> {userService.getUserRepository().getCart(GUEST);}, "Expected cart retrieval to fail after guest deletion");
    }

    @Test
    void guest_registers_with_valid_details() {
        //Step 1: Attempt to register a new subscriber with valid credentials
        String username = "new_subscriber";
        String password = "1234";
        try {
            userService.register(username, password);
        } catch (Exception e) {
            fail("Registration failed: " + e.getMessage());
        }
        try{
            //Step 2: Retrieve the user repository to verify the user was registered successfully
            IUserRepository repoResp = userService.getUserRepository();
            //Step 4: Check that the user exists in the repository
            User user = repoResp.findById(username);
            assertNotNull(user, "User should exist after registration");
        } catch (Exception e) {
            fail("Failed to retrieve user repository: " + e.getMessage());
        }
    }

    @Test
    void guest_login_with_valid_credentials() {
        try {
            //Step 1: Login with valid credentials (manager1 was registered in @BeforeEach)
            AuthToken loginResponse = authService.login(MANAGER1, MANAGER_PASSWORD);
            //Step 2: Retrieve the token from the login response
            String token = loginResponse.token();
            //Step 3: Ensure the token is valid and not null
            assertNotNull(token, "Token should not be null after successful login");
        } catch (Exception e) {
            fail("Login failed: " + e.getMessage());
        }
    }

    @Test
    void guest_login_with_wrong_password() {
        try {
            //Step 1: Attempt to login with incorrect password
            AuthToken loginResponse = authService.login(MANAGER1, "wrongPassword");
            //Step 2: Assert that login failed and contains an appropriate error message
            fail("Login should have failed with wrong password, but succeeded: " + loginResponse);
        } catch (Exception e) {
            //Step 3: If an exception is thrown, it indicates the login failed as expected
            //This is acceptable behavior for this test case
        }
    }

    @Test
    void guest_gets_stores_and_product_info_when_available() {
        try {
            //Step 1: Each manager creates a store
            String storeId1 = storeService.createStore("MyStore", MANAGER1).storeId();
            String storeId2 = storeService.createStore("AnotherStore", MANAGER2).storeId();
            //Step 2: Each store gets a product
            storeService.addNewListing(
                "manager1",
                storeId1,
                "p1",
                "Blue Notebook",
                "Stationery",
                "A ruled blue notebook",
                10,
                15.0,
                "REGULAR"
            );
            storeService.addNewListing(
                "manager2",
                storeId2,
                "p2",
                "Red Pencil",
                "Stationery",
                "A bright red pencil",
                20,
                2.0,
                "REGULAR"
            );
        } catch (Exception e) {
            fail("Failed to set up stores and products: " + e.getMessage());
        }
        try {
            //Step 3: Call the method to retrieve store and product info
            List<Map<String, Object>> storeInfo = storeService.getInformationAboutStoresAndProducts();
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
            .flatMap(storeData -> {
                @SuppressWarnings("unchecked")
                List<Listing> listings = (List<Listing>) storeData.get("listings");
                return listings.stream();
            })
            .anyMatch(listing -> listing.getProductName().equals("Blue Notebook"));
            boolean product2Found = storeInfo.stream()
            .flatMap(storeData -> {
                @SuppressWarnings("unchecked")
                List<Listing> listings = (List<Listing>) storeData.get("listings");
                return listings.stream();
            })
            .anyMatch(listing -> listing.getProductName().equals("Red Pencil"));
            assertTrue(product1Found, "Expected to find the product 'Blue Notebook' in the returned listings");
            assertTrue(product2Found, "Expected to find the product 'Red Pencil' in the returned listings");
        } catch (Exception e) {
            fail("Failed to retrieve store and product information: " + e.getMessage());
        }
    }

    @Test
    void guest_gets_store_info_when_no_stores_active() {
        try {
            // Step 1: Close the store using MANAGER1 ID from setup
            storeService.closeStore(storeId, MANAGER1);
        } catch (Exception e) {
            fail("Failed to close store: " + e.getMessage());
        }

        try {
            // Step 2: Guest requests store info
            List<Map<String, Object>> infoResponse = storeService.getInformationAboutStoresAndProducts();

            // Step 3: Assert all returned stores are inactive
            assertNotNull(infoResponse, "Returned data should not be null");

            for (Map<String, Object> entry : infoResponse) {
                Map<String, Object> store = (Map<String, Object>) entry.get("store");
                Boolean isActive = (Boolean) store.get("isActive");
                assertFalse(isActive, "Expected store to be inactive, but found active store");
            }

        } catch (Exception e) {
            fail("Failed to retrieve store information: " + e.getMessage());
        }
    }

    @Test
    void guest_gets_store_info_when_store_has_no_products() {
        try {
            //Step 1: Guest requests information about stores and their products
            List<Map<String, Object>> data = storeService.getInformationAboutStoresAndProducts();
            //Step 2: Check that at least one store is returned
            assertNotNull(data, "Response data should not be null");
            assertFalse(data.isEmpty(), "Expected at least one store");
                    //Step 3: Find the store with the expected ID and check its listings
            boolean storeFoundWithNoProducts = data.stream()
                .anyMatch(storeData -> {
                    Map<String, Object> store = (Map<String, Object>) storeData.get("store");
                    return store.get("storeID").equals(storeId) && ((List<Listing>) storeData.get("listings")).isEmpty();
                });
            assertTrue(storeFoundWithNoProducts, "Expected the store to have no products listed");
        } catch (Exception e) {
            fail("Failed to retrieve store information: " + e.getMessage());
        }
    }

    @Test
    void guest_can_search_products_across_all_stores_by_keyword() {
        try {
            //Step 1: Create 3 stores, each owned by a different manager
            String storeId1 = storeService.createStore("StoreA", MANAGER1).storeId();
            String storeId2 = storeService.createStore("StoreB", MANAGER2).storeId();
            String storeId3 = storeService.createStore("StoreC", MANAGER3).storeId();
            //Step 2: Add notebook to first store
            storeService.addNewListing(MANAGER1, storeId1, "p1", "Notebook Classic", "Stationery","Ruled notebook", 10, 15.0, "REGULAR");
            //Step 3: Add notebook to second store
            storeService.addNewListing(MANAGER2, storeId2, "p2", "Notebook Deluxe", "Stationery","Premium notebook with hard cover", 5, 25.0, "REGULAR");
            //Step 4: Add pencil to third store
            storeService.addNewListing(MANAGER3, storeId3, "p3", "Yellow Pencil", "Stationery","HB classic pencil", 30, 2.5, "REGULAR");
            //Step 5: Search for "note" keyword (expect 2 results)
            String keyword1 = "note";
            List<Listing> noteResults = productService.searchByProductName(keyword1);
            assertEquals(2, noteResults.size(), "Expected exactly 2 notebook products");
            for (Listing listing : noteResults) {
                assertTrue(
                    listing.getProductName().toLowerCase().contains(keyword1),
                    "Each product name should contain the keyword 'note'"
                );
            }
            //Step 6: Search for "pencil" keyword (expect 1 result)
            String keyword2 = "pencil";
            List<Listing> pencilResults = productService.searchByProductName(keyword2);
            assertEquals(1, pencilResults.size(), "Expected exactly 1 pencil product");
            assertTrue(
                pencilResults.get(0).getProductName().toLowerCase().contains(keyword2),
                "Pencil product name should contain the keyword 'pencil'"
            );
        } catch (Exception e) {
            fail("Failed to search products by keyword " + e.getMessage());
        }
    }


    @Test
    void guest_search_returns_empty_when_no_matches() {
        try {
            //Step 1: Create two stores using pre-registered managers
            String storeId1 = storeService.createStore("StoreA", MANAGER1).storeId();
            String storeId2 = storeService.createStore("StoreB", MANAGER2).storeId();
            //Step 2: Add products to the stores
            storeService.addNewListing(MANAGER1, storeId1, "p1", "Notebook", "Stationery", "Simple ruled notebook", 10, 12.5, "REGULAR");
            storeService.addNewListing(MANAGER2, storeId2, "p2", "Pencil Case", "Stationery", "Blue fabric pencil case", 8, 9.99, "REGULAR");
            //Step 3: Search for a keyword that does not exist
            String keyword = "unicorn-rainbow-sandwich";
            List<Listing> results = productService.searchByProductName(keyword);
            //Step 4: Assert that no results were found
            assertTrue(results.isEmpty(), "Expected no products to match a completely unrelated keyword");
        } catch (Exception e) {
            fail("Search operation failed: " + e.getMessage());
        }
    }


    @Test
    void guest_searches_in_specific_store_exists() {
        try {
            //Step 1: Add two notebook-related products to the setup-created store
            storeService.addNewListing(MANAGER1, storeId, "n1", "Notebook Classic", "Stationery", "Basic notebook", 10, 12.5, "REGULAR");
            storeService.addNewListing(MANAGER1, storeId, "n2", "Notebook Pro", "Stationery", "Premium notebook", 8, 18.0, "REGULAR");
            //Step 2: Add an unrelated product
            storeService.addNewListing(MANAGER1, storeId, "n3", "Marker Red", "Stationery", "Permanent red marker", 5, 4.0, "REGULAR");
            //Step 3: Search for the keyword "note" in the store
            String keyword = "note";
            List<Listing> results = productService.searchInStoreByName(storeId, keyword);
            //Step 4: Validate that only notebook-related products are returned
            assertEquals(2, results.size(), "Expected 2 notebook products in StoreAlpha");
            for (Listing listing : results) {
                assertTrue(
                    listing.getProductName().toLowerCase().contains(keyword),
                    "Product name should contain the keyword 'note'"
                );
            }
        } catch (Exception e) {
            fail("Search operation failed: " + e.getMessage());
        }
    }


    @Test
    void guest_searches_in_specific_store_doesnt_exist() {
        //Step 1: Use an ID for a store that doesn't exist in the system
        String nonExistingStoreId = "9999";
        String keyword = "notebook";
        try {
            //Step 2: Attempt to search for products by keyword in the non-existing store
            List<Listing> searchResp = productService.searchInStoreByName(nonExistingStoreId, keyword);
            assertTrue(searchResp.isEmpty(), "Expected no results from a non-existing store");
        } catch (Exception e) {
            //Step 3: Verify that the search was successful and returned an empty list
            fail("Search operation failed unexpectedly: " + e.getMessage());
        }
    }


    @Test
    void guest_searches_in_specific_store_no_matching_products() {
        try {
            //Step 1: Add unrelated products to the store created in setup
            storeService.addNewListing(MANAGER1, storeId, "p1", "Stapler", "Office Supplies", "Standard metal stapler", 5, 12.0, "REGULAR");
            storeService.addNewListing(MANAGER1, storeId, "p2", "Paper Clips", "Office Supplies", "Pack of 100 clips", 10, 3.0, "REGULAR");
            //Step 2: Search for a product name that doesn't exist in the store
            String keyword = "notebook";
            List<Listing> searchResp = productService.searchInStoreByName(storeId, keyword);
            assertTrue(searchResp.isEmpty(), "Expected no results when no products match the keyword");
        } catch (Exception e) {
            //Step 3: Assert the search was successful and returned no matching results
            fail("Search operation failed unexpectedly: " + e.getMessage());
        }
    }


    @Test 
    void guest_adds_product_to_cart_valid() {
        try {
            //Step 1: Retrieve the user repository and get the User object
            IUserRepository userRep = userService.getUserRepository();
            User guest=userRep.findById(GUEST);
            //Step 2: Generate a token for the guest and set it as the current mock token
            String token = authService.generateToken(guest);
            TokenUtils.setMockToken(token);
            //Step 3: Add a new product listing (Gvina) to the store as the manager1
            String listingIdOfGvina=storeService.addNewListing(MANAGER1, storeId, "123", "Gvina", "food", "Gvina", 10, 5.0, "REGULAR");
            //Step 4: Guest adds 2 units of the product to their cart
            userService.addProductToCart(storeId, listingIdOfGvina, 2);
            //Step 5: Retrieve the guest's shopping cart and verify product quantitys
            ShoppingCart cart = userRep.getCart(GUEST);
            assertEquals(2, cart.getStoreBag(storeId).getProductQuantity(listingIdOfGvina),
            "Expected quantity of 'gvina' in store bag should be 2");
            //Step 6: Guest removes the same quantity of the product from the cart
            userService.removeProductFromCart(storeId, listingIdOfGvina, 2);
            //Step 7: Verify that the store bag was removed (cart is empty for that store)
            assertNull(cart.getStoreBag(storeId),
                "Store bag should be null after removing all quantities of the product");
            //Step 8: Clear the mock token
            TokenUtils.clearMockToken();
        } catch (Exception e) {
            fail(e.getMessage());
        }
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
        try {
            //Step 1: Add a new listing (Notebook) to the existing store
            int quantity=5;
            String listingId=storeService.addNewListing(MANAGER1, storeId, "p1", "Notebook", "writing", "Simple notebook", quantity, 25.0, "REGULAR");
            //Step 2: Stub the payment and shipment services to simulate success
            when(paymentService.processPayment(anyString())).thenReturn(ApiResponse.ok(true)); 
            when(shipmentService.ship(anyString(), anyString(), anyDouble())).thenReturn(ApiResponse.ok("SHIP123")); 
            //Step 3: Generate a token for GUEST and set it as the current token
            IUserRepository userRep = userService.getUserRepository();
            User guestUser=userRep.findById(GUEST); 
            String token = authService.generateToken(guestUser);
            TokenUtils.setMockToken(token);
            //Step 6: Add one notebook to the guest's shopping cart
            userService.addProductToCart(storeId, listingId, 1);
            ShoppingCart guestCart = guestUser.getShoppingCart();
            //Step 7: Execute the purchase
            Purchase purchase = purchaseService.executePurchase(GUEST, guestCart, SHIPPING_ADDRESS, CONTACT_INFO);
            //Step 8: Validate that the purchase object is correct and belongs to the guest
            assertNotNull(purchase, "Purchase should not be null");
            assertEquals(GUEST, purchase.getUserId(), "Buyer ID should be guest");
            //Step 9: Verify that the cart is empty after successful purchase
            User refreshedGuest = userService.getUserRepository().findById(GUEST);
            ShoppingCart refreshedCart = refreshedGuest.getShoppingCart();
            assertTrue(refreshedCart.getAllStoreBags().isEmpty(), "Shopping cart should be empty after purchase");
            //Step 10: Verify that the store stock was reduced by the purchased quantity
            int remainingStock = storeService.getListingRepository().getListingById(listingId).getQuantityAvailable();
            assertEquals(quantity - 1, remainingStock, "Stock should decrease by purchased amount");
            //Step 11: Clear the mock token
            TokenUtils.clearMockToken();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test 
    void guest_purchasing_cart_fails_due_to_stock() { //there is a stock when added to bag but not when purchase???
        try {
            //Step 1: Add a product to the existing store with limited stock (5 units)
            int quantity=5;
            String listingId=storeService.addNewListing(MANAGER1, storeId, "p1", "Notebook", "writing", "Simple notebook", quantity, 25.0, "REGULAR");
            //Step 2: Stub the payment and shipment services to simulate success (even though stock will fail)
            when(paymentService.processPayment(anyString())).thenReturn(ApiResponse.ok(true)); 
            when(shipmentService.ship(anyString(), anyString(), anyDouble())).thenReturn(ApiResponse.ok("SHIP123"));
            //Step 3: Generate a token for GUEST and set it as the current token
            IUserRepository userRep = userService.getUserRepository();
            User guestUser=userRep.findById(GUEST); 
            String token = authService.generateToken(guestUser);
            TokenUtils.setMockToken(token);
            //Step 4: Add more items to the cart than are available in stock (6 > 5)
            userService.addProductToCart(storeId, listingId, quantity + 1); 
            ShoppingCart guestCart = guestUser.getShoppingCart();
            //Step 5: Attempt to execute purchase — should fail due to insufficient stock
            purchaseService.executePurchase(GUEST, guestCart, SHIPPING_ADDRESS, CONTACT_INFO);
            fail("Expected purchase to fail due to insufficient stock, but it succeeded");
        } catch (Exception e) {
            //should fail due to insufficient stock
            assertTrue(e.getMessage().contains("stock"), "Expected stock-related error, but got: " + e.getMessage());
        }
        //Step 7: Clear the mock token to clean up
        TokenUtils.clearMockToken();
    }

    @Test 
    void guest_purchasing_cart_fails_due_to_payment_restore_stock() { //after payment failes- the stock needs to be restored
        //Step 1: Add a product listing ("Notebook") with a stock of 5 to the existing store
        int quantity=5;
        String listingId=storeService.addNewListing(MANAGER1, storeId, "p1", "Notebook", "writing", "Simple notebook", quantity, 25.0, "REGULAR");
        //Step 2: Stub services: simulate payment failure but allow shipment (to isolate payment failure)
        when(paymentService.processPayment(anyString())).thenReturn(ApiResponse.fail("Simulated payment failure"));
        when(shipmentService.ship(anyString(), anyString(), anyDouble())).thenReturn(ApiResponse.ok("SHIP123"));
        //Step 3: Generate a token for GUEST and set it as the current token
        IUserRepository userRep = userService.getUserRepository();
        User guestUser=userRep.findById(GUEST); 
        String token = authService.generateToken(guestUser);
        TokenUtils.setMockToken(token);
        //Step 4: Add 1 unit of the notebook to the guest's shopping cart
        userService.addProductToCart(storeId, listingId, 1); 
        ShoppingCart guestCart = guestUser.getShoppingCart();
        try {
            //Step 5: Attempt to execute purchase — should fail due to payment error
            Purchase purchaseResponse = purchaseService.executePurchase(GUEST, guestCart, SHIPPING_ADDRESS, CONTACT_INFO);
            //Step 6: Verify that the purchase failed and the error is payment-related
            fail("Expected purchase to fail due to payment issue, but it succeeded");
        } catch (Exception e) {
            //Step 6: If an exception is thrown, it indicates the purchase failed as expected
            //This is acceptable behavior for this test case
            assertTrue(e.getMessage().toLowerCase().contains("payment"), "Expected payment-related error, but got: " + e.getMessage());
            int remainingStock = storeService.getListingRepository().getListingById(listingId).getQuantityAvailable();
            //Step 7: Verify that the stock was restored to its original amount (5)
            assertEquals(quantity, remainingStock, "Stock should be restored to original amount after payment failure");
            //Step 8: Verify that the guest's shopping cart is still intact (not cleared)
            ShoppingCart refreshedCart = userRep.getCart(GUEST);
            assertNotNull(refreshedCart, "Shopping cart should still exist after failed purchase");
            assertEquals(1, refreshedCart.getStoreBag(storeId).getProductQuantity(listingId),
                "Shopping cart should still contain the notebook after payment failure");
        }
        //Step 7: Clear the mock token to clean up
        TokenUtils.clearMockToken(); 
    }

    @Test
    void guest_cart_applies_percentage_discount_correctly() {
        // Step 1: Create a discount request for the "food" category (e.g., 10% discount on this category)
        PolicyDTO.AddDiscountRequest discountRequest = new PolicyDTO.AddDiscountRequest(
            "PERCENTAGE",   // Discount type
            "CATEGORY",     // Scope of the discount (category)
            "food",         // Category
            10.0,           // Discount value (10%)
            null,            // No coupon code
            null,            // No additional conditions
            null,            // No sub-discounts
            null             // No combination of discounts
        );
        try {
            // Step 2: Add the discount to the store for the guest
            boolean addDiscountResponse = storePoliciesService.addDiscount(storeId, MANAGER1, discountRequest);
            // Step 3: Create a shopping cart for the guest and add a product to the cart
            IUserRepository userRep = userService.getUserRepository();
            User guest = userRep.findById(GUEST);
            String token = authService.generateToken(guest);
            TokenUtils.setMockToken(token);  // Set the token for the guest
            String listingIdOfGvina = storeService.addNewListing(MANAGER1, storeId, "123", "Gvina", "food", "Gvina", 10, 5.0, "REGULAR");
            when(paymentService.processPayment(anyString())).thenReturn(ApiResponse.ok(true)); 
            when(shipmentService.ship(anyString(), anyString(), anyDouble())).thenReturn(ApiResponse.ok("SHIP123")); 
            userService.addProductToCart(storeId, listingIdOfGvina, 2);
            // Step 4: Retrieve the guest's shopping cart and ensure the product was added with the correct quantity
            ShoppingCart cart = userRep.getCart(GUEST);
            assertEquals(2, cart.getStoreBag(storeId).getProductQuantity(listingIdOfGvina), "Expected quantity of 'Gvina' in store bag should be 2");
            // Step 5: Execute the guest's purchase
            Purchase purchase = purchaseService.executePurchase(GUEST, cart, SHIPPING_ADDRESS, CONTACT_INFO);
            // Step 6: Get the final price of the purchase
            double totalPrice = purchase.getTotalPrice();
            // Step 7: Calculate the expected price - 5 * 2 = 10, 10% discount = 1
            double expectedPrice = 10 - 1; // Price after 10% discount
            // Step 8: Compare the price paid with the expected price
            assertEquals(expectedPrice, totalPrice, 0.001, "The price paid does not match the expected price with discount");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        // Step 9: Clear the token after the test
        TokenUtils.clearMockToken();
    }    

    @Test
    void guest_cart_applies_coupon_discount_correctly() {
        try {
            // Step 1: Create a coupon discount request with a 5 unit discount for coupon "SAVE5"
            PolicyDTO.AddDiscountRequest couponRequest = new PolicyDTO.AddDiscountRequest(
                "COUPON",      // Discount type
                null,           // No scope (coupon)
                null,           // No scope ID
                5.0,            // Discount value (5)
                "SAVE5",        // Coupon code
                null,           // No additional conditions
                null,           // No sub-discounts
                null            // No combination of discounts
            );

            // Step 2: Add the coupon discount to the store for the guest
            boolean addDiscountResponse = storePoliciesService.addDiscount(storeId, MANAGER1, couponRequest);
            assertTrue(addDiscountResponse, "Failed to add coupon discount: ");
            //submitCoupon("SAVE5"); // Simulate applying the coupon

            // Step 3: Create a shopping cart for the guest and add a product to the cart
            IUserRepository userRep = userService.getUserRepository();
            User guest = userRep.findById(GUEST);
            String token = authService.generateToken(guest);
            TokenUtils.setMockToken(token);

            String listingIdOfGvina = storeService.addNewListing(MANAGER1, storeId, "123", "Gvina", "food", "Gvina", 10, 5.0, "REGULAR");
            when(paymentService.processPayment(anyString())).thenReturn(ApiResponse.ok(true)); 
            when(shipmentService.ship(anyString(), anyString(), anyDouble())).thenReturn(ApiResponse.ok("SHIP123")); 
            userService.addProductToCart(storeId, listingIdOfGvina, 2);
            // Step 4: Retrieve the guest's shopping cart and ensure the product was added with the correct quantity
            ShoppingCart cart = userRep.getCart(GUEST);
            assertEquals(2, cart.getStoreBag(storeId).getProductQuantity(listingIdOfGvina), "Expected quantity of 'Gvina' in store bag should be 2");
            // Step 5: Execute the guest's purchase
            Purchase purchaseResponse = purchaseService.executePurchase(GUEST, cart, SHIPPING_ADDRESS, CONTACT_INFO);
            // Step 6: Get the final price of the purchase
            double totalPrice = purchaseResponse.getTotalPrice();
            // Step 7: Calculate the expected price - 5 * 2 = 10, coupon discount = 5, final price = 5
            double expectedPrice = 10 - 5; // Price after coupon discount
            // Step 8: Compare the price paid with the expected price
            //assertEquals(expectedPrice, totalPrice, 0.001, "The price paid does not match the expected price with coupon discount")
        } catch (Exception e) {
            fail(e.getMessage());
        }
        // Step 9: Clear the token after the test
        TokenUtils.clearMockToken();
    }

    @Test
    void guest_cart_applies_fixed_product_discount_correctly() {
        // 1. Add a product to the store
        String productId = "fixed-test-product";
        String listingId = storeService.addNewListing(
            MANAGER1, storeId, productId, "Fixed Test Product", "Test", "Test product description", 100, 12.0, "REGULAR"
        );
        assertTrue(listingId != null && !listingId.isEmpty());

        // 2. Add fixed discount: $4 off per item
        PolicyDTO.AddDiscountRequest fixedRequest = new PolicyDTO.AddDiscountRequest(
            "FIXED", "PRODUCT", productId, 4.0, null, null, null, null
        );
        
        boolean addDiscountResponse = storePoliciesService.addDiscount(storeId, MANAGER1, fixedRequest);
        assertTrue(addDiscountResponse, "Failed to add fixed discount policy");
        
        // 3. Calculate prices using StoreService
        Map<String, Integer> basket = Map.of(listingId, 3); // 3 items at $12 each = $36 total
    
        // Get original price
        ApiResponse<Double> originalResponse = storeService.getStoreBagPrice(storeId, basket);
        assertTrue(originalResponse.isSuccess(), "Should successfully get original price");
        double originalPrice = originalResponse.getData();
    
        // Get discounted price
        ApiResponse<Double> discountedResponse = storeService.getStoreBagDiscountPrice(storeId, basket);
        assertTrue(discountedResponse.isSuccess(), "Should successfully get discounted price");
        double discountedPrice = discountedResponse.getData();
    
        // Calculate discount
        double discount = originalPrice - discountedPrice;
    
        // 4. Verify: $4 per item × 3 items = $12 total discount
        assertEquals(36.0, originalPrice, 0.01, "Original price should be $36");
        assertEquals(12.0, discountedPrice, 0.01, "Discount should be $12");
        assertEquals(24.0, discount, 0.01, "Final price should be $24");
    }

    @Test
    void guest_cart_applies_fixed_store_discount_correctly() {
        // 1. Add products to the store
        String listingId1 = storeService.addNewListing(
            MANAGER1, storeId, "product1", "Product 1", "Test", "Product 1 description", 100, 15.0, "REGULAR"
        );
        String listingId2 = storeService.addNewListing(
            MANAGER1, storeId, "product2", "Product 2", "Test", "Product 2 description", 100, 10.0, "REGULAR"
        );

        // 2. Add store-wide fixed discount: $8 off entire order
        PolicyDTO.AddDiscountRequest storeDiscountRequest = new PolicyDTO.AddDiscountRequest(
            "FIXED", "STORE", null, 8.0, null, null, null, null
        );
        
        boolean addDiscountResponse = storePoliciesService.addDiscount(storeId, MANAGER1, storeDiscountRequest);
        assertTrue(addDiscountResponse);
        
        // 3. Calculate prices using StoreService
        Map<String, Integer> basket = Map.of(
            listingId1, 1,  // $15
            listingId2, 2   // $20
        ); // Total: $35
        
        // Get original price
        ApiResponse<Double> originalResponse = storeService.getStoreBagPrice(storeId, basket);
        assertTrue(originalResponse.isSuccess());
        double originalPrice = originalResponse.getData();
        
        // Get discounted price
        ApiResponse<Double> discountedResponse = storeService.getStoreBagDiscountPrice(storeId, basket);
        assertTrue(discountedResponse.isSuccess());
        double discountedPrice = discountedResponse.getData();
        
        // Calculate discount
        double discount = originalPrice - discountedPrice;
        
        // 4. Verify: $8 store discount applied once
        assertEquals(35.0, originalPrice, 0.01, "Original price should be $35");
        assertEquals(8.0, discountedPrice, 0.01, "Discount should be $8");
        assertEquals(27.0, discount, 0.01, "Final price should be $27");
    }

    @Test
    void guest_cart_fixed_discount_prevents_negative_price() {
        // 1. Add a cheap product
        String listingId = storeService.addNewListing(
            MANAGER1, storeId, "cheap-product", "Cheap Product", "Test", "Cheap product description", 100, 3.0, "REGULAR"
        );

        // 2. Add large fixed discount that exceeds product price
        PolicyDTO.AddDiscountRequest largeDiscountRequest = new PolicyDTO.AddDiscountRequest(
            "FIXED", "PRODUCT", "cheap-product", 10.0, null, null, null, null
        );
        
        boolean addDiscountResponse = storePoliciesService.addDiscount(storeId, MANAGER1, largeDiscountRequest);
        assertTrue(addDiscountResponse);
        
        // 3. Calculate prices using StoreService
        Map<String, Integer> basket = Map.of(listingId, 1); // 1 item at $3
        
        // Get original price
        ApiResponse<Double> originalResponse = storeService.getStoreBagPrice(storeId, basket);
        assertTrue(originalResponse.isSuccess());
        double originalPrice = originalResponse.getData();
        
        // Get discounted price
        ApiResponse<Double> discountedResponse = storeService.getStoreBagDiscountPrice(storeId, basket);
        assertTrue(discountedResponse.isSuccess());
        double discountedPrice = discountedResponse.getData();
        
        // Calculate discount
        double discount = originalPrice - discountedPrice;
        
        // 4. Verify discount is capped at item price and final price is not negative
        assertEquals(3.0, originalPrice, 0.01, "Original price should be $3");
        assertEquals(3.0, discountedPrice, 0.01, "Discount should be capped at item price ($3)");
        assertEquals(0.0, discount, 0.01, "Final price should be $0, not negative");
    }

    @Test
    void guest_cart_multiple_fixed_discounts_composite() {
        // Test multiple fixed discounts combined
        String listingId = storeService.addNewListing(
            MANAGER1, storeId, "combo-product", "Combo Product", "electronics", "Combo product description", 100, 20.0, "REGULAR"
        );

        // Add product-specific discount: $5 off per item
        PolicyDTO.AddDiscountRequest productDiscount = new PolicyDTO.AddDiscountRequest(
            "FIXED", "PRODUCT", "combo-product", 5.0, null, null, null, null
        );
        
        // Add category discount: $3 off per electronics item
        PolicyDTO.AddDiscountRequest categoryDiscount = new PolicyDTO.AddDiscountRequest(
            "FIXED", "CATEGORY", "electronics", 3.0, null, null, null, null
        );
        
        // Create composite discount with SUM combination
        PolicyDTO.AddDiscountRequest compositeDiscount = new PolicyDTO.AddDiscountRequest(
            "COMPOSITE", null, null, 0.0, null, null, 
            List.of(productDiscount, categoryDiscount), "SUM"
        );
        
        boolean addDiscountResponse = storePoliciesService.addDiscount(storeId, MANAGER1, compositeDiscount);
        assertTrue(addDiscountResponse);
        
        // Calculate prices using StoreService
        Map<String, Integer> basket = Map.of(listingId, 2); // 2 items at $20 each = $40 total
        
        // Get original price
        ApiResponse<Double> originalResponse = storeService.getStoreBagPrice(storeId, basket);
        assertTrue(originalResponse.isSuccess());
        double originalPrice = originalResponse.getData();
        
        // Get discounted price
        ApiResponse<Double> discountedResponse = storeService.getStoreBagDiscountPrice(storeId, basket);
        assertTrue(discountedResponse.isSuccess());
        double discountedPrice = discountedResponse.getData();
        
        // Calculate discount
        double discount = originalPrice - discountedPrice;
        
        // Verify: ($5 + $3) × 2 items = $16 total discount
        assertEquals(40.0, originalPrice, 0.01, "Original price should be $40");
        assertEquals(16.0, discountedPrice, 0.01, "Should apply both discounts: ($5 + $3) × 2 = $16");
        assertEquals(24.0, discount, 0.01, "Final price should be $24");
    }
}