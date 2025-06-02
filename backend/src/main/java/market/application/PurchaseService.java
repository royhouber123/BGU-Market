package market.application;

import market.application.External.IPaymentService;
import market.application.External.IShipmentService;
import market.domain.purchase.*;
import market.domain.user.*;
import market.domain.store.*;
import market.domain.store.Policies.*;
import market.middleware.TokenUtils;
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
                    throw new IllegalArgumentException("Invalid purchase bag for store: " + storeId);
                }
    
                totalDiscountPrice += store.calculateStoreBagWithDiscount(bag.getProducts());
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
    
            boolean updated = listingRepository.updateOrRestoreStock(listForUpdateStock, false);
            if (!updated) {
                logger.error("Failed to update stock for purchased items.");
                throw new RuntimeException("Failed to update stock for purchased items.");
            }
    
            RegularPurchase regularPurchase = new RegularPurchase();
            logger.info("Purchase executed successfully for user: " + userId + ", total: " + totalDiscountPrice);
            try {
                Purchase finalPurchase = regularPurchase.purchase(userId, purchasedItems, shippingAddress, contactInfo, totalDiscountPrice, paymentService, shipmentService);
                User user = userRepository.findById(userId);
                 user.clearCart();
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
    public String executePurchase(int userId, String paymentDetails, String shippingAddress) {
            String userIdStr = String.valueOf(userId);
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
    public void submitOffer(String storeId, String productId, String userId, double offerPrice, String shippingAddress, String contactInfo) {
            User user = userRepository.findById(userId);
            if (!(user instanceof Subscriber)) {
                logger.debug("User is not a subscriber: " + userId);
                throw new IllegalArgumentException("User is not a subscriber: " + userId);
            }
    
            AuctionPurchase.submitOffer(storeId, productId, userId, offerPrice, shippingAddress, contactInfo);
            logger.info("Auction offer submitted: user " + userId + ", product " + productId + ", store " + storeId + ", price " + offerPrice);
    }

    public void openAuction(String userId, String storeId, String productId, String productName, String productCategory, String productDescription, int startingPrice, long endTimeMillis) {
        try {
            Store store = storeRepository.getStoreByID(storeId);
            store.addNewListing(userId, productId, productName, productCategory, productDescription, 1, startingPrice);
    
            AuctionPurchase.openAuction(storeRepository, storeId, productId, startingPrice, endTimeMillis, shipmentService, paymentService);
    
            logger.info("Auction opened: store " + storeId + ", product " + productId + ", by user " + userId);
    
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
    public void submitBid(String storeId, String productId, String userId, double offerPrice, String shippingAddress, String contactInfo) {
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
    }

    public void approveBid(String storeId, String productId, String userId, String approverId) {
        validateApproverForBid(storeId, productId, userId, approverId);
        BidPurchase.approveBid(storeId, productId, userId, approverId);

        logger.info("Bid approved: approver " + approverId + ", user " + userId + ", store " + storeId + ", product " + productId);
    }

    public void rejectBid(String storeId, String productId, String userId, String approverId) {
        validateApproverForBid(storeId, productId, userId, approverId);
        BidPurchase.rejectBid(storeId, productId, userId, approverId);

        logger.info("Bid rejected: approver " + approverId + ", user " + userId + ", store " + storeId + ", product " + productId);
    }

    public void proposeCounterBid(String storeId, String productId, String userId, String approverId, double newAmount) {
        validateApproverForBid(storeId, productId, userId, approverId);
        BidPurchase.proposeCounterBid(storeId, productId, userId, newAmount);

        logger.info("Counter bid proposed: approver " + approverId + ", user " + userId + ", store " + storeId + ", product " + productId + ", new amount " + newAmount);
    }
    

    public void acceptCounterOffer(String storeId, String productId, String userId) {
        BidPurchase.acceptCounterOffer(storeId, productId, userId);
        logger.info("Counter offer accepted: user " + userId + ", store " + storeId + ", product " + productId);
    }

    public void declineCounterOffer(String storeId, String productId, String userId) {
        BidPurchase.declineCounterOffer(storeId, productId, userId);
        logger.info("Counter offer declined: user " + userId + ", store " + storeId + ", product " + productId);
    }

    public String getBidStatus(String storeId, String productId, String userId) {
            String status = BidPurchase.getBidStatus(storeId, productId, userId);
            return status;
    }

    public List<Purchase> getPurchasesByUser(String userId) {
        List<Purchase> purchases = purchaseRepository.getPurchasesByUser(userId);
        return purchases;
    }

    public List<Purchase> getPurchasesByStore(String storeId) {
        List<Purchase> purchases = purchaseRepository.getPurchasesByStore(storeId);
        return purchases;
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
            
            ShoppingCart cart = user.getShoppingCart();
            if (cart == null || cart.getAllStoreBags().isEmpty()) {
                throw new IllegalArgumentException("Shopping cart is empty for user: " + username);
            }
            
            Purchase result = executePurchase(username, cart, shippingAddress, paymentDetails);
            return "Purchase completed successfully. Total: $" + result.getTotalPrice() + " at " + result.getTimestamp();
    }
}
