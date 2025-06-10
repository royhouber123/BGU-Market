package market.domain.user;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("SUBSCRIBER")
public class Subscriber extends User {

    @Column(name = "shipping_address")
    private String shippingAddress;

    /**
     * Default constructor for JPA
     */
    public Subscriber() {
        super();
        this.shippingAddress = "";
    }

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
