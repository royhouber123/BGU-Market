package market.domain.purchase;

import java.util.Objects;

/// AuctionKey is a combination of storeId and productId
/// This is used to identify auctions uniquely
public class AuctionKey {

    String storeId;
    String productId;

    AuctionKey(String storeId, String productId) {
        this.storeId = storeId;
        this.productId = productId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuctionKey)) return false;
        AuctionKey other = (AuctionKey) o;
        return storeId.equals(other.storeId) && productId.equals(other.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeId, productId);
    }
}