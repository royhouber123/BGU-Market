package market.domain.store.Policies;

import market.domain.store.Store;

import java.util.ArrayList;
import java.util.List;

public class PolicyHandler {

    private final Store store;

    private List<PurchasePolicy> policies;
    private List<DiscountPolicy> discounts;

    public PolicyHandler(Store store) {
        this.store = store;
        policies = new ArrayList<>();
        discounts = new ArrayList<>();
    }
}
