package market.domain.store;

public class StoreDTO {
    private String storeID;
    private String name;
    private boolean active;

    public StoreDTO(Store store) {
        this.storeID = store.getStoreID();
        this.name = store.getName();
        this.active = store.isActive();
    }


    public String getStoreID() {
        return storeID;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }
}
