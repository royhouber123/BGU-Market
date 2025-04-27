package market.domain.store.Policies;

import market.domain.store.IStoreProductsManager;

import java.util.Map;

public class DiscountPolicyFactory {
    public static DiscountPolicy createPercentageDiscount(IStoreProductsManager store, double percentage) {
        return new PercentageDiscountPolicy(store, percentage);
    }

    public static DiscountPolicy createCouponDiscount(String couponCode, double amount) {
        return new CouponDiscountPolicy(couponCode, amount);
    }

    public static DiscountPolicy createProductPercentageDiscount(IStoreProductsManager store, Map<String, Double> productDiscounts) {
        return new ProductPrecentageDiscountPolicy(store, productDiscounts);
    }
}
