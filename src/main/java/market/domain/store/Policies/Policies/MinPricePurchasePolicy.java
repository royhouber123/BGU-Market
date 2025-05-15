package market.domain.store.Policies.Policies;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Listing;
import market.domain.store.Store;
import market.domain.store.Policies.PurchasePolicy;
import market.dto.AddPurchasePolicyDTO;

import java.util.Map;

public class MinPricePurchasePolicy implements PurchasePolicy {

    int minPrice;

    public MinPricePurchasePolicy(int minPrice) {
        if (minPrice < 0) {
            throw new IllegalArgumentException("minPrice cannot be negative");
        }
        this.minPrice = minPrice;
    }


    @Override
    public boolean isPurchaseAllowed(Map<String, Integer> listings, IStoreProductsManager productManager) {
        Listing l;
        double price = 0;
        for (Map.Entry<String, Integer> entry : listings.entrySet()) {
            l = productManager.getListingById(entry.getKey());
            if(l == null) {
                throw new IllegalArgumentException("Listing " + entry.getKey() + " not found");
            }
            price += entry.getValue() * l.getPrice();
        }
        return price >= minPrice;
    }

    @Override
    public AddPurchasePolicyDTO toDTO() {
        return new AddPurchasePolicyDTO("MINPRICE", minPrice);
    }
}
