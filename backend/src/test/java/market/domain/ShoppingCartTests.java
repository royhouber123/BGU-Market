package market.domain;

import market.domain.user.User;
import market.domain.user.StoreBag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Shopping-Cart functionality accessed via {@link User}.
 */
public class ShoppingCartTests {

    private User user;
    private static final String TEST_STORE = "store1";
    private static final String TEST_PRODUCT = "prodA";

    /**
     * Sets up the test environment before each test.
     * Creates a new User instance with a test username.
     */
    @BeforeEach
    public void setUp() {
        user = new User("user123");
    }

    /**
     * Cleans up the test environment after each test.
     * Removes any products added to the cart during tests.
     */
    @AfterEach
    public void tearDown() {
        // Clean up any remaining products in the cart
        if (user != null && user.getShoppingCart() != null) {
            StoreBag bag = user.getShoppingCart().getStoreBag(TEST_STORE);
            if (bag != null) {
                for (String productId : bag.getProductQuantities().keySet()) {
                    int quantity = bag.getProductQuantities().get(productId);
                    user.removeProductFromCart(TEST_STORE, productId, quantity);
                }
            }
        }
    }

    /* --------------------------------------------------------------------
       ADD PRODUCT
       ------------------------------------------------------------------ */

    /**
     * Tests adding a product to the cart when the store bag doesn't exist yet.
     * 
     * Given a user with an empty shopping cart
     * When adding a product to a new store bag
     * Then a new store bag should be created with the correct product quantity
     */
    @Test
    public void addProductToCart_toNewStoreBag_shouldCreateBagAndSetQuantity() {
        // Arrange
        int initialQuantity = 3;
        
        // Act
        user.addProductToCart(TEST_STORE, TEST_PRODUCT, initialQuantity);

        // Assert
        StoreBag bag = user.getShoppingCart().getStoreBag(TEST_STORE);
        assertNotNull(bag, "StoreBag should have been created");

        Map<String, Integer> qty = bag.getProductQuantities();
        assertEquals(initialQuantity, qty.get(TEST_PRODUCT), 
            "Product quantity should match the added amount");
    }

    /**
     * Tests accumulating product quantity when adding the same product multiple times.
     * 
     * Given a user with a product already in the cart
     * When adding more of the same product
     * Then the quantities should be accumulated correctly
     */
    @Test
    public void addProductToCart_existingProduct_shouldAccumulateQuantity() {
        // Arrange
        int firstBatch = 2;
        int secondBatch = 4;
        int expectedTotal = firstBatch + secondBatch;
        
        // Act
        user.addProductToCart(TEST_STORE, TEST_PRODUCT, firstBatch);
        user.addProductToCart(TEST_STORE, TEST_PRODUCT, secondBatch);

        // Assert
        int finalQty = user.getShoppingCart()
                          .getStoreBag(TEST_STORE)
                          .getProductQuantities()
                          .get(TEST_PRODUCT);
        assertEquals(expectedTotal, finalQty, 
            "Product quantity should be the sum of all additions");
    }

    /**
     * Tests adding a product with zero quantity.
     * 
     * Given a user with a shopping cart
     * When attempting to add a product with zero quantity
     * Then an IllegalArgumentException should be thrown
     */
    @Test
    public void addProductToCart_withZeroQuantity_shouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                    () -> user.addProductToCart(TEST_STORE, TEST_PRODUCT, 0),
                    "Adding product with zero quantity should throw IllegalArgumentException");
    }

    /**
     * Tests adding a product with negative quantity.
     * 
     * Given a user with a shopping cart
     * When attempting to add a product with negative quantity
     * Then an IllegalArgumentException should be thrown
     */
    @Test
    public void addProductToCart_withNegativeQuantity_shouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                    () -> user.addProductToCart(TEST_STORE, TEST_PRODUCT, -5),
                    "Adding product with negative quantity should throw IllegalArgumentException");
    }

    /* --------------------------------------------------------------------
       REMOVE PRODUCT
       ------------------------------------------------------------------ */

    /**
     * Tests removing part of a product's quantity from the cart.
     * 
     * Given a user with a product in the cart
     * When removing part of the product quantity
     * Then the remaining quantity should be correctly updated
     */
    @Test
    public void removeProductFromCart_partialQuantity_shouldReduceQuantity() {
        // Arrange
        int initialQuantity = 5;
        int removeQuantity = 3;
        int expectedRemaining = initialQuantity - removeQuantity;
        
        user.addProductToCart(TEST_STORE, TEST_PRODUCT, initialQuantity);
        
        // Act
        user.removeProductFromCart(TEST_STORE, TEST_PRODUCT, removeQuantity);

        // Assert
        int remaining = user.getShoppingCart()
                           .getStoreBag(TEST_STORE)
                           .getProductQuantities()
                           .get(TEST_PRODUCT);
        assertEquals(expectedRemaining, remaining, 
            "Remaining quantity should be correctly reduced");
    }

    /**
     * Tests removing all of a product's quantity from the cart.
     * 
     * Given a user with a product in the cart
     * When removing all of the product quantity
     * Then the product entry should be removed from the cart
     */
    @Test
    public void removeProductFromCart_entireQuantity_shouldRemoveProductEntry() {
        // Arrange
        int quantity = 2;
        user.addProductToCart(TEST_STORE, TEST_PRODUCT, quantity);
        
        // Act
        user.removeProductFromCart(TEST_STORE, TEST_PRODUCT, quantity);

        // Assert
        StoreBag bag = user.getShoppingCart().getStoreBag(TEST_STORE);
        
        // The test should pass if either:
        // 1. The entire bag was removed (bag is null)
        // 2. The bag exists but doesn't contain the product
        if (bag != null) {
            assertFalse(bag.getProductQuantities().containsKey(TEST_PRODUCT),
                       "Product entry should be removed when quantity reaches zero");
        } else {
            // If the bag is null, that's also valid as the ShoppingCart may remove empty bags
            assertTrue(true, "Store bag was removed completely, which is an acceptable implementation");
        }
    }
    
    /**
     * Tests attempting to remove more than the available quantity.
     * 
     * Given a user with a product in the cart
     * When attempting to remove more than the available quantity
     * Then an IllegalArgumentException should be thrown
     */
    @Test
    public void removeProductFromCart_exceedingQuantity_shouldThrowException() {
        // Arrange
        int initialQuantity = 2;
        int excessiveRemovalQuantity = initialQuantity + 1;
        
        user.addProductToCart(TEST_STORE, TEST_PRODUCT, initialQuantity);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
                   () -> user.removeProductFromCart(TEST_STORE, TEST_PRODUCT, excessiveRemovalQuantity),
                   "Removing more than available quantity should throw exception");
    }
    
    /**
     * Tests removing a product that doesn't exist in the cart.
     * 
     * Given a user with an empty shopping cart
     * When attempting to remove a non-existent product
     * Then an IllegalArgumentException should be thrown
     */
    @Test
    public void removeProductFromCart_nonExistentProduct_shouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                   () -> user.removeProductFromCart(TEST_STORE, "nonExistentProduct", 1),
                   "Removing non-existent product should throw exception");
    }
}
