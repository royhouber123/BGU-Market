package market.domain.store.Policies;

import market.domain.store.IStoreProductsManager;

public class PurchasePolicyFactory {
    public static PurchasePolicy createPolicy(String type, IStoreProductsManager store, int value) {
        return switch (type.toLowerCase()) {
            case "minitems" -> new MinItemsPurchasePolicy(value);
            case "maxitems" -> new MaxItemsPurchasePolicy(value);
            case "minprice" -> new MinPricePurchasePolicy(store, value);
            default -> throw new IllegalArgumentException("Unknown Purchase Policy Type: " + type);
        };
    }
}
