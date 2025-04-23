package market.model;

import java.time.LocalDateTime;
import java.util.List;

public class Purchase {
    private final String userId;
    private final List<PurchasedProduct> products;
    private final double totalPrice;
    private final String shippingAddress;
    private final String contactInfo;
    private final LocalDateTime timestamp;

    public Purchase(String userId, List<PurchasedProduct> products, double totalPrice,
                    String shippingAddress, String contactInfo) {
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
}
