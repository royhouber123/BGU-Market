package market.infrastructure.PersistenceRepositories;

import market.domain.store.IStoreRepository;
import market.domain.store.Store;
import market.infrastructure.IJpaRepository.IStoreJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Primary
@Repository("storeRepositoryJpa")
@Transactional
public class StoreRepositoryPersistance implements IStoreRepository {

    @Autowired
    private IStoreJpaRepository storeJpaRepository;

    @Override
    public Store getStoreByID(String storeID) {
        return storeJpaRepository.findById(storeID).orElse(null);
    }

    @Override
    public void save(Store store) {
        storeJpaRepository.save(store);
    }

    @Override
    public void addStore(Store store) throws Exception {
        if (storeJpaRepository.existsById(store.getStoreID())) {
            throw new Exception("Store with ID " + store.getStoreID() + " already exists.");
        }
        storeJpaRepository.save(store);
    }

    // את השאר נשאיר unimplemented לעכשיו

    @Override
    public Store getStoreByName(String storeName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeStore(String storeName) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsStore(String storeName) {
        return storeJpaRepository.existsByName(storeName);
    }


    @Override
    public String getNextStoreID() {
        return String.valueOf(System.currentTimeMillis());
    }

    @Override
    public boolean updateStockForPurchasedItems(Map<String, Map<String, Integer>> listForUpdateStock) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, List<market.domain.Role.Role>> getUsersRoles(String userName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Store> getAllActiveStores() {
        throw new UnsupportedOperationException();
    }

    
}
