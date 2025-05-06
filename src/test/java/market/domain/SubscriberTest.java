package market.domain;

import market.domain.user.Subscriber;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Subscriber} class functionalities.
 */
public class SubscriberTest {

    private Subscriber subscriber;

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
}
