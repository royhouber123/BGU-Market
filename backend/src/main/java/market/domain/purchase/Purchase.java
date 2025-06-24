package market.domain.purchase;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "purchases")
public class Purchase {

    @Id
    @Column(name = "purchase_id", nullable = false, unique = true)
    private String purchaseId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @ElementCollection(fetch = FetchType.EAGER)    
    @CollectionTable(name = "purchased_products", joinColumns = @JoinColumn(name = "purchase_id"))
    private List<PurchasedProduct> products;

    @Column(name = "total_price", nullable = false)
    private double totalPrice;

    @Column(name = "shipping_address")
    private String shippingAddress;

    @Column(name = "contact_info")
    private String contactInfo;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    public Purchase(String userId, List<PurchasedProduct> products, double totalPrice,
                    String shippingAddress, String contactInfo) {
        this.purchaseId = UUID.randomUUID().toString(); //for the JPA
        this.userId = userId;
        this.products = products;
        this.totalPrice = totalPrice;
        this.shippingAddress = shippingAddress;
        this.contactInfo = contactInfo;
        this.timestamp = LocalDateTime.now();
    }

    public String getUserId() {
        return userId;
    }

    public List<PurchasedProduct> getProducts() {
        return products;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getPurchaseId() {
        return purchaseId;
    }
}
