public class StoreService {
    private IStoreRepository storeRepository;

    public void createStore(String storeName, int founderId) throws Exception {
        // ? - do we need store type
        if(storeRepository.containsStore(storeName)) {
            // LOG - error
            throw new Exception("The storeName '" + storeName + "' already exists");
        }
        Store store = new Store(storeName, founderId);
        storeRepository.addStore(store);
        //LOG - store added
    }

}
