package market.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import market.domain.purchase.Purchase;
import market.domain.purchase.PurchasedProduct;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class RegularPurchaseTest {

    @BeforeEach
    void setUp() {
        // No specific setup needed for now
    }

    @Test
    void testPurchaseCreation() {
        // TODO: implement
        String userId = "user1";
        String storeId = "store1";
        String productId = "prod1";
        double price = 50.0;
        int quantity = 2;
        String shippingAddress = "123 Main St";
        String contactInfo = "555-555-5555";

        // Create a regular purchase
        PurchasedProduct product = new PurchasedProduct(productId, storeId, quantity, price);
        Purchase purchase = new Purchase(userId, List.of(product), price * quantity, shippingAddress, contactInfo);

        // Verify the purchase details
        assertNotNull(purchase);
        assertEquals(userId, purchase.getUserId());
        assertEquals(price * quantity, purchase.getTotalPrice());
        assertEquals(1, purchase.getProducts().size());
        assertEquals(productId, purchase.getProducts().get(0).getProductId());
        assertEquals(storeId, purchase.getProducts().get(0).getStoreId());
        assertEquals(quantity, purchase.getProducts().get(0).getQuantity());
        assertEquals(price, purchase.getProducts().get(0).getUnitPrice());
        assertEquals(shippingAddress, purchase.getShippingAddress());
        assertEquals(contactInfo, purchase.getContactInfo());

    }
}
