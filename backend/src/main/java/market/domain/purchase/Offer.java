package market.domain.purchase;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/// Offer is a class that represents an offer made by a user
/// It contains the userId, price, shipping address, and contact info
@Entity
@Table(name = "offers")
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "price", nullable = false)
    private double price;

    @Column(name = "shipping_address", nullable = false)
    private String shippingAddress;

    @Column(name = "contact_info", nullable = false)
    private String contactInfo;

    public Offer() {}

    public Offer(String userId, double price, String shippingAddress, String contactInfo) {
        this.userId = userId;
        this.price = price;
        this.shippingAddress = shippingAddress;
        this.contactInfo = contactInfo;
    }

    public String getUserId() {
        return userId;
    }
    
    public double getPrice() {
        return price;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public String getContactInfo() {
        return contactInfo;
    }
}
