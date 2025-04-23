package market.domain.policy;

import market.model.ShoppingCart;
import market.model.StoreBag;

public interface PurchasePolicy {
    void validate(String userId, StoreBag bag) throws RuntimeException;
}
