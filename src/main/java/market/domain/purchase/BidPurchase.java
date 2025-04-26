package market.domain.purchase;
import market.domain.user.ShoppingCart;
import market.domain.user.StoreBag;

import java.util.*;


public class BidPurchase implements IPurchase {
    
    private static class BidKey {
        String storeId;
        String productId;

        BidKey(String storeId, String productId) {
            this.storeId = storeId;
            this.productId = productId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BidKey)) return false;
            BidKey other = (BidKey) o;
            return storeId.equals(other.storeId) && productId.equals(other.productId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(storeId, productId);
        }
    }

    private static class Bid {
        String userId;
        double price;
        String shippingAddress;
        String contactInfo;
        Set<String> requiredApprovers;
        Set<String> approvedBy = new HashSet<>();
        boolean approved = false;
        boolean rejected = false;
        boolean counterOffered = false;
        double counterOfferAmount = -1; // ברירת מחדל – אין הצעת נגד

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
    
    
    private static final Map<BidKey, List<Bid>> bids = new HashMap<>();

    private static final Map<String, List<String>> pendingNotifications = new HashMap<>();


    private static BidKey buildKey(String storeId, String productId) {
        return new BidKey(storeId, productId);
    }



    /**
     * Submits a new bid for a product.
     * 
     * The subscriber (user) provides a price offer, shipping address, and contact information.
     * The bid requires approval from all specified store owners and managers.
     * Notifications are sent to the required approvers.
     * 
     * Called by: Subscriber (user)- from executePurchase method in the PurchaseService class.
     */
    public static void submitBid(String storeId, String productId, String userId, double amount,
                                 String shippingAddress, String contactInfo, Set<String> approvers) {
        if (amount <= 0) throw new RuntimeException("Bid must be a positive value.");
        BidKey key = buildKey(storeId, productId);
        Bid bid = new Bid(userId, amount, shippingAddress, contactInfo, approvers);
        bids.computeIfAbsent(key, k -> new ArrayList<>()).add(bid);
        NotificationForPurchase.notifyApprovers(approvers, "New bid submitted for product " + productId);
    }



    /**
     * Approves a pending bid for a product.
     * 
     * A store owner or authorized manager can approve a submitted bid.
     * Once all required approvers have approved, the bid is marked as fully approved
     * and the subscriber is notified.
     * 
     * Called by: Store owner or authorized manager
     */
    public static void approveBid(String storeId, String productId, String userId, String approverId) {
        BidKey key = buildKey(storeId, productId);
        List<Bid> productBids = bids.getOrDefault(key, new ArrayList<>());
        for (Bid bid : productBids) {
            if (bid.userId.equals(userId) && !bid.isRejected()) {
                bid.approve(approverId);
                if (bid.approved) {
                    //notifyUserWithDelayIfNeeded(bid.userId, "Your bid has been approved! Completing purchase automatically.");
                    System.out.println("Bid approved for user: " + bid.userId);
                    // Call purchase directly with simple arguments
                    Purchase purchase = new BidPurchase().purchase(
                            bid.userId,
                            storeId,
                            productId,
                            bid.price,
                            bid.shippingAddress,
                            bid.contactInfo
                    );
                    System.out.println("Purchase completed for user: " + purchase.getUserId());
                }
            return;
            }
        }
        throw new RuntimeException("Bid not found for user.");
    }



    /**
     * Rejects a pending bid for a product.
     * 
     * A store owner or authorized manager can reject a bid, immediately terminating it.
     * Once rejected, the bid cannot be approved or purchased.
     * The subscriber is notified that the bid was rejected.
     * 
     * Called by: Store owner or authorized manager
     */
    public static void rejectBid(String storeId, String productId, String userId, String approverId) {
        BidKey key = buildKey(storeId, productId);
        List<Bid> productBids = bids.getOrDefault(key, new ArrayList<>());
        for (Bid bid : productBids) {
            if (bid.userId.equals(userId)) {
                bid.reject(approverId);
                //notifyUserWithDelayIfNeeded(bid.userId, "Your bid has been rejected.");
                System.out.println("Bid rejected for user: " + bid.userId);
                return;
            }
        }
        throw new RuntimeException("Bid not found for user.");
    }



    /**
     * Proposes a counter-offer for an existing bid.
     * 
     * A store owner or authorized manager can suggest a new price instead of the original bid.
     * The subscriber will need to respond to the counter-offer (future versions).
     * 
     * Called by: Store owner or authorized manager
     */
    public static void proposeCounterBid(String storeId, String productId, String userId, double newAmount) {
        if (newAmount <= 0) throw new RuntimeException("Counter offer must be a positive value.");
        BidKey key = buildKey(storeId, productId);
        List<Bid> productBids = bids.getOrDefault(key, new ArrayList<>());
        for (Bid bid : productBids) {
            if (bid.userId.equals(userId) && !bid.isRejected()) {
                bid.proposeCounterOffer(newAmount);
                //notifyUserWithDelayIfNeeded(bid.userId, "Counter offer proposed: " + newAmount);
                System.out.println("Counter offer proposed for user: " + bid.userId);
                return;
            }
        }
        throw new RuntimeException("Bid not found for user.");
    }


    
    /**
     * Accepts a counter-offer made by the store.
     * 
     * This resets the bid to approved status at the new counter-offer price.
     * 
     * Called by: Subscriber (user)
     */
    public static void acceptCounterOffer(String storeId, String productId, String userId) {
        BidKey key = buildKey(storeId, productId);
        List<Bid> productBids = bids.getOrDefault(key, new ArrayList<>());
        for (Bid bid : productBids) {
            if (bid.userId.equals(userId) && bid.counterOffered && !bid.rejected) {
                // Update price to the counter-offer
                bid.price = bid.counterOfferAmount;
                // Mark as approved automatically (no need for all approvals again)
                bid.approved = true;
                bid.counterOffered = false; // No longer a counter-offer
                //notifyUserWithDelayIfNeeded(bid.userId, "You accepted the counter-offer. Completing purchase.");
                System.out.println("Counter offer accepted for user: " + bid.userId);
                // Complete purchase immediately
                Purchase purchase = new BidPurchase().purchase(
                        bid.userId,
                        storeId,
                        productId,
                        bid.price,
                        bid.shippingAddress,
                        bid.contactInfo
                );
                System.out.println("Purchase completed for user: " + purchase.getUserId());
                return;
            }
        }
        throw new RuntimeException("No counter-offer found for user.");
    }


    /**
     * Declines a counter-offer made by the store.
     * 
     * The bid is canceled after the subscriber declines.
     * 
     * Called by: Subscriber (user)
     */
    public static void declineCounterOffer(String storeId, String productId, String userId) {
        BidKey key = buildKey(storeId, productId);
        List<Bid> productBids = bids.getOrDefault(key, new ArrayList<>());
        for (Bid bid : productBids) {
            if (bid.userId.equals(userId) && bid.counterOffered && !bid.rejected) {
                bid.rejected = true; // Mark bid as rejected
                //notifyUserWithDelayIfNeeded(bid.userId, "You declined the counter-offer. The bid has been canceled.");
                System.out.println("Bid canceled for user: " + bid.userId);
                return;
            }
        }
        throw new RuntimeException("No counter-offer found for user.");
    }



    /**
     * Retrieves the current status of a specific bid.
     * 
     * The status can be:
     * - "Pending Approval" if not yet fully approved
     * - "Approved" if all required parties approved
     * - "Rejected" if any owner/manager rejected
     * - "Counter Offered" if a counter-offer was made
     * 
     * Called by: Subscriber (user) or Store owners/managers
     */
    public static String getBidStatus(String storeId, String productId, String userId) {
        BidKey key = buildKey(storeId, productId);
        List<Bid> productBids = bids.getOrDefault(key, new ArrayList<>());
        for (Bid bid : productBids) {
            if (bid.userId.equals(userId)) {
                if (bid.isRejected()) return "Rejected";
                if (bid.counterOffered) return "Counter Offered: " + bid.counterOfferAmount;
                if (bid.isApproved()) return "Approved";
                return "Pending Approval";
            }
        }
        return "No Bid Found";
    }


    /**
     * Sends a notification to the user immediately if they are online.
     * 
     * If the user is offline, the notification is saved and delivered
     * when the user logs in again.
     * 
     * Called internally by: System (BidPurchase logic)
     */
    // private static void notifyUserWithDelayIfNeeded(String userId, String message) {
    //     if (NotificationForPurchase.isUserOnline(userId)) {
    //         NotificationForPurchase.notifyUser(userId, message);
    //     } else {
    //         pendingNotifications.computeIfAbsent(userId, k -> new ArrayList<>()).add(message);
    //     }
    // }


    /**
     * Retrieves and clears all pending notifications for a specific user.
     * 
     * This is called automatically when the user logs in to ensure they receive
     * any missed notifications.
     * 
     * Called by: System (during user login)
     */
    // public static List<String> pullPendingNotifications(String userId) {
    //     List<String> notifications = pendingNotifications.remove(userId);
    //     if (notifications == null) {
    //         return new ArrayList<>();
    //     }
    //     return notifications;
    // }


    /**
     * Completes a purchase based on an approved bid.
     * 
     * The subscriber must have a fully approved bid (without rejection or counter-offer).
     * If the bid is eligible, a Purchase object is created representing the transaction.
     * 
     * Called by: Subscriber (user)
     */
    public Purchase purchase(String userId, String storeId, String productId, double price, String shippingAddress, String contactInfo) {
        PurchasedProduct product = new PurchasedProduct(
                productId,
                storeId,
                1, // Always 1 in a bid purchase
                price,
                0.0 // No discount in a bid purchase
        );
        return new Purchase(userId, List.of(product), product.getTotalPrice(), shippingAddress, contactInfo);
    } 

}
