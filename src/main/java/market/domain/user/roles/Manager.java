package market.domain.user.roles;

public class Manager implements Role {
    private int storeId;

    public Manager(int storeId) {
        this.storeId = storeId;
    }

    @Override
    public int getStoreId() {
        return this.storeId;
    }

    @Override
    public String getRoleName() {
        return "Manager";
    }
}