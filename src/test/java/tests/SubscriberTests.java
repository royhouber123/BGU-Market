package tests;
import market.application.AuthService.AuthToken;
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
    int user1_storeid;
    int first_product_in_user1_store;



    
    

    @BeforeEach
    void setUp() throws Exception {
        this.userService.register("user1", "password1");
        this.userService.register("user2", "password2");
        this.first_proudct_in_user1_store  = this.storeService.createStore("store1", "1");
       

       
        
        when(paymentService.processPayment(anyString())).thenReturn(true);
        when(shipmentService.ship(anyString(), anyString(), anyDouble())).thenReturn("123");

       
    }
    @Test
    void  get_information_about_stores_and_products_successes(){

    }
    @Test
    void  get_information_about_stores_and_products_success_no_products(){

    }
    @Test
    void search_for_product_across_all_stores_successes(){

    }
    @Test
    void search_for_product_across_all_stores_no_product(){

    }
    @Test
    void search_for_product_in_store_successes(){

    }
    @Test
    void search_for_product_in_store_no_product(){

    }
    @Test
    void add_proudct_to_storebag_successes(){

    }
    @Test
    void add_proudct_to_storebag_no_product(){

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
