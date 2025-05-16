package market.application;

import market.application.External.IPaymentService;
import market.application.External.IShipmentService;
import market.domain.purchase.*;
import market.domain.user.*;
import market.domain.store.*;
import market.domain.store.Policies.*;
import utils.ApiResponse;
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
    public ApiResponse<Purchase> executePurchase(String userId, ShoppingCart cart, String shippingAddress, String contactInfo) {
        try {
            Map<String, Map<String, Integer>> listForUpdateStock = new HashMap<>();
            double totalDiscountPrice = 0.0;
            List<PurchasedProduct> purchasedItems = new ArrayList<>();
            logger.info("Executing purchase for user: " + userId);
    
            for (StoreBag bag : cart.getAllStoreBags()) {
                String storeId = String.valueOf(bag.getStoreId());
                Store store = storeRepository.getStoreByID(storeId);
                listForUpdateStock.put(storeId, bag.getProducts());
    
                if (!store.isPurchaseAllowed(bag.getProducts())) {
                    logger.debug("Invalid purchase bag for store: " + storeId);
                    return ApiResponse.fail("Invalid purchase bag for store: " + storeId);
                }
    
                totalDiscountPrice += store.calculateStoreBagWithDiscount(bag.getProducts());
    
                for (Map.Entry<String, Integer> product : bag.getProducts().entrySet()) {
                    String productId = product.getKey();
                    double unitPrice;
    
                    try {
                        unitPrice = listingRepository.ProductPrice(productId);
                    } catch (Exception e) {
                        logger.debug("Product not found: " + productId);
                        return ApiResponse.fail("Product not found: " + productId);
                    }
    
                    Integer quantity = product.getValue();
                    PurchasedProduct purchasedProduct = new PurchasedProduct(productId, storeId, quantity, unitPrice);
                    purchasedItems.add(purchasedProduct);
                }
            }
    
            boolean updated = listingRepository.updateStockForPurchasedItems(listForUpdateStock);
            if (!updated) {
                logger.error("Failed to update stock for purchased items.");
                return ApiResponse.fail("Failed to update stock for purchased items.");
            }
    
            RegularPurchase regularPurchase = new RegularPurchase();
            logger.info("Purchase executed successfully for user: " + userId + ", total: " + totalDiscountPrice);
    
            Purchase finalPurchase = regularPurchase.purchase(userId, purchasedItems, shippingAddress, contactInfo, totalDiscountPrice, paymentService, shipmentService);
            if (finalPurchase != null) {
                User user = userRepository.findById(userId);
                user.clearCart();
                return ApiResponse.ok(finalPurchase);
            } else {
                return ApiResponse.fail("Final purchase object is null.");
            }
    
        } catch (Exception e) {
            logger.error("Failed to execute regular purchase for user: " + userId + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Failed to execute regular purchase: " + e.getMessage());
        }
    }
    

    // Auction Purchase
    public ApiResponse<Void> submitOffer(String storeId, String productId, String userId, double offerPrice, String shippingAddress, String contactInfo) {
        try {
            User user = userRepository.findById(userId);
            if (!(user instanceof Subscriber)) {
                logger.debug("User is not a subscriber: " + userId);
                return ApiResponse.fail("User is not a subscriber: " + userId);
            }
    
            AuctionPurchase.submitOffer(storeId, productId, userId, offerPrice, shippingAddress, contactInfo);
            logger.info("Auction offer submitted: user " + userId + ", product " + productId + ", store " + storeId + ", price " + offerPrice);
    
            return ApiResponse.ok(null); // הצלחה בלי תוכן
    
        } catch (RuntimeException e) {
            logger.error("Failed to submit auction offer for user: " + userId + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Failed to submit auction offer: " + e.getMessage());
        }
    }

    public ApiResponse<Void> openAuction(String userId, String storeId, String productId, String productName, String productCategory, String productDescription, int startingPrice, long endTimeMillis) {
        try {
            Store store = storeRepository.getStoreByID(storeId);
            store.addNewListing(userId, productId, productName, productCategory, productDescription, 1, startingPrice);
    
            AuctionPurchase.openAuction(storeRepository, storeId, productId, startingPrice, endTimeMillis, shipmentService, paymentService);
    
            logger.info("Auction opened: store " + storeId + ", product " + productId + ", by user " + userId);
            return ApiResponse.ok(null); // הצלחה בלי מידע נוסף
    
        } catch (Exception e) {
            logger.error("Failed to open auction for store: " + storeId + ", product: " + productId + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Failed to open auction: " + e.getMessage());
        }
    }

    public ApiResponse<Map<String, Object>> getAuctionStatus(String userId, String storeId, String productId) {
        try {
            User user = userRepository.findById(userId);
            if (!(user instanceof Subscriber)) {
                logger.debug("User is not a subscriber: " + userId);
                return ApiResponse.fail("User is not a subscriber: " + userId);
            }
    
            logger.info("Getting auction status: user " + userId + ", store " + storeId + ", product " + productId);
            Map<String, Object> status = AuctionPurchase.getAuctionStatus(storeId, productId);
            return ApiResponse.ok(status);
    
        } catch (RuntimeException e) {
            logger.error("Failed to get auction status for user: " + userId + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Failed to get auction status: " + e.getMessage());
        }
    }
    
    // Bid Purchase:
    public ApiResponse<Void> submitBid(String storeId, String productId, String userId, double offerPrice, String shippingAddress, String contactInfo) {
        try {
            Set<String> approvers = storeRepository.getStoreByID(storeId).getApproversForBid();
    
            BidPurchase.submitBid(
                storeRepository,
                storeId,
                productId,
                userId,
                offerPrice,
                shippingAddress,
                contactInfo,
                approvers,
                shipmentService,
                paymentService
            );
    
            logger.info("Bid submitted: user " + userId + ", store " + storeId + ", product " + productId + ", price " + offerPrice);
            return ApiResponse.ok(null);
    
        } catch (RuntimeException e) {
            logger.error("Failed to submit bid for user: " + userId + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Failed to submit bid: " + e.getMessage());
        }
    }

    public ApiResponse<Void> approveBid(String storeId, String productId, String userId, String approverId) {
        try {
            validateApproverForBid(storeId, productId, userId, approverId);
            BidPurchase.approveBid(storeId, productId, userId, approverId);
    
            logger.info("Bid approved: approver " + approverId + ", user " + userId + ", store " + storeId + ", product " + productId);
            return ApiResponse.ok(null);
    
        } catch (RuntimeException e) {
            logger.debug("Failed to approve bid for user: " + userId + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Failed to approve bid: " + e.getMessage());
        }
    }

    public ApiResponse<Void> rejectBid(String storeId, String productId, String userId, String approverId) {
        try {
            validateApproverForBid(storeId, productId, userId, approverId);
            BidPurchase.rejectBid(storeId, productId, userId, approverId);
    
            logger.info("Bid rejected: approver " + approverId + ", user " + userId + ", store " + storeId + ", product " + productId);
            return ApiResponse.ok(null);
    
        } catch (RuntimeException e) {
            logger.debug("Failed to reject bid for user: " + userId + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Failed to reject bid: " + e.getMessage());
        }
    }

    public ApiResponse<Void> proposeCounterBid(String storeId, String productId, String userId, String approverId, double newAmount) {
        try {
            validateApproverForBid(storeId, productId, userId, approverId);
            BidPurchase.proposeCounterBid(storeId, productId, userId, newAmount);
    
            logger.info("Counter bid proposed: approver " + approverId + ", user " + userId + ", store " + storeId + ", product " + productId + ", new amount " + newAmount);
            return ApiResponse.ok(null);
    
        } catch (RuntimeException e) {
            logger.debug("Failed to propose counter bid for user: " + userId + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Failed to propose counter bid: " + e.getMessage());
        }
    }
    

    public ApiResponse<Void> acceptCounterOffer(String storeId, String productId, String userId) {
        try {
            BidPurchase.acceptCounterOffer(storeId, productId, userId);
            logger.info("Counter offer accepted: user " + userId + ", store " + storeId + ", product " + productId);
            return ApiResponse.ok(null);
    
        } catch (RuntimeException e) {
            logger.debug("Failed to accept counter offer for user: " + userId + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Failed to accept counter offer: " + e.getMessage());
        }
    }

    public ApiResponse<Void> declineCounterOffer(String storeId, String productId, String userId) {
        try {
            BidPurchase.declineCounterOffer(storeId, productId, userId);
            logger.info("Counter offer declined: user " + userId + ", store " + storeId + ", product " + productId);
            return ApiResponse.ok(null);
    
        } catch (RuntimeException e) {
            logger.debug("Failed to decline counter offer for user: " + userId + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Failed to decline counter offer: " + e.getMessage());
        }
    }

    public ApiResponse<String> getBidStatus(String storeId, String productId, String userId) {
        try {
            String status = BidPurchase.getBidStatus(storeId, productId, userId);
            return ApiResponse.ok(status);
        } catch (RuntimeException e) {
            logger.debug("Failed to get bid status for user: " + userId + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Failed to get bid status: " + e.getMessage());
        }
    }

    public ApiResponse<List<Purchase>> getPurchasesByUser(String userId) {
        try {
            List<Purchase> purchases = purchaseRepository.getPurchasesByUser(userId);
            return ApiResponse.ok(purchases);
        } catch (RuntimeException e) {
            logger.error("Failed to get purchases by user: " + userId + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Failed to get purchases by user: " + e.getMessage());
        }
    }

    public ApiResponse<List<Purchase>> getPurchasesByStore(String storeId) {
        try {
            List<Purchase> purchases = purchaseRepository.getPurchasesByStore(storeId);
            return ApiResponse.ok(purchases);
        } catch (RuntimeException e) {
            logger.error("Failed to get purchases by store: " + storeId + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Failed to get purchases by store: " + e.getMessage());
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
