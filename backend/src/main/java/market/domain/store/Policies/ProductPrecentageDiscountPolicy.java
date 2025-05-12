package market.domain.store.Policies;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Listing;
import market.domain.store.Store;

import java.util.HashMap;
import java.util.Map;

public class ProductPrecentageDiscountPolicy implements DiscountPolicy {

    Map<String, Double> productDiscountPrecentage;
    IStoreProductsManager store;

    public ProductPrecentageDiscountPolicy(IStoreProductsManager store, Map<String, Double> productDiscountPrecentage) {

        for(Map.Entry<String, Double> entry : productDiscountPrecentage.entrySet()) {
            if(store.getListingById(entry.getKey()) == null) {
                throw new IllegalArgumentException("Product ID " + entry.getKey() + " does not exist");
            }
            if(entry.getValue() < 0 || entry.getValue() > 100) {
                throw new IllegalArgumentException("Product discount is out of range (" + entry.getValue() + ")");
            }
        }

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
                Listing l = store.getListingById(prodId);
                discount += l.getPrice() * quantity * discountEntry.getValue()/100;
            }
        }

        return discount;
    }
}
