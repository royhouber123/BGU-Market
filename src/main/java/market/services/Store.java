package market.services;

import market.model.StoreBag;
import market.domain.policy.DiscountPolicy;
import market.domain.policy.PurchasePolicy;

public interface Store {
    PurchasePolicy getPurchasePolicy();
    DiscountPolicy getDiscountPolicy();
}
