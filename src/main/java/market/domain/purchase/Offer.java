package market.domain.purchase;

/// Offer is a class that represents an offer made by a user
/// It contains the userId, price, shipping address, and contact info
public class Offer {
    String userId;
    double price;
    String shippingAddress;
    String contactInfo;

    Offer(String userId, double price, String shippingAddress, String contactInfo) {
        this.userId = userId;
        this.price = price;
        this.shippingAddress = shippingAddress;
        this.contactInfo = contactInfo;
    }
}