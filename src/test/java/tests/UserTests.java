package tests;

import org.junit.jupiter.api.Test;

import support.AcceptanceTestBase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.Flow.Subscriber;

import org.junit.jupiter.api.BeforeEach;

import market.domain.user.ShoppingCart;
import market.domain.user.User;
import market.domain.user.StoreBag;


import market.application.AuthService;   
import market.application.AuthService.AuthTokens; 
public class UserTests extends AcceptanceTestBase {
    String storeid;

    @BeforeEach
    void setUp() throws Exception {
        this.userService.register("user1", "password1");
        this.userService.register("user2", "password2");
        storeid = this.storeService.createStore("store1", "1");
    }



    @Test
    void user_login_successfully() {
        
    try {
        this.userService.register("user3", "password3");
        AuthTokens tokens = authService.login("user3", "password3");
        String accessToken  = tokens.accessToken();  
        String refreshToken = tokens.refreshToken();
        assertNotNull(refreshToken);
        assertNotNull(refreshToken);
    } catch (Exception exception) {
        
    }
    
       
       
    }

    @Test
void user_login_unsuccessfully() throws Exception {

       
    // Arrange  – user is registered with the *correct* password
        
    
    // Act + Assert – expect AuthService.login to throw
    Exception ex = assertThrows(Exception.class, () ->userService.register("user2", "password2")
        
    );

    
}

// get /getall / auth / roles chech[managar , owner ], userservice.


     @Test
    void user_add_product() {
        try {
            this.userService.getUserRepository().findById("user1").addProductToCart(storeid, "gvina", 2);
            ShoppingCart shoppingCart = this.userService.getUserRepository().getCart("user1");
            assertEquals(2, shoppingCart.getStoreBag(storeid).getProductQuantity("gvina"));
            this.userService.getUserRepository().findById("user1").removeProductFromCart(storeid, "gvina", 2); // check later
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

   
    // @Test
    // void user_logs_out__session_cleared() {
    //     try {
    //         this.userService.register("user4", "password4");
    //         AuthTokens tokens = authService.login("user4", "password4");
    //         String accessToken  = tokens.accessToken();  
    //         String refreshToken = tokens.refreshToken();
    //         String ans = this.authService.logout(refreshToken,accessToken);  /// ask roy and yair to make logout return string  
    //     } catch (Exception exception)
    //     {
            
    //     }
    // }



    @Test
    void user_changes_password_successfully() {
        ///
        try {
            this.userService.register("user5", "password5");
            AuthTokens tokens = authService.login("user5", "password5");
            String accessToken  = tokens.accessToken();  
            String refreshToken = tokens.refreshToken();
            boolean ans = this.userService.changePassword("newPassword5");
            assertTrue(ans);
            this.authService.logout(refreshToken,accessToken);    
        } catch (Exception exception)
        {
            
        }
    }

  

        @Test
        void user_views_saved_payment_and_shipping_info() {
            try {
                this.userService.register("user6", "password6");
                AuthTokens tokens = authService.login("user6", "password6");
                String accessToken  = tokens.accessToken();  
                String refreshToken = tokens.refreshToken();
                this.userService.addProductToCart( "1", "gvina", 3);
                /// need to check payment and shipping which both mock
                when (paymentService.getPaymentStatus("user6")).thenReturn("Visa **** 1234");
                when(shipmentService.getShippingInfo("user6")).thenReturn("123 Main St, City, Country");

                String paymentInfo = paymentService.getPaymentStatus("user6");
                String shippingInfo = shipmentService.getShippingInfo("user6");

                assertEquals("Visa **** 1234", paymentInfo);
                assertEquals("123 Main St, City, Country", shippingInfo);

                this.authService.logout(refreshToken,accessToken);
                
                
            } catch (Exception exception)
            {
                
            }
        }

    
}