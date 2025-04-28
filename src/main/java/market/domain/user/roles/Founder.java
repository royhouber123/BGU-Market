package market.domain.user.roles;


public class Founder implements Role {
    private String storeId;

    public Founder(String storeId) {
        this.storeId = storeId;
    }

    @Override
    public String getStoreId() {
        return storeId;
    }

    @Override
    public String getRoleName() {
        return "Founder";
    }
}
