package market.domain.store;

public class StoreDTO {
    private int storeID;
    private String name;
    private boolean active;

    public StoreDTO(Store store) {
        this.storeID = store.getStoreID();
        this.name = store.getName();
        this.active = store.isActive();
    }


    public int getStoreID() {
        return storeID;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }
}
