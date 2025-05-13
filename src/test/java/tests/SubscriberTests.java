package tests;
import market.application.AuthService.AuthToken;
import market.domain.purchase.Purchase;
import market.domain.store.Listing;
import market.domain.store.StoreDTO;
import market.domain.user.ShoppingCart;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.lang.Assert;
import support.AcceptanceTestBase;
import utils.ApiResponse;

import java.util.List;
import market.domain.user.User; // Ensure this is the correct package for the User class

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Acceptance-level scenarios for a subscriber.
 */
class SubscriberTests extends AcceptanceTestBase {
    String user1_storeid;
    



    
    

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
    void register_a_user_successes(){
        assertDoesNotThrow(() -> {
            this.userService.register("user3", "password3");
            ShoppingCart cart = this.userService.getUserRepository().getData().getCart("user3");
            assertNotNull(cart, "Shopping cart should not be null");
            assertTrue(cart.getAllStoreBags().isEmpty(), "Shopping cart should be empty");
            });

            this.userService.deleteUser("user3");
        }
    


    @Test
    void  get_information_about_stores_and_products_successes(){
        
        assertDoesNotThrow(() -> {
        User user1 = this.userService.getUserRepository().getData().findById("user1");
        String storeid  = this.storeService.createStore("store1", user1.getUserName()).getData();
        String listing_id =this.storeService.addNewListing(
            "user1", 
            storeid, 
            "p1", 
            "ipad", 
            "electronics", 
            "apple", 
            10,
            1000).getData();


        
        ApiResponse<StoreDTO> response = this.storeService.getStore("store1");
        

        // Verify the response
        assertTrue(response.isSuccess(), "Failed to get store information");
        assertNotNull(response.getData(), "Store information should not be null");
        assertEquals("store1", response.getData().getName(),"Store name should match");
        
        

        assertEquals(1, this.productService.searchByProductName("ipad").getData().size(), "There should be one product in the store");



        // Clean up
        this.listingRepository.removeListing(listing_id);
        this.storeService.closeStore(user1_storeid, user1_storeid);
        });
        




    }

    @Test
    void  get_information_about_stores_and_products_success_no_products(){
        assertDoesNotThrow(() -> {
        User user1 = this.userService.getUserRepository().getData().findById("user1");
        String storeid  = this.storeService.createStore("store1", user1.getUserName()).getData();


        
        ApiResponse<StoreDTO> response = this.storeService.getStore("store1");
        

        // Verify the response
        assertTrue(response.isSuccess(), "Failed to get store information");
        assertNotNull(response.getData(), "Store information should not be null");
        assertEquals("store1", response.getData().getName(),"Store name should match");
        
        

        assertEquals(0, this.productService.searchByProductName("ipad").getData().size(), "There should be one product in the store");



        // Clean up
        this.storeService.closeStore(user1_storeid, user1_storeid);
        });

    }
    @Test
    void search_for_product_across_all_stores_successes(){
        User user1 = this.userService.getUserRepository().getData().findById("user1");
        String storeid1  = this.storeService.createStore("store1", user1.getUserName()).getData();

        User user2 = this.userService.getUserRepository().getData().findById("user2");
        String storeid2  = this.storeService.createStore("store2", user2.getUserName()).getData();

        String listing_id1 =this.storeService.addNewListing(
            "user1", 
            storeid1, 
            "p1", 
            "ipad", 
            "electronics", 
            "apple", 
            10,
            1000).getData();

        String listing_id2 =this.storeService.addNewListing(
            "user2", 
            storeid2, 
            "p2", 
            "iphone", 
            "electronics", 
            "apple", 
            10,
            1000).getData();


        List<Listing> ipad = this.productService.searchByProductName("ipad").getData();
        assertNotNull(ipad, "Product search should not return null");
        List<Listing> iphone = this.productService.searchByProductName("iphone").getData();
        assertNotNull(iphone, "Product search should not return null");

        //clean up
        this.listingRepository.removeListing(listing_id1);
        this.listingRepository.removeListing(listing_id2);
        this.storeService.closeStore(storeid1, user1.getUserName());
        this.storeService.closeStore(storeid2, user2.getUserName());

    }


