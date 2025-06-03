package tests;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import market.domain.store.Listing;
import market.domain.store.StoreDTO;
import market.domain.user.ShoppingCart;
import market.domain.user.User;
import market.middleware.TokenUtils; // Ensure this is the correct package for the User class
import support.AcceptanceTestBase;
import utils.ApiResponse;

import market.domain.purchase.Purchase; // Add this import, adjust the package if needed

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Acceptance-level scenarios for a subscriber.
 */
class SubscriberTests extends AcceptanceTestBase {
    
    private final String SHIPPING_ADDRESS = "Subscriber Street";
    private final String CONTACT_INFO = "Suscriber@example.com";

    @BeforeEach
    void setUp() throws Exception {
        this.userService.register("user1", "password1");
        this.userService.register("user2", "password2");
        
    }

    @AfterEach
    void tearDown() throws Exception {
        this.userService.deleteUser("user1");
        this.userService.deleteUser("user2");
    }

    @Test
    void register_a_user_successes_and_cart_was_initial(){
        assertDoesNotThrow(() -> {
            this.userService.register("user3", "password3");
            ShoppingCart cart = this.userService.getUserRepository().getCart("user3");
            assertNotNull(cart, "Shopping cart should not be null");
            assertTrue(cart.getAllStoreBags().isEmpty(), "Shopping cart should be empty");
        });
        this.userService.deleteUser("user3");
    }
    
    @Test
    void register_a_user_failed_already_exists(){
        try {
            this.userService.register("user3", "password3");
            this.userService.register("user3", "password4");
        } catch (Exception e) {
            // Expected exception for duplicate user registration
            assertTrue(e.getMessage().toLowerCase().contains("already exists"), "Error message should indicate user already exists");
        }
        this.userService.deleteUser("user3");
    }

    @Test
    void  get_information_about_stores_and_products_successes(){
        try {
            User user1 = this.userService.getUserRepository().findById("user1");
            String storeid  = this.storeService.createStore("store1", user1.getUserName()).storeId();
            String listing_id =this.storeService.addNewListing(
                "user1", 
                storeid, 
                "p1", 
                "ipad", 
                "electronics", 
                "apple", 
                10,
                1000.0,
                "REGULAR");
            StoreDTO response = this.storeService.getStore("store1");
            // Verify the response
            assertNotNull(response, "Store information should not be null");
            assertEquals("store1", response.getName(),"Store name should match");
            assertEquals(1, this.productService.searchByProductName("ipad").size(), "There should be one product in the store");
            // Clean up
            this.listingRepository.removeListing(listing_id);
            this.storeService.closeStore(storeid, "user1");
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }

    }

