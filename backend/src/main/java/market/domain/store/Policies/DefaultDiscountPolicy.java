package market.domain.store.Policies;

import market.domain.store.Store;

import java.util.Map;

// The default policy: no discount
public class DefaultDiscountPolicy implements DiscountPolicy {
    @Override
    public double calculateDiscount(Map<String, Integer> listings) {
        return 0;
    }
}
