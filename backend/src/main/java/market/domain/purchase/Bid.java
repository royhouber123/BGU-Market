package market.domain.purchase;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BooleanSupplier;

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

    public Bid(String userId, double price, String shippingAddress, String contactInfo, Set<String> requiredApprovers) {
        this.userId = userId;
        this.price = price;
        this.shippingAddress = shippingAddress;
        this.contactInfo = contactInfo;
        this.requiredApprovers = requiredApprovers;
    }

    public void approve(String approverId) {
        if (requiredApprovers.contains(approverId)) {
            approvedBy.add(approverId);
            if (approvedBy.containsAll(requiredApprovers)) {
                approved = true;
            }
        }
    }

    public boolean isApproved() {
        return approved;
    }

    public void reject(String approverId) {
        if (requiredApprovers.contains(approverId)) {
            rejected = true;
        }
    }

    public boolean isRejected() {
        return rejected;
    }

    public void proposeCounterOffer(double newAmount) {
        counterOffered = true;
        counterOfferAmount = newAmount;
    }

    public String getUserId() {
        return userId;
    }

    public Set<String> getRequiredApprovers() {
        return requiredApprovers;
    }

    public double getCounterOfferAmount() {
        if (!counterOffered) {
            throw new RuntimeException("No counter offer has been made yet.");
        }
        return counterOfferAmount;
    }

    public void setCounterOfferAmount(double counterOfferAmount) {
        counterOffered = true;
        this.counterOfferAmount = counterOfferAmount;
    }

    public Boolean isCounterOffered() {
        return counterOffered;
    }

    public double getPrice() {
        return price;
    }
}