package market.domainTests;

import market.domain.user.User;
import market.domain.user.StoreBag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Shopping-Cart functionality accessed via {@link User}.
 */
public class ShoppingCartTests {

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User("user123");
    }

    /* --------------------------------------------------------------------
       ADD PRODUCT
       ------------------------------------------------------------------ */

    @Test
    public void testAddProductCreatesStoreBagAndSetsQuantity() {
        user.addProductToCart("store1", "prodA", 3);

        StoreBag bag = user.getShoppingCart().getStoreBag("store1");
        assertNotNull(bag, "StoreBag should have been created");

        Map<String,Integer> qty = bag.getProductQuantities();
        assertEquals(3, qty.get("prodA"));

        user.removeProductFromCart("store1", "prodA",3);
    
    }


    @Test
    public void testAddProductAccumulatesQuantity() {
        user.addProductToCart("store1", "prodA", 2);
        user.addProductToCart("store1", "prodA", 4);

        int finalQty = user.getShoppingCart()
                           .getStoreBag("store1")
                           .getProductQuantities()
                           .get("prodA");
        assertEquals(6, finalQty);
        user.removeProductFromCart("store1", "prodA",6);
    }

    @Test
    public void testAddProductWithNonPositiveQuantityThrows() {
        assertThrows(IllegalArgumentException.class,
                     () -> user.addProductToCart("store1", "prodA", 0));
        assertThrows(IllegalArgumentException.class,
                     () -> user.addProductToCart("store1", "prodA", -5));
    }

    /* --------------------------------------------------------------------
       REMOVE PRODUCT
       ------------------------------------------------------------------ */

    @Test
    public void testRemoveProductReducesQuantity() {
        user.addProductToCart("store1", "prodA", 5);
        user.removeProductFromCart("store1", "prodA", 3);

        int remaining = user.getShoppingCart()
                            .getStoreBag("store1")
                            .getProductQuantities()
                            .get("prodA");
        assertEquals(2, remaining);

        user.removeProductFromCart("store1", "prodA",2);

    }

    @Test
    public void testRemoveProductRemovesEntryWhenQuantityZero() {
        user.addProductToCart("store1", "prodA", 2);
        user.removeProductFromCart("store1", "prodA", 2);

        StoreBag bag = user.getShoppingCart().getStoreBag("store1");
        assertFalse(bag.getProductQuantities().containsKey("prodA"),
                    "Product entry should be removed when quantity reaches zero");
    }
}
