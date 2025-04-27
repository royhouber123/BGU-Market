package market.domain.store.Policies;

import market.domain.store.Listing;
import market.domain.store.Store;

import java.util.Map;

public class PercentageDiscountPolicy implements DiscountPolicy {

    private final double discountPercentage;
    private final Store store;

    /*
        0 < discountPrecentage < 100
     */
    public PercentageDiscountPolicy(Store store, double discountPercentage) {
        if(discountPercentage <= 0 || discountPercentage > 100) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100");
        }
        this.discountPercentage = discountPercentage;
        this.store = store;
    }

    @Override
    public double calculateDiscount(Map<String, Integer> listings) {
        double sum = 0;
        for (Map.Entry<String, Integer> entry : listings.entrySet()) {
            Listing listing = store.getListing(entry.getKey());
            if(listing != null) {
                throw new IllegalArgumentException("Listing " + entry.getKey() + " not found");
            }
            double price = listing.getPrice();
            sum += price * entry.getValue();
        }

        return sum * discountPercentage / 100;
    }
}
