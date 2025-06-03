package market.domain.store.Policies.Policies;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Policies.PurchasePolicy;
import market.dto.PolicyDTO;

public class PurchasePolicyFactory {
    public static PurchasePolicy createPolicy(String type, int value) {
        return switch (type.toLowerCase()) {
            case "minitems" -> new MinItemsPurchasePolicy(value);
            case "maxitems" -> new MaxItemsPurchasePolicy(value);
            case "minprice" -> new MinPricePurchasePolicy(value);
            default -> throw new IllegalArgumentException("Unknown Purchase Policy Type: " + type);
        };
    }

    public static PurchasePolicy fromDTO(PolicyDTO.AddPurchasePolicyRequest dto) {
        return createPolicy(dto.type(), dto.value());
    }
}
