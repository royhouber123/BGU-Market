package market.domain.store.Policies;

import market.domain.store.Listing;
import market.domain.store.Store;

import java.util.HashMap;
import java.util.Map;

public class ProductPrecentageDiscountPolicy implements DiscountPolicy {

    Map<String, Double> productDiscountPrecentage;
    Store store;

    public ProductPrecentageDiscountPolicy(Store store, Map<String, Double> productDiscountPrecentage) {
        for(String prodID : productDiscountPrecentage.keySet()) {
            if(store.getListing(prodID) == null) {
                throw new IllegalArgumentException("Product ID " + prodID + " does not exist");
            }
        }
        //TODO: check discounts between 0 and 100
        this.productDiscountPrecentage = productDiscountPrecentage;
        this.store = store;
    }

    @Override
    public double calculateDiscount(Map<String, Integer> listings) {
        String prodId;
        Integer quantity;
        double discount = 0.0;
        for (Map.Entry<String, Integer> entry : listings.entrySet()) {
            prodId = entry.getKey();
            quantity = entry.getValue();

            for(Map.Entry<String,Double> discountEntry : productDiscountPrecentage.entrySet()) {
                if(prodId != discountEntry.getKey()){
                    continue;
                }
                Listing l = store.getListing(prodId);
                discount += l.getPrice() * quantity * discountEntry.getValue()/100;
            }
        }

        return discount;
    }
}
