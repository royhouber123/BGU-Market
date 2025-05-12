package market.application;

import market.application.External.IPaymentService;
import market.application.External.IShipmentService;
import market.domain.purchase.*;
import market.domain.user.*;
import market.infrastructure.*;
import market.application.StoreService;
import market.domain.store.*;
import market.domain.store.Policies.*;
import utils.Logger;

import java.util.*;

public class PurchaseService {

    private final IStoreRepository storeRepository;
    private final IPurchaseRepository purchaseRepository;
    private final IListingRepository listingRepository;
    private final IUserRepository userRepository;
    private final IPaymentService paymentService;
    private final IShipmentService shipmentService;
    private final Logger logger = Logger.getInstance();

    public PurchaseService(IStoreRepository storeRepository, IPurchaseRepository purchaseRepository, IListingRepository listingRepository, IUserRepository userRepository, IPaymentService paymentService, IShipmentService shipmentService) {
        this.storeRepository = storeRepository;
        this.purchaseRepository = purchaseRepository;
        this.listingRepository=listingRepository;
        this.userRepository = userRepository;
        this.paymentService = paymentService;
        this.shipmentService = shipmentService;
    }

    // Regular Purchase
    public Purchase executePurchase(String userId, ShoppingCart cart, String shippingAddress, String contactInfo) {
        try {
            Map<String, Map<String, Integer>> listForUpdateStock=new HashMap<>();
            double totalDiscountPrice = 0.0;
            List<PurchasedProduct> purchasedItems = new ArrayList<>();
            logger.info("Executing purchase for user: " + userId);
            for (StoreBag bag : cart.getAllStoreBags()) {
                String storeId = String.valueOf(bag.getStoreId());
                Store store = storeRepository.getStoreByID(storeId);
                listForUpdateStock.put(storeId, bag.getProducts());
                boolean isValidBag = store.isPurchaseAllowed(bag.getProducts());
                if (!isValidBag) {
                    logger.debug("Invalid purchase bag for store: " + storeId);
                    throw new RuntimeException("Invalid purchase bag for store: " + storeId);
                }
                totalDiscountPrice=totalDiscountPrice+store.calculateStoreBagWithDiscount(bag.getProducts());
                for (Map.Entry<String, Integer> product : bag.getProducts().entrySet()) {
                    String productId = product.getKey();
                    double unitPrice;
                    try {
                        unitPrice = listingRepository.ProductPrice(productId);
                    } catch (Exception e) {
                        logger.debug("Product not found: " + productId);
                        throw new RuntimeException("Product not found: " + productId);
                    }
                    Integer quantity = product.getValue();
                    PurchasedProduct purchasedProduct = new PurchasedProduct(productId, storeId, quantity, unitPrice);
                    purchasedItems.add(purchasedProduct);
                }
            }
            boolean updated = listingRepository.updateStockForPurchasedItems(listForUpdateStock);
            if (!updated) {
                logger.error("Failed to update stock for purchased items.");
                throw new RuntimeException("Failed to update stock for purchased items.");
            }
            RegularPurchase regularPurchase = new RegularPurchase();
            logger.info("Purchase executed successfully for user: " + userId + ", total: " + totalDiscountPrice);
            Purchase finalPurchase=regularPurchase.purchase(userId, purchasedItems, shippingAddress, contactInfo, totalDiscountPrice, paymentService, shipmentService);
            if (finalPurchase!=null){
                User user = userRepository.findById(userId);
                user.clearCart();
                return finalPurchase;
            }
            return null;
        } catch (Exception e) {
            logger.error("Failed to execute regular purchase for user: " + userId + ". Reason: " + e.getMessage());
            throw new RuntimeException("Failed to execute regular purchase: " + e.getMessage(), e);
        }
    }

    // Auction Purchase
    public void submitOffer(String storeId, String productId, String userId, double offerPrice, String shippingAddress, String contactInfo) {
        try {
            User user = userRepository.findById(userId);
            if (!(user instanceof Subscriber)) {
                logger.debug("User is not a subscriber: " + userId);
                throw new RuntimeException("User is not a subscriber: " + userId);
            }
            AuctionPurchase.submitOffer(storeId, productId, userId, offerPrice, shippingAddress, contactInfo);
            logger.info("Auction offer submitted: user " + userId + ", product " + productId + ", store " + storeId + ", price " + offerPrice);
        } catch (RuntimeException e) {
            logger.error("Failed to submit auction offer for user: " + userId + ". Reason: " + e.getMessage());
            throw new RuntimeException("Failed to submit auction offer: " + e.getMessage(), e);
        }
    }

