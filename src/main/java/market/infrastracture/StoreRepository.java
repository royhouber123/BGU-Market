package market.infrastructure;


import market.domain.store.IStoreRepository;
import market.domain.store.Store;


import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;

public class StoreRepository implements IStoreRepository {

    private final HashMap<String, Store> storesByName = new HashMap<>();
    private final HashMap<String, Store> storesById = new HashMap<>();


    @Override
    public Store getStoreByName(String storeName) {
        if(storesByName.containsKey(storeName)) {
            return storesByName.get(storeName);
        }
        return null;
    }


    @Override
    public Store getStoreByID(String storeID) {
        if(storesById.containsKey(storeID)) {
            return storesById.get(storeID);
        }
        return null;
    }

    @Override
    public void addStore(Store store) throws Exception {
        String id = store.getStoreID();
        String storeName = store.getName();
        if(storesByName.containsKey(storeName)) {
            throw new Exception("Store with name " + storeName + " already exists");
        }
        if(storesById.containsKey(id)) {
            throw new Exception("Store with id " + id + " already exists");
        }
        storesById.put(id, store);
        storesByName.put(storeName, store);
    }

    @Override
    public void removeStore(String storeName) throws Exception {
        Store s = getStoreByName(storeName);
        if(s == null)
            throw new Exception("Store '" + storeName +"' doesn't exist in repository");
        String id = s.getStoreID();
        storesById.remove(id);
        storesByName.remove(storeName);
    }

    @Override
    public boolean containsStore(String storeName) {
        return storesByName.containsKey(storeName);
    }

    @Override
    public String getNextStoreID() {
        if(storesById.isEmpty()) {
            return "1";
        }
        return String.valueOf(Collections.max(this.storesById.keySet().stream().map(Integer::parseInt).toList()) + 1);
    }

}
