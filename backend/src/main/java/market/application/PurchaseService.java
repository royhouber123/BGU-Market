package market.application;

import market.application.External.IPaymentService;
import market.application.External.IShipmentService;
import market.domain.purchase.*;
import market.domain.user.*;
import market.domain.store.*;
import io.jsonwebtoken.Claims;
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
    private ISuspensionRepository suspensionRepository; 

    public PurchaseService(IStoreRepository storeRepository, IPurchaseRepository purchaseRepository, IListingRepository listingRepository, IUserRepository userRepository, IPaymentService paymentService, IShipmentService shipmentService, ISuspensionRepository suspentionRepository) {
        this.storeRepository = storeRepository;
        this.purchaseRepository = purchaseRepository;
        this.listingRepository=listingRepository;
        this.userRepository = userRepository;
        this.paymentService = paymentService;
        this.shipmentService = shipmentService;
        this.suspensionRepository = suspentionRepository;
    }

    // Regular Purchase
    public ApiResponse<Purchase> executePurchase(String userId, ShoppingCart cart, String shippingAddress, String contactInfo) {
        try {
            suspensionRepository.checkNotSuspended(userId);// check if user is suspended
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
    
            boolean updated = listingRepository.updateOrRestoreStock(listForUpdateStock, false);
            if (!updated) {
                logger.error("Failed to update stock for purchased items.");
                return ApiResponse.fail("Failed to update stock for purchased items.");
            }
    
            RegularPurchase regularPurchase = new RegularPurchase();
            logger.info("Purchase executed successfully for user: " + userId + ", total: " + totalDiscountPrice);
            try {
                Purchase finalPurchase = regularPurchase.purchase(userId, purchasedItems, shippingAddress, contactInfo, totalDiscountPrice, paymentService, shipmentService);
                User user = userRepository.findById(userId);
                user.clearCart();
                purchaseRepository.save(finalPurchase);
                return ApiResponse.ok(finalPurchase);
            } catch (IllegalArgumentException e) {
                logger.error("Invalid purchase details for user: " + userId + ". Reason: " + e.getMessage());
                return ApiResponse.fail("Invalid purchase details: " + e.getMessage());
            } catch (RuntimeException e) {
                logger.error("Payment or shipment failed for user: " + userId + ". Reason: " + e.getMessage());
                listingRepository.updateOrRestoreStock(listForUpdateStock, true);
                return ApiResponse.fail("Payment or shipment failed: " + e.getMessage());
            }
    
        } catch (Exception e) {
            logger.error("Failed to execute regular purchase for user: " + userId + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Failed to execute regular purchase: " + e.getMessage());
        }
    }
    
    // Overloaded method for simplified API access - automatically gets user's cart
    public ApiResponse<String> executePurchase(int userId, String paymentDetails, String shippingAddress) {
        try {
            String userIdStr = String.valueOf(userId);
            suspensionRepository.checkNotSuspended(userIdStr);// check if user is suspended
            User user = userRepository.findById(userIdStr);
            if (user == null) {
                return ApiResponse.fail("User not found: " + userId);
            }
            
            ShoppingCart cart = user.getShoppingCart();
            if (cart == null || cart.getAllStoreBags().isEmpty()) {
                return ApiResponse.fail("Shopping cart is empty");
            }
            
            ApiResponse<Purchase> result = executePurchase(userIdStr, cart, shippingAddress, paymentDetails);
            if (result.isSuccess()) {
                Purchase purchase = result.getData();
                return ApiResponse.ok("Purchase completed successfully. Total: $" + purchase.getTotalPrice() + 
                                    " at " + purchase.getTimestamp());
            } else {
                return ApiResponse.fail(result.getError());
            }
        } catch (Exception e) {
            logger.error("Failed to execute simplified purchase for user: " + userId + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Failed to execute purchase: " + e.getMessage());
        }
    }

    // Auction Purchase
    public ApiResponse<Void> submitOffer(String storeId, String productId, String userId, double offerPrice, String shippingAddress, String contactInfo) {
        try {
            suspensionRepository.checkNotSuspended(userId);// check if user is suspended
            User user = userRepository.findById(userId);
            if (!(user instanceof Subscriber)) {
                logger.debug("User is not a subscriber: " + userId);
                return ApiResponse.fail("User is not a subscriber: " + userId);
            }
    
            AuctionPurchase.submitOffer(storeId, productId, userId, offerPrice, shippingAddress, contactInfo);
            logger.info("Auction offer submitted: user " + userId + ", product " + productId + ", store " + storeId + ", price " + offerPrice);
    
            return ApiResponse.ok(null); // הצלחה בלי תוכן
    
        } catch (Exception e) {
            logger.error("Failed to submit auction offer for user: " + userId + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Failed to submit auction offer: " + e.getMessage());
        }
    }

    public ApiResponse<Void> openAuction(String userId, String storeId, String productId, String productName, String productCategory, String productDescription, int startingPrice, long endTimeMillis) {
        try {
            suspensionRepository.checkNotSuspended(userId);// check if user is suspended
            Store store = storeRepository.getStoreByID(storeId);
            store.addNewListing(userId, productId, productName, productCategory, productDescription, 1, startingPrice, "AUCTION");
    
            AuctionPurchase.openAuction(storeRepository, storeId, productId, startingPrice, endTimeMillis, shipmentService, paymentService, purchaseRepository);
    
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
            suspensionRepository.checkNotSuspended(userId);// check if user is suspended
            // Validate input parameters
            if (storeId == null || storeId.trim().isEmpty()) {
                return ApiResponse.fail("Store ID cannot be null or empty");
            }
            if (productId == null || productId.trim().isEmpty()) {
                return ApiResponse.fail("Product ID cannot be null or empty");
            }
            if (userId == null || userId.trim().isEmpty()) {
                return ApiResponse.fail("User ID cannot be null or empty");
            }
            if (offerPrice <= 0) {
                return ApiResponse.fail("Bid amount must be a positive value");
            }
            
            // Check if store exists first
            Store store = storeRepository.getStoreByID(storeId);
            if (store == null) {
                return ApiResponse.fail("Store not found with ID: " + storeId + ". Please ensure the store exists before submitting a bid.");
            }
            
            Set<String> approvers = store.getApproversForBid();
            if (approvers == null || approvers.isEmpty()) {
                return ApiResponse.fail("No approvers found for store " + storeId + ". Cannot process bid without approvers.");
            }
    
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
                paymentService,
                purchaseRepository
            );
    
            logger.info("Bid submitted: user " + userId + ", store " + storeId + ", product " + productId + ", price " + offerPrice);
            return ApiResponse.ok(null);
    
        } catch (Exception e) {
            logger.error("Failed to submit bid for user: " + userId + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Failed to submit bid: " + e.getMessage());
        }
    }

    public ApiResponse<Void> approveBid(String storeId, String productId, String userId, String approverId) {
        try {
            suspensionRepository.checkNotSuspended(userId);// check if user is suspended
            validateApproverForBid(storeId, productId, userId, approverId);
            BidPurchase.approveBid(storeId, productId, userId, approverId);
    
            logger.info("Bid approved: approver " + approverId + ", user " + userId + ", store " + storeId + ", product " + productId);
            return ApiResponse.ok(null);
    
        } catch (Exception e) {
            logger.debug("Failed to approve bid for user: " + userId + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Failed to approve bid: " + e.getMessage());
        }
    }

    public ApiResponse<Void> rejectBid(String storeId, String productId, String userId, String approverId) {
        try {
            suspensionRepository.checkNotSuspended(userId);// check if user is suspended
            validateApproverForBid(storeId, productId, userId, approverId);
            BidPurchase.rejectBid(storeId, productId, userId, approverId);
    
            logger.info("Bid rejected: approver " + approverId + ", user " + userId + ", store " + storeId + ", product " + productId);
            return ApiResponse.ok(null);
    
        } catch (Exception e) {
            logger.debug("Failed to reject bid for user: " + userId + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Failed to reject bid: " + e.getMessage());
        }
    }

    public ApiResponse<Void> proposeCounterBid(String storeId, String productId, String userId, String approverId, double newAmount) {
        try {
            suspensionRepository.checkNotSuspended(userId);// check if user is suspended
            validateApproverForBid(storeId, productId, userId, approverId);
            BidPurchase.proposeCounterBid(storeId, productId, userId, newAmount);
    
            logger.info("Counter bid proposed: approver " + approverId + ", user " + userId + ", store " + storeId + ", product " + productId + ", new amount " + newAmount);
            return ApiResponse.ok(null);
    
        } catch (Exception e) {
            logger.debug("Failed to propose counter bid for user: " + userId + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Failed to propose counter bid: " + e.getMessage());
        }
    }
    

    public ApiResponse<Void> acceptCounterOffer(String storeId, String productId, String userId) {
        try {
            suspensionRepository.checkNotSuspended(userId);// check if user is suspended
            BidPurchase.acceptCounterOffer(storeId, productId, userId);
            logger.info("Counter offer accepted: user " + userId + ", store " + storeId + ", product " + productId);
            return ApiResponse.ok(null);
    
        } catch (Exception e) {
            logger.debug("Failed to accept counter offer for user: " + userId + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Failed to accept counter offer: " + e.getMessage());
        }
    }

    public ApiResponse<Void> declineCounterOffer(String storeId, String productId, String userId) {
        try {
            suspensionRepository.checkNotSuspended(userId);// check if user is suspended
            BidPurchase.declineCounterOffer(storeId, productId, userId);
            logger.info("Counter offer declined: user " + userId + ", store " + storeId + ", product " + productId);
            return ApiResponse.ok(null);
    
        } catch (Exception e) {
            logger.debug("Failed to decline counter offer for user: " + userId + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Failed to decline counter offer: " + e.getMessage());
        }
    }

    public ApiResponse<String> getBidStatus(String storeId, String productId, String userId) {
        try {
            suspensionRepository.checkNotSuspended(userId);// check if user is suspended
            BidKey key = new BidKey(storeId, productId);
            List<Bid> bids = BidPurchase.getBids().get(key);
            
            if (bids == null || bids.isEmpty()) {
                return ApiResponse.ok("No Bid Found");
            }
            
            for (Bid bid : bids) {
                if (bid.getUserId().equals(userId)) {
                    return ApiResponse.ok(getBidStatusString(bid));
                }
            }
            
            return ApiResponse.ok("No Bid Found");
        } catch (Exception e) {
            logger.error("Failed to get bid status: " + e.getMessage());
            return ApiResponse.fail("Failed to get bid status: " + e.getMessage());
        }
    }

    public ApiResponse<List<Map<String, Object>>> getProductBids(String storeId, String productId, String requestingUser) {
        try {
            
            // Verify that the requesting user has permission to view bids for this store
            Store store = storeRepository.getStoreByID(storeId);
            if (store == null) {
                return ApiResponse.fail("Store not found with ID: " + storeId);
            }
            
            // Add debugging information
            logger.info("Bid permission check - User: " + requestingUser + ", Store: " + storeId);
            logger.info("User is owner: " + store.isOwner(requestingUser));
            logger.info("User is manager: " + store.isManager(requestingUser));
            
            // Check if user has permission to view bids (owners, managers with bid approval permission)
            if (!store.checkBidPermission(requestingUser)) {
                logger.error("User " + requestingUser + " does not have permission to view bids for store " + storeId);
                return ApiResponse.fail("You don't have permission to view bids for this store. Only store owners and managers with bid approval permission can view bids.");
            }
            
            // Get bids for the product
            BidKey key = new BidKey(storeId, productId);
            List<Bid> bids = BidPurchase.getBids().get(key);
            
            if (bids == null || bids.isEmpty()) {
                return ApiResponse.ok(new ArrayList<>());
            }
            
            // Convert bids to map format for JSON response
            List<Map<String, Object>> bidData = new ArrayList<>();
            for (Bid bid : bids) {
                Map<String, Object> bidInfo = new HashMap<>();
                bidInfo.put("userId", bid.getUserId());
                bidInfo.put("bidAmount", bid.getPrice());
                bidInfo.put("shippingAddress", bid.getShippingAddress());
                bidInfo.put("contactInfo", bid.getContactInfo());
                bidInfo.put("status", getBidStatusString(bid));
                bidInfo.put("isApproved", bid.isApproved());
                bidInfo.put("isRejected", bid.isRejected());
                bidInfo.put("counterOffered", bid.isCounterOffered());
                if (bid.isCounterOffered()) {
                    bidInfo.put("counterOfferAmount", bid.getCounterOfferAmount());
                }
                bidInfo.put("requiredApprovers", bid.getRequiredApprovers());
                bidInfo.put("approvedBy", bid.getApprovedBy());
                bidData.add(bidInfo);
            }
            
            return ApiResponse.ok(bidData);
        } catch (RuntimeException e) {
            logger.error("Failed to get product bids: " + e.getMessage());
            return ApiResponse.fail("Failed to get product bids: " + e.getMessage());
        }
    }
    
    private String getBidStatusString(Bid bid) {
        if (bid.isRejected()) return "Rejected";
        if (bid.isCounterOffered()) return "Counter Offered";
        if (bid.isApproved()) return "Approved";
        return "Pending Approval";
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
    

    private void validateApproverForBid(String storeId, String productId, String userId, String approverId) throws Exception {
        suspensionRepository.checkNotSuspended(userId);// check if user is suspended
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

    // New method to handle purchase using JWT token
    public ApiResponse<String> executePurchaseByUsername(String token, String paymentDetails, String shippingAddress) {
        try {
            // Extract username from token (simplified approach without AuthService dependency)
            Claims claims = io.jsonwebtoken.Jwts.parserBuilder()
                .setSigningKey(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                    io.jsonwebtoken.io.Decoders.BASE64URL.decode("JMvzGmTQtUL4OWwh-JAiawZXbxKKrFssCXZtkC_ZUKc")))
                .build()
                .parseClaimsJws(token)
                .getBody();
            String username = claims.getSubject();
            
            if (username == null) {
                return ApiResponse.fail("Invalid token: no username found");
            }
            
            // Get user and execute purchase
            User user = userRepository.findById(username);
            if (user == null) {
                return ApiResponse.fail("User not found: " + username);
            }
            
            suspensionRepository.checkNotSuspended(username);// check if user is suspended
            
            ShoppingCart cart = user.getShoppingCart();
            if (cart == null || cart.getAllStoreBags().isEmpty()) {
                return ApiResponse.fail("Shopping cart is empty");
            }
            
            ApiResponse<Purchase> result = executePurchase(username, cart, shippingAddress, paymentDetails);
            if (result.isSuccess()) {
                Purchase purchase = result.getData();
                return ApiResponse.ok("Purchase completed successfully. Total: $" + purchase.getTotalPrice() + 
                                    " at " + purchase.getTimestamp());
            } else {
                return ApiResponse.fail(result.getError());
            }
        } catch (Exception e) {
            logger.error("Failed to execute purchase for token. Reason: " + e.getMessage());
            return ApiResponse.fail("Failed to execute purchase: " + e.getMessage());
        }
    }

    // Add new method to get current user's bids for a product
    public ApiResponse<List<Map<String, Object>>> getMyProductBids(String storeId, String productId, String requestingUser) {
        try {
            // Get bids for the product
            BidKey key = new BidKey(storeId, productId);
            List<Bid> bids = BidPurchase.getBids().get(key);
            
            if (bids == null || bids.isEmpty()) {
                return ApiResponse.ok(new ArrayList<>());
            }
            
            // Filter bids to only include the requesting user's bids
            List<Map<String, Object>> bidData = new ArrayList<>();
            for (Bid bid : bids) {
                if (bid.getUserId().equals(requestingUser)) {
                    Map<String, Object> bidInfo = new HashMap<>();
                    bidInfo.put("userId", bid.getUserId());
                    bidInfo.put("bidAmount", bid.getPrice());
                    bidInfo.put("shippingAddress", bid.getShippingAddress());
                    bidInfo.put("contactInfo", bid.getContactInfo());
                    bidInfo.put("status", getBidStatusString(bid));
                    bidInfo.put("isApproved", bid.isApproved());
                    bidInfo.put("isRejected", bid.isRejected());
                    bidInfo.put("counterOffered", bid.isCounterOffered());
                    if (bid.isCounterOffered()) {
                        bidInfo.put("counterOfferAmount", bid.getCounterOfferAmount());
                    }
                    bidData.add(bidInfo);
                }
            }
            
            return ApiResponse.ok(bidData);
        } catch (RuntimeException e) {
            logger.error("Failed to get user's product bids: " + e.getMessage());
            return ApiResponse.fail("Failed to get user's product bids: " + e.getMessage());
        }
    }
}
