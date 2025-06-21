package market.infrastructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import market.domain.Role.Role;
import market.domain.store.IStoreRepository;
import market.domain.store.Listing;
import market.domain.store.Store;



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
    public void save(Store store) {
        storesById.put(store.getStoreID(), store);
        storesByName.put(store.getName(), store);
}


    @Override
    public String getNextStoreID() {
        if(storesById.isEmpty()) {
            return "1";
        }
        return String.valueOf(Collections.max(this.storesById.keySet().stream().map(Integer::parseInt).toList()) + 1);
    }



   /**
 * Updates the stock of multiple stores after a purchase attempt.
 * <p>
 * The method first locks all involved stores (in a consistent order to avoid deadlocks),
 * checks that sufficient stock exists for all listings, and only if all checks pass,
 * proceeds to reduce the quantities.
 * <p>
 * This operation is atomic across all the involved stores: either all succeed, or none.
 *
 * @param listForUpdateStock A map where each key is a store ID, and the value is a map of listing IDs to quantities to purchase.
 * @return {@code true} if the purchase succeeded and stock was reduced; {@code false} otherwise (e.g., if some listing has insufficient stock).
 */
public boolean updateStockForPurchasedItems(Map<String, Map<String, Integer>> listForUpdateStock) {
    List<Store> lockedStores = new ArrayList<>();

    // Defensive: make sure input is not null
    if (listForUpdateStock == null || listForUpdateStock.isEmpty()) {
        return false;
    }

    // Step 1: Acquire locks in consistent order to avoid deadlocks
    List<String> sortedStoreIds = new ArrayList<>(listForUpdateStock.keySet());
    Collections.sort(sortedStoreIds); // Sort alphabetically

    try {
        for (String storeId : sortedStoreIds) {
            Store store = storesById.get(storeId);
            if (store == null) {
                return false; // Store does not exist
            }
            synchronized (store) {
                lockedStores.add(store);
            }
        }

        // Step 2: Validate all listings have sufficient stock
        for (String storeId : sortedStoreIds) {
            Store store = storesById.get(storeId);
            Map<String, Integer> listingUpdates = listForUpdateStock.get(storeId);

            if (listingUpdates == null) {
                return false; // Defensive
            }

            for (Map.Entry<String, Integer> entry : listingUpdates.entrySet()) {
                String listingId = entry.getKey();
                int quantityRequested = entry.getValue();

                Listing listing = store.getListing(listingId);
                if (listing == null) {
                    return false; // Listing not found
                }
                if (listing.getQuantityAvailable() < quantityRequested) {
                    return false; // Not enough stock
                }
            }
        }

        // Step 3: All checks passed, perform the purchases
        for (String storeId : sortedStoreIds) {
            Store store = storesById.get(storeId);
            Map<String, Integer> listingUpdates = listForUpdateStock.get(storeId);

            for (Map.Entry<String, Integer> entry : listingUpdates.entrySet()) {
                String listingId = entry.getKey();
                int quantityRequested = entry.getValue();

                try {
                    store.purchaseFromListing(listingId, quantityRequested);
                } catch (Exception e) {
                    // Should not happen, because we checked stock already
                    return false;
                }
            }
        }
        return true; // Success!
    } finally {
        // No explicit unlock needed: synchronized blocks release when leaving the block
        // We just used locking in a safe deterministic way
    }
}


   @Override
   public Map<String, List<Role>> getUsersRoles(String userName) {
        Map<String,List<Role>> usersInfo = new HashMap<>();
        String key;

        for (Store store : this.storesById.values()) {
            key = store.getStoreID();
            if(store.getFounderID().equals(userName)){
                usersInfo.put(key,new ArrayList<>());
                usersInfo.get(key).add(Role.FOUNDER);
            }
            for(String owner:store.getAllOwners()){
                if(owner.equals(userName)){
                    if(!usersInfo.containsKey(key)){
                        usersInfo.put(key,new ArrayList<>());
                    }
                    usersInfo.get(key).add(Role.OWNER);
                    break;
                }
            }
            for(String manager:store.getAllManagersStrs()){
                if(manager.equals(userName)){
                    if(!usersInfo.containsKey(key)){
                        usersInfo.put(key,new ArrayList<>());
                    }
                    usersInfo.get(key).add(Role.MANAGER);
                    break;
                }
            }

        }
        return usersInfo;
    }

    @Override
    public List<Store> getAllActiveStores() {
    return storesById.values().stream()
            .filter(Store::isActive)
            .collect(Collectors.toList());
}

}
