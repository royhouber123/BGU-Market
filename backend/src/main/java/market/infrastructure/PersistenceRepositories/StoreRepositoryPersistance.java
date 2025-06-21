package market.infrastructure.PersistenceRepositories;

import market.domain.store.IStoreRepository;
import market.domain.store.Store;
import market.infrastructure.IJpaRepository.IStoreJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import market.domain.store.IListingRepository;
import java.util.HashMap;
import java.util.ArrayList;



import java.util.List;
import java.util.Map;

@Primary
@Repository("storeRepositoryJpa")
@Transactional
public class StoreRepositoryPersistance implements IStoreRepository {

    @Autowired
    private IStoreJpaRepository storeJpaRepository;

    @Autowired
    private IListingRepository listingRepository;


    @Override
    public Store getStoreByID(String storeID) {
        Store store = storeJpaRepository.findById(storeID).orElse(null);
        if (store != null) {
            store.initializeAfterLoad(listingRepository);
        }
        return store;
    }


    @Override
    public void save(Store store) {
        store.regenerateStoreRolesTable();
        storeJpaRepository.save(store);
    }

    @Override
    public void addStore(Store store) throws Exception {
        if (storeJpaRepository.existsById(store.getStoreID())) {
            throw new Exception("Store with ID " + store.getStoreID() + " already exists.");
        }
        store.regenerateStoreRolesTable();
        storeJpaRepository.save(store);
    }

    @Override
    public Store getStoreByName(String storeName) {
        Store store = storeJpaRepository.findByName(storeName).orElse(null);
        if (store != null) {
            store.initializeAfterLoad(listingRepository);
        }
        return store;
    }


    @Override
    public void removeStore(String storeName) throws Exception {
        Store store = storeJpaRepository.findByName(storeName).orElse(null);
        if (store == null) {
            throw new Exception("Store with name '" + storeName + "' does not exist.");
        }
        storeJpaRepository.delete(store);
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
    public Map<String, List<market.domain.Role.Role>> getUsersRoles(String userName) {
        List<Store> stores = storeJpaRepository.findStoresByUserId(userName);
        Map<String, List<market.domain.Role.Role>> result = new HashMap<>();

        for (Store store : stores) {
            store.initializeAfterLoad(listingRepository); 

            List<market.domain.Role.Role> roles = new ArrayList<>();

            if (store.isOwner(userName)) {
                roles.add(market.domain.Role.Role.OWNER);
            }

            if (store.isManager(userName)) {
                roles.add(market.domain.Role.Role.MANAGER);
            }

            if (!roles.isEmpty()) {
                result.put(store.getStoreID(), roles);
            }
        }

        return result;
    }


    @Override
    public List<Store> getAllActiveStores() {
        List<Store> stores = storeJpaRepository.findByActiveTrue();
        for (Store store : stores) {
            store.initializeAfterLoad(listingRepository); 
        }
        return stores;
    }


     @Override
    public boolean updateStockForPurchasedItems(Map<String, Map<String, Integer>> listForUpdateStock) {
        throw new UnsupportedOperationException();
    }

    
}
