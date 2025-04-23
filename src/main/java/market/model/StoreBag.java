package market.model;

import java.util.Collection;
import java.util.Map;

public class StoreBag {
    private final String storeId;
    private final Map<String, CartItem> items;

    public StoreBag(String storeId, Map<String, CartItem> items) {
        this.storeId = storeId;
        this.items = items;
    }

    public String getStoreId() {
        return storeId;
    }

    public Collection<CartItem> getItems() {
        return items.values();
    }

    public Map<String, CartItem> getItemsMap() {
        return items;
    }
}
