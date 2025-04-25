package market.domain.purchase;
import java.util.HashSet;
import java.util.Set;

import market.model.*;

public class BidPurchase implements IPurchase {
    
    private static class Bid {
        String userId;
        double price;
        String shippingAddress;
        String contactInfo;
        Set<String> requiredApprovers;
        Set<String> approvedBy = new HashSet<>();
        boolean approved = false;

        Bid(String userId, double price, String shippingAddress, String contactInfo, Set<String> requiredApprovers) {
            this.userId = userId;
            this.price = price;
            this.shippingAddress = shippingAddress;
            this.contactInfo = contactInfo;
            this.requiredApprovers = requiredApprovers;
        }

        void approve(String approverId) {
            if (requiredApprovers.contains(approverId)) {
                approvedBy.add(approverId);
                if (approvedBy.containsAll(requiredApprovers)) {
                    approved = true;
                }
            }
        }

        boolean isApproved() {
            return approved;
        }
    }

    
    
    
    
    @Override
    public Purchase purchase(String userId, ShoppingCart cart, String shippingAddress, String contactInfo) {
        // TODO: Implement
        return null;
    }
}
