package market.domain.policy;

import market.model.ShoppingCart;
import market.model.StoreBag;

public interface DiscountPolicy {
    void apply(StoreBag bag);
}
