package market.domain.store;

import java.util.List;
import java.util.Map;

import market.domain.Role.Role;

public interface IStoreRepository {
    public Store getStoreByName(String storeName);
    public Store getStoreByID(String storeID);
    public void addStore(Store store) throws Exception;
    public void removeStore(String storeName) throws Exception;

    public boolean containsStore(String storeName);

    public String getNextStoreID();
    public boolean updateStockForPurchasedItems(Map<String, Map<String, Integer>> listForUpdateStock);

    public Map<String,List<Role>> getUsersRoles(String userName);
}
