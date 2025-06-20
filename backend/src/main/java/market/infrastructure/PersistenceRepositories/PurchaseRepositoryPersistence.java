package market.infrastructure.PersistenceRepositories;

import jakarta.transaction.Transactional;
import market.domain.purchase.IPurchaseRepository;
import market.domain.purchase.Purchase;
import market.domain.purchase.PurchasedProduct;
import market.infrastructure.IJpaRepository.IPurchaseJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Primary
@Repository
@Transactional
public class PurchaseRepositoryPersistence implements IPurchaseRepository {

    @Autowired
    private IPurchaseJpaRepository purchaseJpaRepository;

    @Override
    public void save(Purchase purchase) {
        purchaseJpaRepository.save(purchase);
    }

    /**
     * Retrieves all purchases made by a specific user.
     * Each returned Purchase object contains all products in the transaction,
     * regardless of which stores were involved.
     *
     * @param userId The ID (username) of the user to retrieve purchases for
     * @return List of Purchase objects made by the specified user
     */
    @Override
    public List<Purchase> getPurchasesByUser(String userId) {
        return purchaseJpaRepository.findByUserId(userId);
    }

    /**
     * Retrieves all purchases that contain at least one product from the specified store.
     * The returned Purchase objects include all products in the transaction,
     * regardless of which store sold them.
     *
     * @param storeId The ID of the store to filter purchases by
     * @return List of Purchase objects including at least one product from the store
     */
    @Override
    public List<Purchase> getPurchasesByStore(String storeId) {
        return purchaseJpaRepository.findByStoreId(storeId);
    }
}
