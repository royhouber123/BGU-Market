package market.domain.user.roles;


public class Founder implements Role {
    private int storeId;

    public Founder(int storeId) {
        this.storeId = storeId;
    }

    @Override
    public int getStoreId() {
        return storeId;
    }

    @Override
    public String getRoleName() {
        return "Founder";
    }
}
