package market.infrastructure;

import jakarta.transaction.Transactional;
import market.domain.purchase.IPurchaseRepository;
import market.domain.purchase.Purchase;
import market.domain.purchase.PurchasedProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional
public class PurchaseRepositoryPersistence implements IPurchaseRepository {

    @Autowired
    private IPurchaseJpaRepository purchaseJpaRepository;

    @Override
    public void save(Purchase purchase) {
        purchaseJpaRepository.save(purchase);
    }

    @Override
    public List<Purchase> getPurchasesByUser(String userId) {
        return purchaseJpaRepository.findByUserId(userId);
    }

    @Override
    public List<Purchase> getPurchasesByStore(String storeId) {
        List<Purchase> all = purchaseJpaRepository.findAll();
        List<Purchase> result = new ArrayList<>();
        for (Purchase p : all) {
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
