package market.application;

import market.domain.purchase.*;
import market.domain.user.ShoppingCart;
import market.domain.user.StoreBag;
import market.application.StoreService;
import market.domain.store.Store;
import market.domain.policy.PurchasePolicy;
import market.domain.policy.DiscountPolicy;

import java.util.*;

public class PurchaseService {

    private final StoreService storeService;
    private final IPurchaseRepository purchaseRepository;

    public PurchaseService(StoreService storeService, IPurchaseRepository purchaseRepository) {
        this.storeService = storeService;
        this.purchaseRepository = purchaseRepository;
    }

    // Regular Purchase:
    public Purchase executePurchase(String userId, ShoppingCart cart, String shippingAddress, String contactInfo) {
        List<PurchasedProduct> purchasedItems = new ArrayList<>();
        for (StoreBag bag : cart.getAllStoreBags()) {
            String storeId = bag.getStoreId();
            DiscountPolicy discountPolicy = storeService.getDiscountPolicy();
            PurchasePolicy purchasePolicies = storeService.getPurchasePolicies();
            for (Map.Entry<String, Integer> item : bag.getProducts().entrySet()) {
                String itemId = item.getKey();
                double unitPrice = StoreService.getProductPrice(storeId, itemId);
                String listingId = storeService.getProductListing(storeId, itemId); 
                if(purchasePolicies.validate(userId, itemId)) {
                    throw new IllegalArgumentException("Purchase policy validation failed for item: " + Integer.parseInt(itemId));
                }
                double discount = discountPolicy.getDiscount(userId, itemId);
                Integer quantity = item.getValue();
                PurchasedProduct product = new PurchasedProduct(itemId, storeId, listingId, quantity, unitPrice, discount);
                purchasedItems.add(product);
            }
        }
        for (PurchasedProduct product : purchasedItems) {
            boolean updatedStock = storeService.checkAndUpdateStock(product.getStoreId(), product.getProductId(), product.getQuantity());
            if (!updatedStock) {
                throw new RuntimeException("Stock update failed for product: " + product.getProductId());
            }
        }
        RegularPurchase regularPurchase = new RegularPurchase();
        return regularPurchase.purchase(userId, purchasedItems, shippingAddress, contactInfo);
    }
    
    // Auction Purchase:
    public void submitOffer(String storeId, String productId, String userId, double offerPrice, String shippingAddress, String contactInfo) {
        AuctionPurchase.submitOffer(storeId, productId, userId, offerPrice, shippingAddress, contactInfo);
    }

    public void openAuction(String storeId, String productId, String listingId, double startingPrice, long endTimeMillis) {
        AuctionPurchase.openAuction(storeService, storeId, productId, listingId, startingPrice, endTimeMillis);
    }
    
    public Map<String, Object> getAuctionStatus(String storeId, String productId) {
        return AuctionPurchase.getAuctionStatus(storeId, productId);
    }
    
    // Bid Purchase:
    public void submitBid(String storeId, String productId, String userId, double offerPrice, String shippingAddress, String contactInfo) {
        Set<String> approvers = storeService.getStoreOwners(storeId);
        BidPurchase.submitBid(storeService, storeId, productId, userId, offerPrice, shippingAddress, contactInfo, approvers);
    }

    public void approveBid(String storeId, String productId, String listingId, String userId, String approverId) {
        BidPurchase.approveBid(storeId, productId, listingId, userId, approverId);
    }
    
    public void rejectBid(String storeId, String productId, String userId, String approverId) {
        BidPurchase.rejectBid(storeId, productId, userId, approverId);
    }
    
    public void proposeCounterBid(String storeId, String productId, String userId, double newAmount) {
        BidPurchase.proposeCounterBid(storeId, productId, userId, newAmount);
    }
    
    public void acceptCounterOffer(String storeId, String productId, String listingId, String userId) {
        BidPurchase.acceptCounterOffer(storeId, productId, listingId, userId);
    }
    
    public void declineCounterOffer(String storeId, String productId, String userId) {
        BidPurchase.declineCounterOffer(storeId, productId, userId);
    }
    
    public String getBidStatus(String storeId, String productId, String userId) {
        return BidPurchase.getBidStatus(storeId, productId, userId);
    }


    // Get all purchases by user or store
    public List<Purchase> getPurchasesByUser(String userId) {
        return purchaseRepository.getPurchasesByUser(userId);
    }
    
    public List<Purchase> getPurchasesByStore(String storeId) {
        return purchaseRepository.getPurchasesByStore(storeId);
    }

}
