package market.domain.user;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A container for all products a user picked from a single store.
 */
public class StoreBag {

    private final String storeId;
    /** key = productName, value = (quantity, purchaseType) */
    private final Map<String, Integer> products = new HashMap<>();

    public StoreBag(String storeId) {
        this.storeId = storeId;
    }


    public String getStoreId() {
        return storeId;
    }

    /** Unmodifiable view of product → (qty, type) */
    public Map<String, Integer> getProducts() {
        return Collections.unmodifiableMap(products);
    }

    public void addProduct(String productName, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to add must be positive");
        }
        products.merge(productName, quantity, Integer::sum);
    }

    public void removeProduct(String productName, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to remove must be positive");
        }
        Integer existing = products.get(productName);
        if (existing == null) {
            return;
        }
        if (quantity >= existing) {
            products.remove(productName);
        } else {
            products.put(productName, existing - quantity);
        }
    }

    public int getProductQuantity(String productName)
    {
        return this.products.get(productName);
    }

    public Map<String, Integer> getProductQuantities() {
        return Collections.unmodifiableMap(products);
    }

    public void clear() {
        products.clear();
    }
}