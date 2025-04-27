package market.domain.purchase;

public class PurchasedProduct {
    private final String productId;
    private final String storeId;
    private final String listingId;
    private final int quantity;
    private final double unitPrice;
    private double discount;


    public PurchasedProduct(String productId, String storeId, String listingId, int quantity, double unitPrice, double discount) {
        this.productId = productId;
        this.storeId = storeId;
        this.listingId = listingId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discount = discount;
    }

    public String getProductId() {
        return productId;
    }

    public String getStoreId() {
        return storeId;
    }

    public String getListingId() {
        return listingId;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getTotalPrice() {
        return unitPrice * quantity * (1-discount/100);
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }
}