package market.domain.purchase;

public class PurchasedProduct {
    private final String productId;
    private final String storeId;
    private final int quantity;
    private final double unitPrice;

    public PurchasedProduct(String productId, String storeId, int quantity, double unitPrice) {
        this.productId = productId;
        this.storeId = storeId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getProductId() {
        return productId;
    }

    public String getStoreId() {
        return storeId;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }
}