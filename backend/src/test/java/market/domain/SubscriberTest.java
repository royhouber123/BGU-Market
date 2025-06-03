package market.domain;

import market.domain.user.Subscriber;
import market.domain.user.StoreBag;
import market.domain.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Subscriber} class functionalities.
 */
public class SubscriberTest {

    private Subscriber subscriber;
    private static final String TEST_STORE = "store1";
    private static final String TEST_PRODUCT = "prodA";

    /**
     * Sets up the test environment before each test.
     * Creates a new Subscriber instance with a test username.
     */
    @BeforeEach
    public void setUp() {
        subscriber = new Subscriber("user123");
    }
    
    /**
     * Cleans up the test environment after each test.
     * Resets the subscriber's shipping address.
     */
    @AfterEach
    public void tearDown() {
        if (subscriber != null) {
            subscriber.setShippingAddress("");
            subscriber.clearCart();
        }
    }

    /**
     * Tests setting and getting a shipping address for a subscriber.
     * 
     * Given a subscriber
     * When setting a valid shipping address
     * Then the shipping address should be stored correctly
     */
    @Test
    public void setShippingAddress_withValidAddress_shouldStoreCorrectly() {
        // Arrange
        String testAddress = "123 Main St";
        
        // Act
        subscriber.setShippingAddress(testAddress);
        
        // Assert
        assertEquals(testAddress, subscriber.getShippingAddress(), 
            "Shipping address should match the set address");
    }
    
    /**
     * Tests setting and getting an empty shipping address for a subscriber.
     * 
     * Given a subscriber
     * When setting an empty shipping address
     * Then the empty address should be stored correctly
     */
    @Test
    public void setShippingAddress_withEmptyString_shouldStoreEmptyAddress() {
        // Arrange
        String emptyAddress = "";
        
        // Act
        subscriber.setShippingAddress(emptyAddress);
        
        // Assert
        assertEquals(emptyAddress, subscriber.getShippingAddress(), 
            "Empty shipping address should be stored correctly");
    }
    
    /**
     * Tests the initial state of a new subscriber's shipping address.
     * 
     * Given a newly created subscriber
     * When getting the shipping address
     * Then the address should be empty
     */
    @Test
    public void getShippingAddress_onNewSubscriber_shouldReturnEmptyString() {
        // Act & Assert
        assertEquals("", subscriber.getShippingAddress(), 
            "New subscriber should have an empty shipping address");
    }
    
    /**
     * Tests creation of a subscriber from a basic user.
     * 
     * Given a basic user
     * When creating a subscriber with the same username
     * Then the subscriber should properly inherit user properties
     */
    @Test
    public void createSubscriber_fromBasicUser_shouldInheritUserProperties() {
        // Arrange
        String testUsername = "testUser";
        User basicUser = new User(testUsername);
        
        // Act
        Subscriber newSubscriber = new Subscriber(testUsername);
        
        // Assert
        assertEquals(basicUser.getUserName(), newSubscriber.getUserName(), 
            "Subscriber should have the same username as the basic user");
        assertNotNull(newSubscriber.getShoppingCart(), 
            "Subscriber should have a shopping cart initialized");
        assertEquals("", newSubscriber.getShippingAddress(), 
            "New subscriber should have an empty shipping address");
    }
    
    /**
     * Tests that a subscriber can add a product to their cart.
     * 
     * Given a subscriber
     * When adding a product to the cart
     * Then the product should be added successfully with the correct quantity
     */
    @Test
    public void addProductToCart_asSubscriber_shouldAddProductSuccessfully() {
        // Arrange
        int quantity = 3;
        
        // Act
        subscriber.addProductToCart(TEST_STORE, TEST_PRODUCT, quantity);
        
        // Assert
        StoreBag bag = subscriber.getShoppingCart().getStoreBag(TEST_STORE);
        assertNotNull(bag, "StoreBag should have been created for the subscriber");
        
        Map<String, Integer> productQuantities = bag.getProductQuantities();
        assertEquals(quantity, productQuantities.get(TEST_PRODUCT), 
            "Product should be added with the correct quantity");
    }
    
    /**
     * Tests that a subscriber can remove a product from their cart.
     * 
     * Given a subscriber with a product in their cart
     * When removing some quantity of the product
     * Then the product quantity should be updated correctly
     */
    @Test
    public void removeProductFromCart_asSubscriber_shouldUpdateCartCorrectly() {
        // Arrange
        int initialQuantity = 5;
        int removeQuantity = 2;
        int expectedFinalQuantity = initialQuantity - removeQuantity;
        
        subscriber.addProductToCart(TEST_STORE, TEST_PRODUCT, initialQuantity);
        
        // Act
        subscriber.removeProductFromCart(TEST_STORE, TEST_PRODUCT, removeQuantity);
        
        // Assert
        StoreBag bag = subscriber.getShoppingCart().getStoreBag(TEST_STORE);
        assertNotNull(bag, "StoreBag should still exist after partial removal");
        
        Map<String, Integer> productQuantities = bag.getProductQuantities();
        assertEquals(expectedFinalQuantity, productQuantities.get(TEST_PRODUCT), 
            "Product quantity should be updated correctly after removal");
    }
}
