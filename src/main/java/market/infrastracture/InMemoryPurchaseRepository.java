package market.infrastructure;

import market.domain.purchase.IPurchaseRepository;
import market.model.Purchase;
import market.model.PurchasedProduct;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryPurchaseRepository implements IPurchaseRepository {

    private final List<Purchase> allPurchases = new CopyOnWriteArrayList<>();

    @Override
    public void save(Purchase purchase) {
        allPurchases.add(purchase);
    }

    @Override
    public List<Purchase> getPurchasesByUser(String userId) {
        List<Purchase> result = new ArrayList<>();
        for (Purchase p : allPurchases) {
            if (p.getUserId().equals(userId)) {
                result.add(p);
            }
        }
        return result;
    }

    @Override
    public List<Purchase> getPurchasesByStore(String storeId) {
        List<Purchase> result = new ArrayList<>();
        for (Purchase p : allPurchases) {
            for (PurchasedProduct product : p.getProducts()) {
                if (product.getStoreId().equals(storeId)) {
                    result.add(p);
                    break;
                }
            }
        }
        return result;
    }
}
