package market.domain.user;
import java.util.Collections;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ShoppingCart {
    private Map<Integer, StoreBag> storeBags = new HashMap<>();

    public ShoppingCart(){
        this.storeBags = new HashMap<>();
    }

    public boolean addProduct(int storeId, String productId,  int quantity) {
        storeBags.computeIfAbsent(storeId, StoreBag::new)
                 .addProduct(productId , quantity);
        return true;
    }

    public boolean removeProduct(int storeId, String productId,  int quantity) {
        StoreBag bag = storeBags.get(storeId);
        if (bag != null) {
            bag.removeProduct(productId, quantity);
            return true;
        }
        return false;
    }

    public StoreBag getStoreBag(int storeId) {
        return storeBags.get(storeId);
    }

    public Collection<StoreBag> getAllStoreBags() {
        return Collections.unmodifiableCollection(storeBags.values());
    }

    public void clear(){
        this.storeBags = new HashMap<>();
    }
}
