package market.domain.purchase;


import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "bids")
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "price")
    private double price;

    @Column(name = "shipping_address")
    private String shippingAddress;

    @Column(name = "contact_info")
    private String contactInfo;

    @ElementCollection
    @CollectionTable(name = "required_approvers", joinColumns = @JoinColumn(name = "bid_id"))
    @Column(name = "approver_id")
    private Set<String> requiredApprovers = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "approved_by", joinColumns = @JoinColumn(name = "bid_id"))
    @Column(name = "approver_id")
    private Set<String> approvedBy = new HashSet<>();

    private boolean approved = false;
    private boolean rejected = false;
    private boolean counterOffered = false;
    private double counterOfferAmount = -1; //no counter offer yet

    public Bid() {}  

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

    public void setApproved(boolean app) {
        this.approved = app;
    }

    public boolean isApproved() {
        return approved;
    }

    public void reject(String approverId) {
        if (requiredApprovers.contains(approverId)) {
            rejected = true;
        }
    }

    public void setRejected(boolean rej) {
        this.rejected = rej;
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

    public Long getId() {
        return id;
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

    public void setCounterOffered(boolean counter) {
        this.counterOffered = counter;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double pr) {
        this.price = pr;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public Set<String> getApprovedBy() {
        return approvedBy;
    }
}