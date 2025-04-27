package market.domain.store.Policies;

import market.domain.store.Store;

import java.util.Map;

public interface DiscountPolicy {
    double calculateDiscount(Map<String, Integer> listings);
}
