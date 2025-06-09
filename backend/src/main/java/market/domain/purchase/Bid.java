package market.domain.purchase;

import java.util.HashSet;
import java.util.Set;

public class Bid {
    public String userId;
    public double price;
    public String shippingAddress;
    public String contactInfo;
    public Set<String> requiredApprovers;
    public Set<String> approvers;
    public boolean approved;
    public boolean rejected;
    public boolean counterOffered;
    public double counterOfferAmount;
    
    // Add payment details
    public String currency;
    public String cardNumber;
    public String month;
    public String year;
    public String holder;
    public String ccv;

    public Bid(String userId, double price, String shippingAddress, String contactInfo, Set<String> requiredApprovers, String currency, String cardNumber, String month, String year, String holder, String ccv) {
        this.userId = userId;
        this.price = price;
        this.shippingAddress = shippingAddress;
        this.contactInfo = contactInfo;
        this.requiredApprovers = new HashSet<>(requiredApprovers);
        this.approvers = new HashSet<>();
        this.approved = false;
        this.rejected = false;
        this.counterOffered = false;
        this.counterOfferAmount = 0.0;
        this.currency = currency;
        this.cardNumber = cardNumber;
        this.month = month;
        this.year = year;
        this.holder = holder;
        this.ccv = ccv;
    }

    public String getUserId() {
        return userId;
    }

    public double getPrice() {
        return price;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public String getCurrency() {
        return currency;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getMonth() {
        return month;
    }

    public String getYear() {
        return year;
    }

    public String getHolder() {
        return holder;
    }

    public String getCcv() {
        return ccv;
    }

    // Add missing getter methods
    public Set<String> getRequiredApprovers() {
        return requiredApprovers;
    }

    public Set<String> getApprovedBy() {
        return approvers;
    }

    public boolean isApproved() {
        return approved;
    }

    public boolean isRejected() {
        return rejected;
    }

    public boolean isCounterOffered() {
        return counterOffered;
    }

    public double getCounterOfferAmount() {
        return counterOfferAmount;
    }

    // Add business logic methods
    public void approve(String approverId) {
        if (requiredApprovers.contains(approverId)) {
            approvers.add(approverId);
            // Check if all required approvers have approved
            if (approvers.containsAll(requiredApprovers)) {
                this.approved = true;
            }
        }
    }

    public void reject(String approverId) {
        if (requiredApprovers.contains(approverId)) {
            this.rejected = true;
        }
    }

    public void proposeCounterOffer(double amount) {
        this.counterOffered = true;
        this.counterOfferAmount = amount;
    }
}