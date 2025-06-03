package market.domain.store.Policies.Policies;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Listing;
import market.domain.store.Policies.PurchasePolicy;
import market.dto.PolicyDTO;

import java.util.Map;
import java.util.Objects;

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
    public PolicyDTO.AddPurchasePolicyRequest toDTO() {
        return new PolicyDTO.AddPurchasePolicyRequest("MINPRICE", minPrice);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        MinPricePurchasePolicy that = (MinPricePurchasePolicy) obj;
        return minPrice == that.minPrice;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(minPrice);
    }
}
