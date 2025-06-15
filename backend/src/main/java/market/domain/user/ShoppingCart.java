package market.domain.user;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a shopping cart containing products from multiple stores.
 * Each store has its own StoreBag that tracks products and their quantities.
 */
public class ShoppingCart {
    private Map<String, StoreBag> storeBags = new HashMap<>();

    public ShoppingCart(){
        this.storeBags = new HashMap<>();
    }

    public void setStoreBags(Map<String, StoreBag> storeBags) {
        this.storeBags = storeBags;
    }
    
    public Map<String, StoreBag> getStoreBags() {
        return storeBags;
    }

    /**
     * Adds a product to the shopping cart.
     * If the store doesn't exist in the cart, a new StoreBag is created.
     *
     * @param storeId    The ID of the store
     * @param productId  The ID of the product to add
     * @param quantity   The quantity to add (must be positive)
     * @return true if the product was successfully added
     */
    public boolean addProduct(String storeId, String productId,  int quantity) {
        storeBags.computeIfAbsent(storeId, StoreBag::new)
                 .addProduct(productId , quantity);
        return true;
    }

    /**
     * Removes a product from the shopping cart.
     * Throws exceptions for invalid operations to ensure data integrity.
     *
     * @param storeId    The ID of the store
     * @param productId  The ID of the product to remove
     * @param quantity   The quantity to remove (must be positive)
     * @throws IllegalArgumentException if the store doesn't exist, product doesn't exist,
     *         or trying to remove more than the available quantity
     */
    public void removeProduct(String storeId, String productId, int quantity) {
        // Validate quantity
        if (quantity <= 0) {
            throw new IllegalArgumentException("Removal quantity must be positive");
        }
        
        // Check if store exists
        StoreBag bag = storeBags.get(storeId);
        if (bag == null) {
            throw new IllegalArgumentException("Store '" + storeId + "' not found in shopping cart");
        }
        
        // Check if product exists in the store bag
        Map<String, Integer> productQuantities = bag.getProductQuantities();
        if (!productQuantities.containsKey(productId)) {
            throw new IllegalArgumentException("Product '" + productId + "' not found in store '" + storeId + "'");
        }
        
        // Check if trying to remove more than available
        int currentQuantity = productQuantities.get(productId);
        if (quantity > currentQuantity) {
            throw new IllegalArgumentException(
                "Cannot remove " + quantity + " items of product '" + productId + 
                "', only " + currentQuantity + " available");
        }
        
        // Remove the product
        bag.removeProduct(productId, quantity);
        
        // If the store bag becomes empty, remove it from the shopping cart
        if (bag.getProductQuantities().isEmpty()) {
            storeBags.remove(storeId);
        }
    }

    /**
     * Gets a specific store bag from the shopping cart.
     *
     * @param storeId The ID of the store
     * @return The StoreBag for the given store, or null if not found
     */
    public StoreBag getStoreBag(String storeId) {
        return storeBags.get(storeId);
    }

    /**
     * Gets all store bags in the shopping cart.
     *
     * @return An unmodifiable collection of all StoreBags
     */
    @JsonIgnore
    public Collection<StoreBag> getAllStoreBags() {
        return Collections.unmodifiableCollection(storeBags.values());
    }

    /**
     * Clears the shopping cart, removing all store bags and products.
     */
    public void clear() {
        this.storeBags = new HashMap<>();
    }
}
