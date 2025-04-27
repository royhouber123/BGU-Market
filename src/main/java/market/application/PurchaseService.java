package market.application;

import market.domain.purchase.*;
import market.domain.user.*;
import market.infrastracture.*;
import market.application.StoreService;
import market.domain.store.Store;
import market.domain.store.Policies.*;

import java.util.*;

public class PurchaseService {

    private final StoreRepository storeRepository;
    private final IPurchaseRepository purchaseRepository;
    private final UserRepository userRepository;


    public PurchaseService(StoreRepository storeRepository, IPurchaseRepository purchaseRepository, UserRepository userRepository) {
        this.storeRepository = storeRepository;
        this.purchaseRepository = purchaseRepository;
        this.userRepository = userRepository;
    }

    // Regular Purchase:
    public Purchase executePurchase(String userId, ShoppingCart cart, String shippingAddress, String contactInfo){
        List<PurchasedProduct> purchasedItems = new ArrayList<>();
        for (StoreBag bag : cart.getAllStoreBags()) {
            String storeId = bag.getStoreId();
            Store store = storeRepository.getStoreByID(bag.getStoreId());
            boolean isValidBag=store.checkPurchasePolicy(bag, userId);
            if (!isValidBag) {
                throw new RuntimeException("Invalid purchase bag for store: " + bag.getStoreId());
            }
            int totalDiscountPrice=store.getTotalDiscountPrice(bag);
            for (Map.Entry<String, Integer> product : bag.getProducts().entrySet()) {
                String productId = product.getKey();
                double unitPrice = store.getProductUnitPrice(productId);
                Integer quantity = product.getValue();
                PurchasedProduct purchasedProduct = new PurchasedProduct(productId, storeId, quantity, unitPrice);
                purchasedItems.add(purchasedProduct);
            }
        }
        Boolean updateStockForPurchasedItems = storeRepository.updateStockForPurchasedItems(purchasedItems);
        if (!updateStockForPurchasedItems) {
            throw new RuntimeException("Failed to update stock for purchased items.");
        }
        RegularPurchase regularPurchase = new RegularPurchase();
        return regularPurchase.purchase(userId, purchasedItems, shippingAddress, contactInfo);
    }
    
    // Auction Purchase:
    public void submitOffer(String storeId, String productId, String userId, double offerPrice, String shippingAddress, String contactInfo) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new RuntimeException("User not found: " + userId);
        }
        if (user instanceof Subscriber) {
            AuctionPurchase.submitOffer(storeId, productId, userId, offerPrice, shippingAddress, contactInfo);
        }

    }

    public void openAuction(String userId, String storeId, String productId, String productName, String productDescription, String listingId, int startingPrice, long endTimeMillis) {
        storeRepository.getStoreByID(storeId).addNewListing(userId, productId, productName, productDescription, 1, startingPrice);
        AuctionPurchase.openAuction(storeRepository, storeId, productId, startingPrice, endTimeMillis);
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////
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
