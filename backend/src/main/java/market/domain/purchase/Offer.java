package market.domain.purchase;

/// Offer is a class that represents an offer made by a user
/// It contains the userId, price, shipping address, and contact info
public class Offer {
    public String userId;
    public double price;
    public String shippingAddress;
    public String contactInfo;
    // Add payment details
    public String currency;
    public String cardNumber;
    public String month;
    public String year;
    public String holder;
    public String ccv;

    public Offer(String userId, double price, String shippingAddress, String contactInfo, String currency, String cardNumber, String month, String year, String holder, String ccv) {
        this.userId = userId;
        this.price = price;
        this.shippingAddress = shippingAddress;
        this.contactInfo = contactInfo;
        this.currency = currency;
        this.cardNumber = cardNumber;
        this.month = month;
        this.year = year;
        this.holder = holder;
        this.ccv = ccv;
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
