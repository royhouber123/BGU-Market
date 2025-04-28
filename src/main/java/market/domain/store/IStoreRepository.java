package market.domain.store;

public interface IStoreRepository {
    public Store getStoreByName(String storeName);
    public Store getStoreByID(String storeID);
    public void addStore(Store store) throws Exception;
    public void removeStore(String storeName) throws Exception;

    public boolean containsStore(String storeName);

    public String getNextStoreID();

}
