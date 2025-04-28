package market.domain.user.roles;

public class Manager implements Role {
    private String storeId;

    public Manager(String storeId) {
        this.storeId = storeId;
    }

    @Override
    public String getStoreId() {
        return this.storeId;
    }

    @Override
    public String getRoleName() {
        return "Manager";
    }
}