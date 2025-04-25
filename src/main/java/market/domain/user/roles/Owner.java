package market.domain.user.roles;


public class Owner implements Role {
    private String storeId;

    public Owner(String storeId) {
        this.storeId = storeId;
    }

    @Override
    public String getStoreId() {
        return storeId;
    }

    @Override
    public String getRoleName() {
        return "Owner";
    }
}