    public void openAuction(String userId, String storeId, String productId, String productName, String productCategory, String productDescription, int startingPrice, long endTimeMillis) {
        try {
            Store store = storeRepository.getStoreByID(storeId);
            store.addNewListing(userId, productId, productName, productCategory, productDescription, 1, startingPrice);
            AuctionPurchase.openAuction(storeRepository, storeId, productId, startingPrice, endTimeMillis, shipmentService, paymentService);
            logger.info("Auction opened: store " + storeId + ", product " + productId + ", by user " + userId);
        } catch (Exception e) {
            logger.error("Failed to open auction for store: " + storeId + ", product: " + productId + ". Reason: " + e.getMessage());
            throw new RuntimeException("Failed to open auction: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getAuctionStatus(String userId, String storeId, String productId) {
        try {
            User user = userRepository.findById(userId);
            if (!(user instanceof Subscriber)) {
                logger.debug("User is not a subscriber: " + userId);
                throw new RuntimeException("User is not a subscriber: " + userId);
            }
            logger.info("Getting auction status: user " + userId + ", store " + storeId + ", product " + productId);
            return AuctionPurchase.getAuctionStatus(storeId, productId);
        } catch (RuntimeException e) {
            logger.error("Failed to get auction status for user: " + userId + ". Reason: " + e.getMessage());
            throw new RuntimeException("Failed to get auction status: " + e.getMessage(), e);
        }
    }
    
    // Bid Purchase:
    public void submitBid(String storeId, String productId, String userId, double offerPrice, String shippingAddress, String contactInfo) {
        try {
            Set<String> approvers = storeRepository.getStoreByID(storeId).getApproversForBid();
            BidPurchase.submitBid(storeRepository, storeId, productId, userId, offerPrice, shippingAddress, contactInfo, approvers, shipmentService, paymentService);
            logger.info("Bid submitted: user " + userId + ", store " + storeId + ", product " + productId + ", price " + offerPrice);
        } catch (RuntimeException e) {
            logger.error("Failed to submit bid for user: " + userId + ". Reason: " + e.getMessage());
            throw new RuntimeException("Failed to submit bid: " + e.getMessage(), e);
        }
    }

    public void approveBid(String storeId, String productId, String userId, String approverId) {
        try {
            validateApproverForBid(storeId, productId, userId, approverId);
            BidPurchase.approveBid(storeId, productId, userId, approverId);
            logger.info("Bid approved: approver " + approverId + ", user " + userId + ", store " + storeId + ", product " + productId);
        } catch (RuntimeException e) {
            logger.debug("Failed to approve bid for user: " + userId + ". Reason: " + e.getMessage());
            throw new RuntimeException("Failed to approve bid: " + e.getMessage(), e);
        }
    }

    public void rejectBid(String storeId, String productId, String userId, String approverId) {
        try {
            validateApproverForBid(storeId, productId, userId, approverId);
            BidPurchase.rejectBid(storeId, productId, userId, approverId);
            logger.info("Bid rejected: approver " + approverId + ", user " + userId + ", store " + storeId + ", product " + productId);
        } catch (RuntimeException e) {
            logger.debug("Failed to reject bid for user: " + userId + ". Reason: " + e.getMessage());
            throw new RuntimeException("Failed to reject bid: " + e.getMessage(), e);
        }
    }

    public void proposeCounterBid(String storeId, String productId, String userId, String approverId, double newAmount) {
        try {
            validateApproverForBid(storeId, productId, userId, approverId);
            BidPurchase.proposeCounterBid(storeId, productId, userId, newAmount);
            logger.info("Counter bid proposed: approver " + approverId + ", user " + userId + ", store " + storeId + ", product " + productId + ", new amount " + newAmount);
        } catch (RuntimeException e) {
            logger.debug("Failed to propose counter bid for user: " + userId + ". Reason: " + e.getMessage());
            throw new RuntimeException("Failed to propose counter bid: " + e.getMessage(), e);
        }
    }

    public void acceptCounterOffer(String storeId, String productId, String userId) {
        try {
            BidPurchase.acceptCounterOffer(storeId, productId, userId);
            logger.info("Counter offer accepted: user " + userId + ", store " + storeId + ", product " + productId);
        } catch (RuntimeException e) {
            logger.debug("Failed to accept counter offer for user: " + userId + ". Reason: " + e.getMessage());
            throw new RuntimeException("Failed to accept counter offer: " + e.getMessage(), e);
        }
    }

    public void declineCounterOffer(String storeId, String productId, String userId) {
        try {
            BidPurchase.declineCounterOffer(storeId, productId, userId);
            logger.info("Counter offer declined: user " + userId + ", store " + storeId + ", product " + productId);
        } catch (RuntimeException e) {
            logger.debug("Failed to decline counter offer for user: " + userId + ". Reason: " + e.getMessage());
            throw new RuntimeException("Failed to decline counter offer: " + e.getMessage(), e);
        }
    }

    public String getBidStatus(String storeId, String productId, String userId) {
        try {
            return BidPurchase.getBidStatus(storeId, productId, userId);
        } catch (RuntimeException e) {
            logger.debug("Failed to get bid status for user: " + userId + ". Reason: " + e.getMessage());
            throw new RuntimeException("Failed to get bid status: " + e.getMessage(), e);
        }
    }

    public List<Purchase> getPurchasesByUser(String userId) {
        try {
            return purchaseRepository.getPurchasesByUser(userId);
        } catch (RuntimeException e) {
            logger.error("Failed to get purchases by user: " + userId + ". Reason: " + e.getMessage());
            throw new RuntimeException("Failed to get purchases by user: " + e.getMessage(), e);
        }
    }

    public List<Purchase> getPurchasesByStore(String storeId) {
        try {
            return purchaseRepository.getPurchasesByStore(storeId);
        } catch (RuntimeException e) {
            logger.error("Failed to get purchases by store: " + storeId + ". Reason: " + e.getMessage());
            throw new RuntimeException("Failed to get purchases by store: " + e.getMessage(), e);
        }
    }

    private void validateApproverForBid(String storeId, String productId, String userId, String approverId) {
        List<Bid> bids=BidPurchase.getBids().get(new BidKey(storeId, productId));
        if (bids == null || bids.isEmpty()) {
            throw new RuntimeException("No bids found for product " + productId + " in store " + storeId);
        }        
        Bid bidOfUser=null;
        for (Bid bid : bids) {
            if (bid.getUserId().equals(userId)) {
                bidOfUser=bid;
                break; // find the matching bid and break
            }
        }
        if (bidOfUser == null) {
            throw new RuntimeException("No bid found for user: " + userId);
        }
        if (!bidOfUser.getRequiredApprovers().contains(approverId)) {
            throw new RuntimeException("User " + approverId + " does not have permission to approve/reject/propose counter offer this bid.");
        }
    }
}
