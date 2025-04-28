package market.domain.purchase;

import java.util.HashSet;
import java.util.Set;

public class Bid {

    String userId;
    double price;
    String shippingAddress;
    String contactInfo;
    Set<String> requiredApprovers;
    Set<String> approvedBy = new HashSet<>();
    boolean approved = false;
    boolean rejected = false;
    boolean counterOffered = false;
    double counterOfferAmount = -1; //no counter offer yet

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

    void reject(String approverId) {
        if (requiredApprovers.contains(approverId)) {
            rejected = true;
        }
    }

    boolean isRejected() {
        return rejected;
    }

    void proposeCounterOffer(double newAmount) {
        counterOffered = true;
        counterOfferAmount = newAmount;
    }
}