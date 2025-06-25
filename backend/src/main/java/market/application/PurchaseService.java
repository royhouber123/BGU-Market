package market.application;

import market.application.External.IPaymentService;
import market.application.External.IShipmentService;
import market.domain.purchase.*;
import market.domain.user.*;
import market.domain.store.*;
import io.jsonwebtoken.Claims;
import utils.Logger;
import org.springframework.transaction.annotation.Transactional;
import java.beans.Transient;
import java.util.*;

import javax.management.RuntimeErrorException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PurchaseService {
    private final NotificationService notificationService;
    private final IStoreRepository storeRepository;
    private final IPurchaseRepository purchaseRepository;
    private final IListingRepository listingRepository;
    private final IUserRepository userRepository;
    private final IAuctionRepository auctionRepository;
    private final IBidRepository bidRepository;
    private final IPaymentService paymentService;
    private final IShipmentService shipmentService;
    private final Logger logger = Logger.getInstance();
    private ISuspensionRepository suspensionRepository; 

    @Autowired
    public PurchaseService(IStoreRepository storeRepository, IPurchaseRepository purchaseRepository, IListingRepository listingRepository, IUserRepository userRepository, IPaymentService paymentService, IShipmentService shipmentService, ISuspensionRepository suspentionRepository, NotificationService notificationService, IAuctionRepository auctionRep, IBidRepository bidRep) {
        this.storeRepository = storeRepository;
        this.purchaseRepository = purchaseRepository;
        this.listingRepository=listingRepository;
        this.userRepository = userRepository;
        this.paymentService = paymentService;
        this.shipmentService = shipmentService;
        this.suspensionRepository = suspentionRepository;
        this.notificationService = notificationService;
        this.auctionRepository = auctionRep;
        this.bidRepository = bidRep;
    }

    // Regular Purchase
    @Transactional
    public Purchase executePurchase(String userId, ShoppingCart cart, String shippingAddress, String paymentDetails)  {
            suspensionRepository.checkNotSuspended(userId);// check if user is suspended
            System.out.println("=====PurchaseService====\nExecuting purchase for user: " + userId);
            Map<String, Map<String, Integer>> listForUpdateStock = new HashMap<>();
            double totalDiscountPrice = 0.0;
            List<PurchasedProduct> purchasedItems = new ArrayList<>();
            logger.info("Executing purchase for user: " + userId);
    
            for (StoreBag bag : cart.getAllStoreBags()) {
                String storeId = String.valueOf(bag.getStoreId());
                logger.info("Looking up store with id: " + storeId);
                Store store = storeRepository.getStoreByID(storeId);
                if (store == null) {
                    logger.error("Store not found for id: " + storeId);
                    throw new RuntimeException("Store not found for id: " + storeId);
                }
                /*
                 * Ensure transient fields are initialized (they are not persisted via JPA).
                 * This prevents NullPointerExceptions when the store entity is reloaded from the database.
                 */
                if (store.getStoreProductsManager() == null) {
                    store.setStoreProductsManager(new StoreProductManager(storeId, listingRepository));
                }
                if (store.getPolicyHandler() == null) {
                    store.setPolicyHandler(new market.domain.store.Policies.PolicyHandler());
                }

                listForUpdateStock.put(storeId, bag.getProducts());
    
                if (!store.isPurchaseAllowed(bag.getProducts())) {
                    logger.debug("Invalid purchase bag for store: " + storeId);
                    throw new IllegalArgumentException("Invalid purchase bag for store: " + storeId);
                }
    
                totalDiscountPrice += store.calculateStoreBagWithDiscount(bag.getProducts());
                for (Map.Entry<String, Integer> product : bag.getProducts().entrySet()) {
                    String productId = product.getKey();
                    double unitPrice;
                    String productName;
    
                    try {
                        logger.info("Retrieving product price for productId: " + productId + " in store: " + storeId);
                        unitPrice = listingRepository.ProductPrice(productId);
                        Listing listing = listingRepository.getListingById(productId);
                        productName = (listing != null) ? listing.getProductName() : "";
                    } catch (Exception e) {
                        logger.debug("Product not found: " + productId);
                        throw new RuntimeException("Product not found: " + productId);
                    }
    
                    Integer quantity = product.getValue();
                    PurchasedProduct purchasedProduct = new PurchasedProduct(productId, productName, storeId, quantity, unitPrice);
                    purchasedItems.add(purchasedProduct);
                }
            }
    
            boolean updated = listingRepository.updateOrRestoreStock(listForUpdateStock, false);
            if (!updated) {
                logger.error("Failed to update stock for purchased items.");
                throw new RuntimeException("Failed to update stock for purchased items.");
            }
    
            RegularPurchase regularPurchase = new RegularPurchase();
            logger.info("Purchase executed successfully for user: " + userId + ", total: " + totalDiscountPrice);
            try {
                Purchase finalPurchase = regularPurchase.purchase(userId, purchasedItems, shippingAddress, paymentDetails, totalDiscountPrice, paymentService, shipmentService);
                User user = userRepository.findById(userId);
                // Persist cart clearing so that subsequent reads reflect the empty cart
                user.clearCart();
                userRepository.save(user);
                purchaseRepository.save(finalPurchase);
                return finalPurchase;
            } catch (IllegalArgumentException e) {
                logger.error("Invalid purchase details for user: " + userId + ". Reason: " + e.getMessage());
                throw e;
            } catch (RuntimeException e) {
                logger.error("Payment or shipment failed for user: " + userId + ". Reason: " + e.getMessage());
                listingRepository.updateOrRestoreStock(listForUpdateStock, true);
                throw e;
            }
    }
    
    // Overloaded method for simplified API access - automatically gets user's cart
    public String executePurchase(int userId, String paymentDetails, String shippingAddress) throws Exception{
            String userIdStr = String.valueOf(userId);
            suspensionRepository.checkNotSuspended(userIdStr);// check if user is suspended
            User user = userRepository.findById(userIdStr);
            if (user == null) {
                logger.debug("User not found: " + userId);
                throw new IllegalArgumentException("User not found: " + userId);
            }
            
            ShoppingCart cart = user.getShoppingCart();
            if (cart == null || cart.getAllStoreBags().isEmpty()) {
                throw new IllegalArgumentException("Shopping cart is empty");
            }
            
            Purchase result = executePurchase(userIdStr, cart, shippingAddress, paymentDetails);
            return "Purchase completed successfully. Total: $" + result.getTotalPrice() + " at " + result.getTimestamp();
    }

    // Auction Purchase
    @Transactional
    public void submitOffer(String storeId, String productId, String userId, double offerPrice, String shippingAddress, String contactInfo) {
            suspensionRepository.checkNotSuspended(userId);// check if user is suspended
            User user = userRepository.findById(userId);
            if (!(user instanceof Subscriber)) {
                logger.debug("User is not a subscriber: " + userId);
                throw new IllegalArgumentException("User is not a subscriber: " + userId);
            }
    
            AuctionPurchase.submitOffer(storeId, productId, userId, offerPrice, shippingAddress, contactInfo);
            logger.info("Auction offer submitted: user " + userId + ", product " + productId + ", store " + storeId + ", price " + offerPrice);

    }

    @Transactional
    public void openAuction(String userId, String storeId, String productId, String productName, String productCategory, String productDescription, int startingPrice, long endTimeMillis) {
        try {
            suspensionRepository.checkNotSuspended(userId);// check if user is suspended
            
            Store store = storeRepository.getStoreByID(storeId);
            if (store == null) {
                throw new IllegalArgumentException("Store not found with ID: " + storeId);
            }
            
            // Get the actual product ID created by the backend
            String actualProductId = store.addNewListing(userId, productId, productName, productCategory, productDescription, 1, startingPrice, "AUCTION");
    
            // Use the actual product ID for the auction
            AuctionPurchase.openAuction(listingRepository, storeId, actualProductId, startingPrice, endTimeMillis, shipmentService, paymentService, purchaseRepository, notificationService, auctionRepository);
    
            logger.info("Auction opened: store " + storeId + ", product " + actualProductId + ", by user " + userId);
    
        } catch (Exception e) {
            logger.error("Failed to open auction for store: " + storeId + ", product: " + productId + ". Reason: " + e.getMessage());
            throw new RuntimeException("Failed to open auction: " + e.getMessage());
        }
    }

    public Map<String, Object> getAuctionStatus(String userId, String storeId, String productId) {
        try {
            User user = userRepository.findById(userId);
            if (!(user instanceof Subscriber)) {
                logger.debug("User is not a subscriber: " + userId);
                throw new IllegalArgumentException("User is not a subscriber: " + userId);
            }
    
            logger.info("Getting auction status: user " + userId + ", store " + storeId + ", product " + productId);
            Map<String, Object> status = AuctionPurchase.getAuctionStatus(storeId, productId);
            if (status == null || status.isEmpty()) {
                logger.debug("No auction status found for user: " + userId + ", store: " + storeId + ", product: " + productId);
                return Collections.emptyMap();
            }
            else {
                logger.info("Auction status retrieved successfully for user: " + userId + ", store: " + storeId + ", product: " + productId);
                return status;
            }
        } catch (RuntimeException e) {
            logger.error("Failed to get auction status for user: " + userId + ". Reason: " + e.getMessage());
            throw new RuntimeException("Failed to get auction status: " + e.getMessage());
        }
    }
    
    // Bid Purchase:
    @Transactional
    public void submitBid(String storeId, String productId, String userId, double offerPrice, String shippingAddress, String contactInfo) {
            suspensionRepository.checkNotSuspended(userId);// check if user is suspended

            // Validate input parameters
            if (storeId == null || storeId.trim().isEmpty()) {
                throw new IllegalArgumentException("Store ID cannot be null or empty");
            }
            if (productId == null || productId.trim().isEmpty()) {
                throw new IllegalArgumentException("Product ID cannot be null or empty");
            }
            if (userId == null || userId.trim().isEmpty()) {
                throw new IllegalArgumentException("User ID cannot be null or empty");
            }
            if (offerPrice <= 0) {
                throw new IllegalArgumentException("Bid amount must be a positive value");
            }

            // Check if store exists first
            Store store = storeRepository.getStoreByID(storeId);
            if (store == null) {
                logger.debug("Store not found: " + storeId);
                throw new IllegalArgumentException("Store not found: " + storeId);
            }
            
            Set<String> approvers = store.getApproversForBid();
            if (approvers == null || approvers.isEmpty()) {
                logger.debug("No approvers found for store: " + storeId);
                throw new IllegalArgumentException("No approvers found for store: " + storeId);
            }
    
            BidPurchase.submitBid(
                listingRepository,
                storeId,
                productId,
                userId,
                offerPrice,
                shippingAddress,
                contactInfo,
                approvers,
                shipmentService,
                paymentService,
                purchaseRepository,
                notificationService,
                bidRepository
            );
            notifyAllApproversForBid(storeId, "New bid submitted for approval: " + userId + " has submitted a bid of $" + offerPrice + " in store " + storeId);
            logger.info("Bid submitted: user " + userId + ", store " + storeId + ", product " + productId + ", price " + offerPrice);

    }

    @Transactional
    public void approveBid(String storeId, String productId, String userId, String approverId) {
        validateApproverForBid(storeId, productId, userId, approverId);
        BidPurchase.approveBid(storeId, productId, userId, approverId);
        
        logger.info("Bid approved: approver " + approverId + ", user " + userId + ", store " + storeId + ", product " + productId);
    }

    @Transactional
    public void rejectBid(String storeId, String productId, String userId, String approverId){
        validateApproverForBid(storeId, productId, userId, approverId);
        BidPurchase.rejectBid(storeId, productId, userId, approverId);

        logger.info("Bid rejected: approver " + approverId + ", user " + userId + ", store " + storeId + ", product " + productId);
    }

    @Transactional
    public void proposeCounterBid(String storeId, String productId, String userId, String approverId, double newAmount) {
        validateApproverForBid(storeId, productId, userId, approverId);
        BidPurchase.proposeCounterBid(storeId, productId, userId, newAmount);

        logger.info("Counter bid proposed: approver " + approverId + ", user " + userId + ", store " + storeId + ", product " + productId + ", new amount " + newAmount);
    }
    
    @Transactional
    public void acceptCounterOffer(String storeId, String productId, String userId) {
        BidPurchase.acceptCounterOffer(storeId, productId, userId);
        logger.info("Counter offer accepted: user " + userId + ", store " + storeId + ", product " + productId);
    }

    @Transactional
    public void declineCounterOffer(String storeId, String productId, String userId) {
        BidPurchase.declineCounterOffer(storeId, productId, userId);
        logger.info("Counter offer declined: user " + userId + ", store " + storeId + ", product " + productId);
    }

    public String getBidStatus(String storeId, String productId, String userId) {
        try {
            suspensionRepository.checkNotSuspended(userId);// check if user is suspended
            BidKey key = new BidKey(storeId, productId);
            List<Bid> bids = BidPurchase.getBids().get(key);
            
            if (bids == null || bids.isEmpty()) {
                return "No Bid Found";
            }
            
            for (Bid bid : bids) {
                if (bid.getUserId().equals(userId)) {
                    return getBidStatusString(bid);
                }
            }
        
            return "No Bid Found";
        } catch (RuntimeException e) {
            logger.error("Failed to get bid status: " + e.getMessage());
            throw new RuntimeException("Failed to get bid status: " + e.getMessage());
        }
    }

    public Map<String, Object> getBidStatusWithCurrApprovers(String storeId, String productId, String userId) {
        try {
            suspensionRepository.checkNotSuspended(userId);// check if user is suspended
            BidKey key = new BidKey(storeId, productId);
            List<Bid> bids = BidPurchase.getBids().get(key);

            Map<String, Object> result = new HashMap<>();
            if (bids == null || bids.isEmpty()) {
                result.put("status", "No Bid Found");
                result.put("approvedBy", List.of());
                return result;
            }

            for (Bid bid : bids) {
                if (bid.getUserId().equals(userId)) {
                    result.put("status", getBidStatusString(bid));
                    result.put("approvedBy", bid.getApprovedBy());
                    return result;
                }
            }

            result.put("status", "No Bid Found");
            result.put("approvedBy", List.of());
            return result;
            
        } catch (RuntimeException e) {
            logger.error("Failed to get bid status: " + e.getMessage());
            throw new RuntimeException("Failed to get bid status: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> getProductBids(String storeId, String productId, String requestingUser) {
        try {
            
            // Verify that the requesting user has permission to view bids for this store
            Store store = storeRepository.getStoreByID(storeId);
            if (store == null) {
                throw new IllegalArgumentException("Store not found: " + storeId);
            }
            
            // Add debugging information
            logger.info("Bid permission check - User: " + requestingUser + ", Store: " + storeId);
            logger.info("User is owner: " + store.isOwner(requestingUser));
            logger.info("User is manager: " + store.isManager(requestingUser));
            
            // Check if user has permission to view bids (owners, managers with bid approval permission)
            if (!store.checkBidPermission(requestingUser)) {
                logger.error("User " + requestingUser + " does not have permission to view bids for store " + storeId);
                throw new IllegalArgumentException("You don't have permission to view bids for this store. Only store owners and managers with bid approval permission can view bids.");    
            }
            
            // Get bids for the product
            BidKey key = new BidKey(storeId, productId);
            List<Bid> bids = BidPurchase.getBids().get(key);
            
            if (bids == null || bids.isEmpty()) {
                return new ArrayList<>();
            }
            
            // Convert bids to map format for JSON response
            List<Map<String, Object>> bidData = new ArrayList<>();
            for (Bid bid : bids) {
                Map<String, Object> bidInfo = new HashMap<>();
                bidInfo.put("id", bid.getId());
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
            
            return bidData;
        } catch (RuntimeException e) {
            logger.error("Failed to get product bids: " + e.getMessage());
            throw new RuntimeException("Failed to get product bids: " + e.getMessage());
        }
    }
    
    private String getBidStatusString(Bid bid) {
        if (bid.isRejected()) return "Rejected";
        if (bid.isCounterOffered()) return "Counter Offered";
        if (bid.isApproved()) return "Approved";
        return "Pending Approval";
    }


    public List<Purchase> getPurchasesByUser(String userId) {
        logger.info("Retrieving purchases for user: " + userId);
        List<Purchase> purchases = purchaseRepository.getPurchasesByUser(userId);
        logger.info("Found " + purchases.size() + " purchases for user: " + userId);
        return purchases;
    }

    public List<Purchase> getPurchasesByStore(String storeId) {
        List<Purchase> purchases = purchaseRepository.getPurchasesByStore(storeId);
        return purchases;
    }
    

    private void validateApproverForBid(String storeId, String productId, String userId, String approverId) throws RuntimeErrorException {
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
    public String executePurchaseByUsername(String token, String paymentDetails, String shippingAddress) {
            // Extract username from token (simplified approach without AuthService dependency)
            Claims claims = io.jsonwebtoken.Jwts.parserBuilder()
                .setSigningKey(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                    io.jsonwebtoken.io.Decoders.BASE64URL.decode("JMvzGmTQtUL4OWwh-JAiawZXbxKKrFssCXZtkC_ZUKc")))
                .build()
                .parseClaimsJws(token)
                .getBody();
            String username = claims.getSubject();
            
            if (username == null) {
                throw new IllegalArgumentException("Invalid token: no username found");
            }
            
            // Get user and execute purchase
            User user = userRepository.findById(username);
            if (user == null) {
                throw new IllegalArgumentException("User not found: " + username);
            }
            
            suspensionRepository.checkNotSuspended(username);// check if user is suspended
            
            ShoppingCart cart = user.getShoppingCart();
            if (cart == null || cart.getAllStoreBags().isEmpty()) {
                throw new IllegalArgumentException("Shopping cart is empty for user: " + username);
            }
            
            Purchase result = executePurchase(username, cart, shippingAddress, paymentDetails);
            return "Purchase completed successfully. Total: $" + result.getTotalPrice() + " at " + result.getTimestamp();
    }

    // Add new method to get current user's bids for a product
    public List<Map<String, Object>> getMyProductBids(String storeId, String productId, String requestingUser) {
        try {
            // Get bids for the product
            BidKey key = new BidKey(storeId, productId);
            List<Bid> bids = BidPurchase.getBids().get(key);

            if (bids == null || bids.isEmpty()) {
                return new ArrayList<>();
            }

            // Filter bids to only include the requesting user's bids
            List<Map<String, Object>> bidData = new ArrayList<>();
            for (Bid bid : bids) {
                if (bid.getUserId().equals(requestingUser)) {
                    Map<String, Object> bidInfo = new HashMap<>();
                    bidInfo.put("id", bid.getId());
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

            return bidData;
        } catch (RuntimeException e) {
            logger.error("Failed to get user's product bids: " + e.getMessage());
            throw new RuntimeException("Failed to get user's product bids: " + e.getMessage());
        }
    }

    private void notifyAllOwners(String storeID, String message) {
        Store store = storeRepository.getStoreByID(storeID);
        if (store != null) {
            for (String ownerId : store.getAllOwners()) {
                notificationService.sendNotification(ownerId, message);
            }
        }
    }

    private void notifyAllApproversForBid(String storeID, String message) {
        Store store = storeRepository.getStoreByID(storeID);
        if (store != null) {
            for (String approver : store.getApproversForBid()) {
                notificationService.sendNotification(approver, message);
            }
        }
    }
}
