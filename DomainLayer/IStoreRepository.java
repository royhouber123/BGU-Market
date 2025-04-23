public interface IStoreRepository {
    public Store getStore(String storeName);
    public void addStore(Store store);
    public void removeStore(String storeName);

    public boolean containsStore(String storeName);

}