    @Test
    void search_for_product_across_all_stores_no_product(){
        User user1 = this.userService.getUserRepository().getData().findById("user1");
        String storeid1  = this.storeService.createStore("store1", user1.getUserName()).getData();

        User user2 = this.userService.getUserRepository().getData().findById("user2");
        String storeid2  = this.storeService.createStore("store2", user2.getUserName()).getData();



        List<Listing> ipad = this.productService.searchByProductName("ipad").getData();
        assertTrue(ipad.isEmpty(), "Product search should return empty list");

        //clean up
        this.storeService.closeStore(storeid1, user1.getUserName());
    }
    @Test
    void search_for_product_in_store_successes(){

    }
    @Test
    void search_for_product_in_store_no_product(){

    }
    @Test
    void add_proudct_to_storebag_successes(){
        User user1 = this.userService.getUserRepository().getData().findById("user1");
        String storeid1  = this.storeService.createStore("store1", user1.getUserName()).getData();

        String listing_id1 =this.storeService.addNewListing(
            "user1", 
            storeid1, 
            "p1", 
            "ipad", 
            "electronics", 
            "apple", 
            10,
            1000).getData();

        ShoppingCart cart = this.userService.getUserRepository().getData().getCart(user1.getUserName());
        cart.addProduct(storeid1, listing_id1, 2);
        assertEquals(2, cart.getStoreBag(storeid1).getProductQuantity(listing_id1), "Product quantity should be 2");

        //clean up
        this.listingRepository.removeListing(listing_id1);
        this.storeService.closeStore(storeid1, user1.getUserName());
    }
    @Test
    void add_proudct_to_storebag_no_product(){   //dosent check if the product is in the store tell omer/dayan to fix it
        User user1 = this.userService.getUserRepository().getData().findById("user1");
        String storeid1  = this.storeService.createStore("store1", user1.getUserName()).getData();
        ShoppingCart cart = this.userService.getUserRepository().getData().getCart(user1.getUserName());
        assertThrows(IllegalArgumentException.class, () -> cart.addProduct(storeid1, "5", 2));
        

        //clean up
        this.storeService.closeStore(storeid1, user1.getUserName());
    }

    @Test
    void view_and_edit_shopping_cart_successes() throws Exception {
        
    }

    @Test
    void purches_cart_successes() throws Exception {
    }

    @Test
    void purches_cart_no_product() throws Exception {
    }

    @Test
    void purches_cart_no_enough_money_no_real_credit_card() throws Exception {
    }

    @Test
    void exit_from_the_system() throws Exception {
    }

    @Test
    void log_out_succsesses() throws Exception {
    }

    @Test
    void log_out_fail() throws Exception {
    }

    @Test
    void open_a_store() throws Exception {
    }

    @Test
    void open_a_store_fail() throws Exception {
    }

    @Test
    void rate_proudct_and_store_successes() throws Exception {
    }

    @Test
    void rate_proudct_and_store_fail() throws Exception {
    }

    @Test
    void send_message_to_store_successes() throws Exception {
    }

    @Test
    void send_message_to_store_fail() throws Exception {
    }

    @Test
    void view_personal_purcheses_history_successes() throws Exception {
    }

    @Test
    void view_personal_purcheses_history_fail() throws Exception {
    }

    @Test
    void submit_bid_for_product_successes() throws Exception {
    }

    @Test
    void submit_bid_for_product_fail() throws Exception {
    }

    @Test
    void purche_product_after_bids_successes() throws Exception {
    }

    @Test
    void purche_proudct_after_auction_succsesses() throws Exception {
    }

    @Test
    void purche_proudct_after_auction_fail() throws Exception {
    }

    




   
}