    @Test
    void  get_information_about_stores_and_products_success_no_products(){
        try {
            User user1 = this.userService.getUserRepository().findById("user1");
            String storeid  = this.storeService.createStore("store1", user1.getUserName()).storeId();
            StoreDTO response = this.storeService.getStore("store1");
            // Verify the response
            assertNotNull(response, "Store information should not be null");
            assertEquals("store1", response.getName(),"Store name should match");
            assertEquals(0, this.productService.searchByProductName("ipad").size(), "There should be one product in the store");
            // Clean up
            this.storeService.closeStore(storeid, "user1");
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    void search_for_product_across_all_stores_successes(){
        try {
            User user1 = this.userService.getUserRepository().findById("user1");
            String storeid1  = this.storeService.createStore("store1", user1.getUserName()).storeId();
            User user2 = this.userService.getUserRepository().findById("user2");
            String storeid2  = this.storeService.createStore("store2", user2.getUserName()).storeId();
            String listing_id1 =this.storeService.addNewListing(
                "user1", 
                storeid1, 
                "p1", 
                "ipad", 
                "electronics", 
                "apple", 
                10,
                1000.0,
                "REGULAR");
            String listing_id2 =this.storeService.addNewListing(
                "user2", 
                storeid2, 
                "p2", 
                "iphone", 
                "electronics", 
                "apple", 
                10,
                1000,
                "REGULAR");
            List<Listing> ipad = this.productService.searchByProductName("ipad");
            assertNotNull(ipad, "Product search should not return null");
            List<Listing> iphone = this.productService.searchByProductName("iphone");
            assertNotNull(iphone, "Product search should not return null");

            //clean up
            this.listingRepository.removeListing(listing_id1);
            this.listingRepository.removeListing(listing_id2);
            this.storeService.closeStore(storeid1, "user1");
            this.storeService.closeStore(storeid2, "user2");
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    void search_for_product_across_all_stores_no_product(){
        try {
            User user1 = this.userService.getUserRepository().findById("user1");
            String storeid1  = this.storeService.createStore("store1", user1.getUserName()).storeId();
            User user2 = this.userService.getUserRepository().findById("user2");
            String storeid2 = this.storeService.createStore("store2", user2.getUserName()).storeId();
            List<Listing> ipad = this.productService.searchByProductName("ipad");
            assertTrue(ipad.isEmpty(), "Product search should return empty list");
            //clean up
            this.storeService.closeStore(storeid1, "user1");
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    void search_for_product_in_store_successes(){
        try {
            User user1 = this.userService.getUserRepository().findById("user1");
            String storeid1  = this.storeService.createStore("store1", user1.getUserName()).storeId();
            String listing_id1 =this.storeService.addNewListing(
                "user1", 
                storeid1, 
                "p1", 
                "ipad", 
                "electronics", 
                "apple", 
                10,
                1000.0,
                "REGULAR");
            List<Listing> ipad = this.productService.searchInStoreByName(storeid1, "ipad");
            assertNotNull(ipad, "Product search should not return null");
            assertEquals(1, ipad.size(), "There should be one product in the store");
            //clean up
            this.listingRepository.removeListing(listing_id1);
            this.storeService.closeStore(storeid1, "user1");
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    void search_for_product_in_store_no_product(){
        try {
            User user1 = this.userService.getUserRepository().findById("user1");
            String storeid1  = this.storeService.createStore("store1", user1.getUserName()).storeId();
            List<Listing> ipad = this.productService.searchInStoreByName(storeid1, "ipad");
            assertTrue(ipad.isEmpty(), "Product search should return empty list");
            //clean up
            this.storeService.closeStore(storeid1, "user1");
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    void add_proudct_to_storebag_successes(){
        try {
            User user1 = this.userService.getUserRepository().findById("user1");
            String storeid1  = this.storeService.createStore("store1", user1.getUserName()).storeId();
            String listing_id1 =this.storeService.addNewListing(
                "user1", 
                storeid1, 
                "p1", 
                "ipad", 
                "electronics", 
                "apple", 
                10,
                1000.0,
                "REGULAR");
            // Generate token and inject
            String token = authService.generateToken(user1);
            TokenUtils.setMockToken(token);  // <<--- Key step!
            // Call method under test
            userService.addProductToCart(storeid1, listing_id1, 2);
            // Assert
            ShoppingCart cart = this.userService.getUserRepository().getCart("user1");
            assertEquals(2, cart.getStoreBag(storeid1).getProductQuantity(listing_id1), "Product quantity should be 2");
            //clean up
            this.listingRepository.removeListing(listing_id1);
            this.storeService.closeStore(storeid1, "user1");
            TokenUtils.clearMockToken();
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }
 
    @Test
    void view_and_edit_shopping_cart_successes() throws Exception {
        try {
            User user1 = this.userService.getUserRepository().findById("user1");
            String storeid1  = this.storeService.createStore("store1", user1.getUserName()).storeId();
            String listing_id1 =this.storeService.addNewListing(
                "user1", 
                storeid1, 
                "p1", 
                "ipad", 
                "electronics", 
                "apple", 
                10,
                1000.0,
                "REGULAR");
            // Generate token and inject
            String token = authService.generateToken(user1);
            TokenUtils.setMockToken(token);  // <<--- Key step!
            // Call method under test
            userService.addProductToCart(storeid1, listing_id1, 2);    
            // Assert
            ShoppingCart cart = this.userService.getUserRepository().getCart("user1");
            assertEquals(2, cart.getStoreBag(storeid1).getProductQuantity(listing_id1), "Product quantity should be 2");
            //clean up
            this.listingRepository.removeListing(listing_id1);
            this.storeService.closeStore(storeid1, "user1");
            TokenUtils.clearMockToken();
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }     
    }

    @Test
    void purches_cart_successes() throws Exception {
        try {
            User user1 = this.userService.getUserRepository().findById("user1");
            String storeid1  = this.storeService.createStore("store1", user1.getUserName()).storeId();
            String listing_id1 =this.storeService.addNewListing(
                "user1", 
                storeid1, 
                "p1", 
                "ipad", 
                "electronics", 
                "apple", 
                10,
                1000.0,
                "REGULAR");
            when(paymentService.processPayment(anyString())).thenReturn(ApiResponse.ok(true)); 
            when(shipmentService.ship(anyString(), anyString(), anyDouble())).thenReturn(ApiResponse.ok("trackingId")); 
            // Generate token and inject
            String token = authService.generateToken(user1);
            TokenUtils.setMockToken(token);  // <<--- Key step!
            // Call method under test
            userService.addProductToCart(storeid1, listing_id1, 2);
            // Assert
            ShoppingCart cart = this.userService.getUserRepository().getCart("user1");
            assertEquals(2, cart.getStoreBag(storeid1).getProductQuantity(listing_id1), "Product quantity should be 2");
            // Call purchase method
            Purchase purchaseResponse = purchaseService.executePurchase("user1", cart, SHIPPING_ADDRESS, CONTACT_INFO);
            assertNotNull(purchaseResponse, "Purchase data should not be null");
            //verify that the cart is empty after successful purchase
            ShoppingCart updatedCart = this.userService.getUserRepository().getCart("user1");
            assertTrue(updatedCart.getAllStoreBags().isEmpty(), "Shopping cart should be empty after purchase");
            //verify that the store stock is updated
            Listing listing = this.listingRepository.getListingById(listing_id1);
            assertEquals(8, listing.getQuantityAvailable(), "Stock should be reduced by 2 after purchase");
            // Clean up
            this.listingRepository.removeListing(listing_id1);
            this.storeService.closeStore(storeid1, "user1");
            TokenUtils.clearMockToken();
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    void purches_cart_no_product() throws Exception {
        User user1 = this.userService.getUserRepository().findById("user1");
        String storeid1  = this.storeService.createStore("store1", user1.getUserName()).storeId();
        try {
            String shippingAddress = "123 Guest Street";
            String contactInfo = "guest@example.com";
            ShoppingCart cart = user1.getShoppingCart();
            Purchase purchaseResponse = purchaseService.executePurchase(storeid1, cart, shippingAddress, contactInfo);
            fail("Purchase should not succeed with empty cart");
        } catch (Exception e) {
            assertTrue(e.getMessage().toLowerCase().contains("fail"), "Error message should indicate empty cart");
        }
        // Clean up
        this.storeService.closeStore(storeid1, "user1");
    }

    @Test
    void purches_cart_no_enough_money_no_real_credit_card() throws Exception {
        User user1 = this.userService.getUserRepository().findById("user1");
        String storeid1  = this.storeService.createStore("store1", user1.getUserName()).storeId();
        String listing_id1 =this.storeService.addNewListing(
            "user1", 
            storeid1, 
            "p1", 
            "ipad", 
            "electronics", 
            "apple", 
            10,
            1000.0, "REGULAR");
        ShoppingCart cart = this.userService.getUserRepository().getCart("user1");
        cart.addProduct(storeid1, listing_id1, 2);
        assertEquals(2, cart.getStoreBag(storeid1).getProductQuantity(listing_id1), "Product quantity should be 2");
        // Simulate insufficient funds
        when(paymentService.processPayment(anyString())).thenReturn(
            ApiResponse.fail("Insufficient funds")
        );
        try {
            Purchase purchaseResponse = purchaseService.executePurchase(storeid1, cart, "123 Guest Street", "guest@example.com");
        } catch (Exception e) {
            // Expected exception for insufficient funds
            assertTrue(e.getMessage().toLowerCase().contains("failed"), "Error message should indicate insufficient funds");
        }
        assertEquals(2, cart.getStoreBag(storeid1).getProductQuantity(listing_id1), "Product quantity should remain 2 after failed purchase");
    }

    @Test
    void exit_from_the_system() throws Exception {
        try {
            this.userService.register("user3", "password3");
            User user3 = this.userService.getUserRepository().findById("user3");
            String token = this.authService.generateToken(user3);
            assertNotNull(token, "Token should not be null");
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    void open_a_store() throws Exception {
        try {
            User user1 = this.userService.getUserRepository().findById("user1");
            String storeid  = this.storeService.createStore("store1", user1.getUserName()).storeId();
            assertNotNull(storeid, "Store ID should not be null");
            assertTrue(this.storeService.getStore("store1").isActive(), "Store should be active");
            // Clean up
            this.storeService.closeStore(storeid, "user1");
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    void open_a_store_fail() throws Exception {        
        User user1 = this.userService.getUserRepository().findById("user1");
        String storeid  = this.storeService.createStore("store1", user1.getUserName()).storeId();
        try {    
            assertNotNull(storeid, "Store ID should not be null");
            assertTrue(this.storeService.getStore("store1").isActive(), "Store should be active");
            // Attempt to open the same store again
            this.storeService.createStore("store1", user1.getUserName());
        } catch (Exception e) {
            // Expected exception for opening an existing store
            assertTrue(e.getMessage().toLowerCase().contains("already exists"), "Error message should indicate store already exists");
        }
        // Clean up
        this.storeService.closeStore(storeid, "user1");
    }

    @Test
    void submit_bid_for_product_successes() throws Exception {
        try {
            // Setup test data
            User user1 = this.userService.getUserRepository().findById("user1");
            String storeid1 = this.storeService.createStore("store1", user1.getUserName()).storeId();

            String listing_id1 = this.storeService.addNewListing(
                "user1", 
                storeid1, 
                "p1", 
                "premium-phone", 
                "electronics", 
                "flagship smartphone", 
                1,  // Limited quantity for bid
                1500.0, "REGULAR");

            // Generate token and inject
            String token = authService.generateToken(user1);
            TokenUtils.setMockToken(token);
            
            // Configure mocks for successful bid
            when(paymentService.processPayment(anyString())).thenReturn(ApiResponse.ok(true));
            when(shipmentService.ship(anyString(), anyString(), anyDouble())).thenReturn(ApiResponse.ok("trackingId"));
            
            double bidAmount = 1200.0; // Bid below list price
            
            // Submit bid
            purchaseService.submitBid(
                storeid1, 
                listing_id1, 
                "user1", 
                bidAmount, 
                SHIPPING_ADDRESS, 
                CONTACT_INFO);

            // Verify bid status 
            String statusResponse = purchaseService.getBidStatus(storeid1, listing_id1, "user1");
            assertEquals("Pending Approval", statusResponse, "Bid should be in PENDING status");

            // Clean up
            this.listingRepository.removeListing(listing_id1);
            this.storeService.closeStore(storeid1, "user1");
            TokenUtils.clearMockToken();
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
}

    @Test
    void submit_bid_for_product_fail() throws Exception {
        // Setup test data
        User user1 = this.userService.getUserRepository().findById("user1");
        String storeid1 = this.storeService.createStore("store1", user1.getUserName()).storeId();

        String listing_id1 = this.storeService.addNewListing(
            "user1", 
            storeid1, 
            "p1", 
            "premium-phone", 
            "electronics", 
            "flagship smartphone", 
            1,
            1500.0, "REGULAR");

        // Generate token and inject
        String token = authService.generateToken(user1);
        TokenUtils.setMockToken(token);
        
        // Configure mocks
        when(paymentService.processPayment(anyString())).thenReturn(ApiResponse.ok(true));
        when(shipmentService.ship(anyString(), anyString(), anyDouble())).thenReturn(ApiResponse.ok("trackingId"));
        
        // Attempt to submit an invalid bid (negative amount)
        double invalidBidAmount = -500.0;
        try {    
            // Submit invalid bid
            purchaseService.submitBid(
                storeid1, 
                listing_id1, 
                "user1", 
                invalidBidAmount, 
                SHIPPING_ADDRESS, 
                CONTACT_INFO);
            fail("Bid submission should have failed with negative amount");
        } catch (Exception e) {
            // Expected exception for invalid bid amount
            assertTrue(e.getMessage().toLowerCase().contains("bid must be a positive value"), "Error message should indicate bid failure");
        }

        // Clean up
        this.listingRepository.removeListing(listing_id1);
        this.storeService.closeStore(storeid1, "user1");
        TokenUtils.clearMockToken();
    }

    @Test
    void purche_proudct_after_auction_succsesses() throws Exception {
        try {
            // Setup test data
            User user1 = this.userService.getUserRepository().findById("user1");
            String storeid1 = this.storeService.createStore("store1", user1.getUserName()).storeId();

            String listing_id1 = this.storeService.addNewListing(
                "user1", 
                storeid1, 
                "p1", 
                "collectible-item", 
                "collectibles", 
                "rare collectible item", 
                1,
                2000.0, "REGULAR");
            
            // Setup end time for auction (1 minute from now)
            long endTime = System.currentTimeMillis() + 60000;
            
            // Generate token and inject
            String token = authService.generateToken(user1);
            TokenUtils.setMockToken(token);
            
            // Configure mocks for payment and shipping
            when(paymentService.processPayment(anyString())).thenReturn(ApiResponse.ok(true));
            when(shipmentService.ship(anyString(), anyString(), anyDouble())).thenReturn(ApiResponse.ok("trackingId"));
            
            // Open auction for the item
            purchaseService.openAuction(
                "user1", 
                storeid1, 
                listing_id1, 
                "collectible-item", 
                "collectibles", 
                "rare collectible item", 
                1500, // Starting price
                endTime);
                        
            // Register another user for bidding
            User user2 = this.userService.getUserRepository().findById("user2");
            String token2 = authService.generateToken(user2);
            
            // Submit an offer
            TokenUtils.setMockToken(token2);
            purchaseService.submitOffer(
                storeid1,
                listing_id1,
                "user2",
                1800.0, // Offer price
                SHIPPING_ADDRESS,
                CONTACT_INFO);
                        
            // Check auction status
            Map<String, Object> status = purchaseService.getAuctionStatus("user2", storeid1, listing_id1);
            
            assertNotNull(status, "Auction status data should not be null");
            
            // UPDATED: Instead of checking for bidder, check for the current offer value
            assertNotNull(status.get("currentMaxOffer"), "Current max offer should not be null");
            assertEquals(1800.0, (Double)status.get("currentMaxOffer"), 0.001, "Current max offer should match the submitted bid");
        
            // Verify the starting price is correct
            assertEquals(1500.0, (Double)status.get("startingPrice"), 0.001,
                        "Starting price should match what was set");
            
            // Verify time remaining is positive
            assertTrue((Long)status.get("timeLeftMillis") > 0, 
                    "Auction should still have time remaining");
            
            // Clean up
            this.listingRepository.removeListing(listing_id1);
            this.storeService.closeStore(storeid1, "user1");
            TokenUtils.clearMockToken();
            
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }


 

   
}
