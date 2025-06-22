package market.domain.purchase;
import market.application.External.*;
import market.domain.store.*;
import market.application.NotificationService;

import java.util.*;

import javax.management.Notification;


public class BidPurchase {

    //to update and check stock
    private static IListingRepository listingRepository;
    private static IShipmentService shipmentService;
    private static IPaymentService paymentService;
    private static IPurchaseRepository purchaseRepository;
    private static IBidRepository bidRepository;
    private static NotificationService notificationService;
    
    
    private static final Map<BidKey, List<Bid>> bids = new HashMap<>();

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
    public static void submitBid(IListingRepository rep, String storeId, String productId, String userId, double amount,
                                 String shippingAddress, String contactInfo, Set<String> approvers, IShipmentService shipment, IPaymentService payment, IPurchaseRepository purchaseRep, NotificationService notifService, IBidRepository bidRep) { 

        if (amount <= 0) throw new RuntimeException("Bid must be a positive value.");
        listingRepository=rep;
        shipmentService=shipment;
        paymentService=payment;
        purchaseRepository=purchaseRep;
        bidRepository = bidRep;
        notificationService = notifService;
        BidKey key = buildKey(storeId, productId);
        Bid bid = new Bid(userId, amount, shippingAddress, contactInfo, approvers);
        bids.computeIfAbsent(key, k -> new ArrayList<>()).add(bid);
        
        BidEntity entity = bidRepository.findByStoreIdAndProductId(storeId, productId)
            .orElseGet(() -> new BidEntity(storeId, productId));

        entity.getBids().add(bid);
        bidRepository.save(entity);
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
            if (bid.getUserId().equals(userId) && !bid.isRejected()) {
                if (!bid.getRequiredApprovers().contains(approverId)) {
                    throw new RuntimeException("Approver is not authorized.");
                }
                bid.approve(approverId);

                BidEntity entity = bidRepository.findByStoreIdAndProductId(storeId, productId)
                        .orElseThrow(() -> new RuntimeException("BidEntity not found in database."));

                for (Bid b : entity.getBids()) {
                    if (b.getUserId().equals(userId)) {
                        b.approve(approverId);
                        break;
                    }
                }

                bidRepository.save(entity);

                if (bid.isApproved()) {
                    notificationService.sendNotification(bid.getUserId(), "Your bid has been approved! Completing purchase automatically.");
                    // Call purchase directly with simple arguments
                    Map<String, Map<String, Integer>> listForUpdateStock = new HashMap<>();
                    Map<String, Integer> productMap = new HashMap<>();
                    productMap.put(productId, 1); // Assuming quantity is 1 for auction purchase
                    listForUpdateStock.put(storeId, productMap);
                    boolean updatedStock = listingRepository.updateOrRestoreStock(listForUpdateStock, false);
                    if (!updatedStock) {
                        throw new RuntimeException("Failed to update stock for bid purchase.");
                    }
                    Purchase purchase = new BidPurchase().purchase(
                            bid.getUserId(),
                            storeId,
                            productId,
                            bid.getPrice(),
                            bid.getShippingAddress(),
                            bid.getContactInfo()
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
            if (bid.getUserId().equals(userId)) {
                bid.reject(approverId);
                notificationService.sendNotification(userId, "Your bid has been rejected.");
                BidEntity entity = bidRepository.findByStoreIdAndProductId(storeId, productId)
                        .orElseThrow(() -> new RuntimeException("BidEntity not found in database."));

                for (Bid b : entity.getBids()) {
                    if (b.getUserId().equals(userId)) {
                        b.reject(approverId);
                        break;
                    }
                }

                bidRepository.save(entity);
                
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
            if (bid.getUserId().equals(userId) && !bid.isRejected()) {
                bid.proposeCounterOffer(newAmount);
                notificationService.sendNotification(userId, "Counter offer proposed: " + newAmount);
                
                BidEntity entity = bidRepository.findByStoreIdAndProductId(storeId, productId)
                        .orElseThrow(() -> new RuntimeException("BidEntity not found in database."));
                for (Bid b : entity.getBids()) {
                    if (b.getUserId().equals(userId)) {
                        b.proposeCounterOffer(newAmount); 
                        break;
                    }
                }
                bidRepository.save(entity); 

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
            if (bid.getUserId().equals(userId) && bid.isCounterOffered() && !bid.isRejected()) {
                // Update price to the counter-offer
                double newPrice = bid.getCounterOfferAmount(); 
                bid.setPrice(newPrice);
                // Mark as approved automatically (no need for all approvals again)
                bid.setApproved(true);
                bid.setCounterOffered(false); // No longer a counter-offer
                
                BidEntity entity = bidRepository.findByStoreIdAndProductId(storeId, productId)
                        .orElseThrow(() -> new RuntimeException("BidEntity not found in database."));
                for (Bid b : entity.getBids()) {
                    if (b.getUserId().equals(userId)) {
                        b.setPrice(newPrice);
                        b.setApproved(true);
                        b.setCounterOffered(false);
                        break;
                    }
                }
                bidRepository.save(entity);
                
                notificationService.sendNotification(bid.getUserId(), "You accepted the counter-offer at price: " + bid.getPrice() + ". Completing purchase automatically.");
                // Complete purchase immediately
                Map<String, Map<String, Integer>> listForUpdateStock = new HashMap<>();
                Map<String, Integer> productMap = new HashMap<>();
                productMap.put(productId, 1); // Assuming quantity is 1 for auction purchase
                listForUpdateStock.put(storeId, productMap);
                boolean updatedStock = listingRepository.updateOrRestoreStock(listForUpdateStock, false);
                if (!updatedStock) {
                    throw new RuntimeException("Failed to update stock for bid purchase.");
                }
                Purchase purchase = new BidPurchase().purchase(
                        bid.getUserId(),
                        storeId,
                        productId,
                        bid.getPrice(),
                        bid.getShippingAddress(),
                        bid.getContactInfo()
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
            if (bid.getUserId().equals(userId) && bid.isCounterOffered() && !bid.isRejected()) {
                bid.setRejected(true);; // Mark bid as rejected
                
                BidEntity entity = bidRepository.findByStoreIdAndProductId(storeId, productId)
                        .orElseThrow(() -> new RuntimeException("BidEntity not found in database."));

                for (Bid b : entity.getBids()) {
                    if (b.getUserId().equals(userId)) {
                        b.setRejected(true);
                        break;
                    }
                }

                bidRepository.save(entity);
                
                notificationService.sendNotification(bid.getUserId(), "You declined the counter-offer. The bid has been canceled.");
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
            if (bid.getUserId().equals(userId)) {
                if (bid.isRejected()) return "Rejected";
                if (bid.isCounterOffered()) return "Counter Offered: " + bid.getCounterOfferAmount();
                if (bid.isApproved()) return "Approved";
                return "Pending Approval";
            }
        }
        return "No Bid Found";
    }


    /**
     * Completes a purchase based on an approved bid.
     * 
     * The subscriber must have a fully approved bid (without rejection or counter-offer).
     * If the bid is eligible, a Purchase object is created representing the transaction.
     * 
     * Called by: Subscriber (user)
     */
    public Purchase purchase(String userId, String storeId, String productId, double price, String shippingAddress, String paymentDetails) {
        String productName = "";
        Listing listing = listingRepository.getListingById(productId);
        if (listing != null) {
            productName = listing.getProductName();
        }
        PurchasedProduct product = new PurchasedProduct(
                productId,
                productName,
                storeId,
                1, // Always 1 in a bid purchase
                price
        );
        Purchase newP = null;
        try {
            paymentService.processPayment(paymentDetails);
            shipmentService.ship(shippingAddress, userId, 1); // Assuming weight is 1 for simplicity
            newP=new Purchase(userId, List.of(product), price, shippingAddress, paymentDetails);
            purchaseRepository.save(newP);
        } catch (Exception e) {
            listingRepository.updateOrRestoreStock(Map.of(storeId, Map.of(productId, 1)), true); // Restock if purchase fails
            notificationService.sendNotification(userId, "Purchase failed: " + e.getMessage());
            throw new RuntimeException("Failed to complete purchase: " + e.getMessage(), e);
        }

        //update or restock in catch
        return newP;
    } 

    public static Map<BidKey, List<Bid>> getBids() {
        return bids;
    }


    public static void setListingRepository(IListingRepository listingRepository2) {
        listingRepository=listingRepository2;
    }


    public static void setPaymentService(IPaymentService paymentService2) {
        paymentService = paymentService2;
    }


    public static void setShippingService(IShipmentService shipmentService2) {
        shipmentService=shipmentService2;
    }

    public static void setPurchaseRepository(IPurchaseRepository purchaseRepository2) {
        purchaseRepository=purchaseRepository2;
    }

    public static void setNotificationService(NotificationService notificationService2) {
        notificationService = notificationService2;
    }

    public static void setBidRepository(IBidRepository repo) {
        bidRepository = repo;
    }
}