package market.domain.purchase;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor 
@Embeddable
public class PurchasedProduct {
    private String productId;
    private String storeId;
    private int quantity;
    private double unitPrice;

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