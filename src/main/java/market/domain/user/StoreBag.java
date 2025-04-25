package market.domain.user;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StoreBag {
    private final String storeId;
    private final Map<String, Integer> products = new HashMap<>();

    public StoreBag(String storeId) {
        this.storeId = storeId;
    }

    public String getStoreId() {
        return storeId;
    }

    public void addProduct(String productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to add must be positive");
        }
        products.merge(productId, quantity, Integer::sum);
    }

    public void removeProduct(String productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to remove must be positive");
        }
        Integer existing = products.get(productId);
        if (existing == null) {
            return;
        }
        if (quantity >= existing) {
            products.remove(productId);
        } else {
            products.put(productId, existing - quantity);
        }
    }

    public Map<String, Integer> getProductQuantities() {
        return Collections.unmodifiableMap(products);
    }

    public void clear() {
        products.clear();
    }
}
