package market.application;

import market.domain.user.User;
import market.middleware.TokenUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import support.AcceptanceTestSpringBase;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.junit.jupiter.api.condition.EnabledIf;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import market.domain.user.StoreBag;
import java.util.Map;
import market.dto.StoreDTO.CreateStoreResponse;

@EnabledIf("market.application.UserServiceTests#isMySQLAvailable")
public class UserServiceTests extends AcceptanceTestSpringBase {

    @BeforeEach
    void setUp() {
        TokenUtils.clearMockToken();
    }

    @AfterEach
    void tearDown() {
        TokenUtils.clearMockToken();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void testAddProductToCart_ShouldPersistCart() throws Exception {
        // Arrange: Create a user and log them in
        String username = "service_tester";
        String password = "password123";
        userService.register(username, password);
        AuthService.AuthToken token = authService.login(username, password);
        TokenUtils.setMockToken(token.token());

        // Act: Add products from multiple stores to the cart
        // Create stores and add listings first
        CreateStoreResponse electronicsStore = storeService.createStore("electronics_store", username);
        CreateStoreResponse bookStore = storeService.createStore("book_store", username);
        CreateStoreResponse officeStore = storeService.createStore("office_supplies", username);

        // Add listings to stores
        storeService.addNewListing(username, electronicsStore.storeId(), "laptop", "Laptop", "electronics", "A powerful laptop", 10, 999.99, "REGULAR");
        storeService.addNewListing(username, electronicsStore.storeId(), "mouse", "Mouse", "electronics", "A wireless mouse", 20, 29.99, "REGULAR");
        storeService.addNewListing(username, bookStore.storeId(), "spring_in_action", "Spring in Action", "books", "A book about Spring", 15, 49.99, "REGULAR");
        storeService.addNewListing(username, officeStore.storeId(), "stapler", "Stapler", "office", "A red stapler", 50, 9.99, "REGULAR");
        storeService.addNewListing(username, officeStore.storeId(), "paper_clips", "Paper Clips", "office", "A box of paper clips", 1000, 2.99, "REGULAR");

        // Now add products to cart
        userService.addProductToCart(electronicsStore.storeId(), "laptop", 1);
        userService.addProductToCart(electronicsStore.storeId(), "mouse", 2);
        userService.addProductToCart(bookStore.storeId(), "spring_in_action", 1);
        userService.addProductToCart(officeStore.storeId(), "stapler", 1);
        userService.addProductToCart(officeStore.storeId(), "paper_clips", 100);

        // Assert: Verify the entire shopping cart structure
        User foundUser = userRepository.findById(username);
        assertNotNull(foundUser, "User should be found in the database.");
        
        var cart = foundUser.getShoppingCart();
        assertNotNull(cart, "Shopping cart should not be null.");

        Map<String, StoreBag> storeBags = cart.getStoreBags();
        assertEquals(3, storeBags.size(), "Should have bags from 3 different stores.");

        // Verify electronics_store bag
        StoreBag electronicsBag = storeBags.get(electronicsStore.storeId());
        assertNotNull(electronicsBag, "Electronics store bag should exist.");
        assertEquals(2, electronicsBag.getProductQuantities().size(), "Electronics bag should have 2 products.");
        assertEquals(1, electronicsBag.getProductQuantity("laptop"));
        assertEquals(2, electronicsBag.getProductQuantity("mouse"));

        // Verify book_store bag
        StoreBag bookBag = storeBags.get(bookStore.storeId());
        assertNotNull(bookBag, "Book store bag should exist.");
        assertEquals(1, bookBag.getProductQuantities().size(), "Book bag should have 1 product.");
        assertEquals(1, bookBag.getProductQuantity("spring_in_action"));

        // Verify office_supplies bag
        StoreBag officeBag = storeBags.get(officeStore.storeId());
        assertNotNull(officeBag, "Office supplies bag should exist.");
        assertEquals(2, officeBag.getProductQuantities().size(), "Office supplies bag should have 2 products.");
        assertEquals(1, officeBag.getProductQuantity("stapler"));
        assertEquals(100, officeBag.getProductQuantity("paper_clips"));
    }

    @Test
    void test_login_new_user() {
        System.out.println("Debugger is attached. You can now step through the code.");
        assertDoesNotThrow(() -> {
            userService.register("testuser", "password");
            AuthService.AuthToken loggedInUser = authService.login("testuser", "password");
        });
    }

    static boolean isMySQLAvailable() {
        try {
            DriverManager.getConnection("jdbc:mysql://localhost:3306/?useSSL=false", "bgu", "changeme").close();
            return true;
        } catch (Exception e) {
            System.out.println("⚠️  MySQL not available – skipping UserServiceTests");
            return false;
        }
    }
} 