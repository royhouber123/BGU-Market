package market.domain.purchase;

import market.model.Purchase;

import java.util.List;

public interface IPurchaseRepository {
    void save(Purchase purchase);

    List<Purchase> getPurchasesByUser(String userId);

    List<Purchase> getPurchasesByStore(String storeId);
}
