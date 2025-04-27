package market.domain.user.roles;


public class Owner implements Role {
    private int storeId;

    public Owner(int storeId) {
        this.storeId = storeId;
    }

    @Override
    public int getStoreId() {
        return storeId;
    }

    @Override
    public String getRoleName() {
        return "Owner";
    }
}