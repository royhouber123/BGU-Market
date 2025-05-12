package market.domain.user;

public class Subscriber extends User {

    private String shippingAddress;

    public Subscriber(String userName) {
        super(userName);
        this.shippingAddress = "";
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}
