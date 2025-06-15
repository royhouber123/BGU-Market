package market.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A container for all products a user picked from a single store.
 */
public class StoreBag {

    private String storeId;
    /** key = productName, value = (quantity, purchaseType) */
    private Map<String, Integer> products = new HashMap<>();

    public StoreBag() {}

    public StoreBag(String storeId) {
        this.storeId = storeId;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public Map<String, Integer> getProducts() {
        return products;
    }

    public void setProducts(Map<String, Integer> products) {
        this.products = products;
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

    @JsonIgnore
    public Map<String, Integer> getProductQuantities() {
        return Collections.unmodifiableMap(products);
    }

    public void clear() {
        products.clear();
    }
}