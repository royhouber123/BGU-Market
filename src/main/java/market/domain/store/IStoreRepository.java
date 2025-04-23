package market.domain.store;

public interface IStoreRepository {
    public Store getStoreByName(String storeName);
    public Store getStoreByID(int storeID);
    public void addStore(Store store);
    public void removeStore(String storeName);

    public boolean containsStore(String storeName);

}
