package market.domain.purchase;

import java.util.Objects;

public class BidKey {
    String storeId;
    String productId;

    public BidKey(String storeId, String productId) {
        this.storeId = storeId;
        this.productId = productId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BidKey)) return false;
        BidKey other = (BidKey) o;
        return Objects.equals(storeId, other.storeId) && Objects.equals(productId, other.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeId, productId);
    }
}
