package market.application;

import market.application.External.PaymentService;
import market.application.External.ShipmentService;
import market.domain.purchase.*;
import market.domain.user.*;
import market.infrastructure.*;
import market.application.StoreService;
import market.domain.store.Store;
import market.domain.store.Policies.*;

import java.util.*;

public class PurchaseService {

    private final StoreRepository storeRepository;
    private final IPurchaseRepository purchaseRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;
    private final ShipmentService shipmentService;


    public PurchaseService(StoreRepository storeRepository, IPurchaseRepository purchaseRepository, UserRepository userRepository, PaymentService paymentService, ShipmentService shipmentService) {
        this.storeRepository = storeRepository;
        this.purchaseRepository = purchaseRepository;
        this.userRepository = userRepository;
        this.paymentService = paymentService;
        this.shipmentService = shipmentService;
    }

    // Regular Purchase
    public Purchase executePurchase(String userId, ShoppingCart cart, String shippingAddress, String contactInfo) {
        try {
            double totalDiscountPrice = 0.0;
            List<PurchasedProduct> purchasedItems = new ArrayList<>();
            for (StoreBag bag : cart.getAllStoreBags()) {
                String storeId = String.valueOf(bag.getStoreId());
                Store store = storeRepository.getStoreByID(storeId);
                boolean isValidBag = store.isPurchaseAllowed(bag.getProducts());
                ////לוודא עם דיין כי אין את הפונקציה
                if (!isValidBag) {
                    throw new RuntimeException("Invalid purchase bag for store: " + storeId);
                }
                totalDiscountPrice=totalDiscountPrice+store.calculateStoreBagWithDiscount(bag.getProducts());
                for (Map.Entry<String, Integer> product : bag.getProducts().entrySet()) {
                    String productId = product.getKey();
                    double unitPrice = store.ProductPrice(productId);
                    Integer quantity = product.getValue();
                    PurchasedProduct purchasedProduct = new PurchasedProduct(productId, storeId, quantity, unitPrice);
                    purchasedItems.add(purchasedProduct);
                }
            }
            boolean updated = storeRepository.updateStockForPurchasedItems(purchasedItems);
            ////לוודא עם דיין כי אין את הפונקציה
            if (!updated) {
                throw new RuntimeException("Failed to update stock for purchased items.");
            }
            RegularPurchase regularPurchase = new RegularPurchase();
            return regularPurchase.purchase(userId, purchasedItems, shippingAddress, contactInfo, totalDiscountPrice, paymentService, shipmentService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute regular purchase: " + e.getMessage(), e);
        }
    }

    // Auction Purchase
    public void submitOffer(String storeId, String productId, String userId, double offerPrice, String shippingAddress, String contactInfo) {
        try {
            User user = userRepository.findById(userId);
            if (!(user instanceof Subscriber)) {
                throw new RuntimeException("User is not a subscriber: " + userId);
            }
            AuctionPurchase.submitOffer(storeId, productId, userId, offerPrice, shippingAddress, contactInfo);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to submit auction offer: " + e.getMessage(), e);
        }
    }

    public void openAuction(String userId, String storeId, String productId, String productName, String productDescription, int startingPrice, long endTimeMillis) {
        try {
            Store store = storeRepository.getStoreByID(storeId);
            store.addNewListing(userId, productId, productName, productDescription, 1, startingPrice);
            AuctionPurchase.openAuction(storeRepository, storeId, productId, startingPrice, endTimeMillis, shipmentService, paymentService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to open auction: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getAuctionStatus(String userId, String storeId, String productId) {
        try {
            User user = userRepository.findById(userId);
            if (!(user instanceof Subscriber)) {
                throw new RuntimeException("User is not a subscriber: " + userId);
            }
            return AuctionPurchase.getAuctionStatus(storeId, productId);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to get auction status: " + e.getMessage(), e);
        }
    }
    
    // Bid Purchase:
    //should return "bid successful" or "bid failed"
    public void submitBid(String storeId, String productId, String userId, double offerPrice, String shippingAddress, String contactInfo) {
        try {
            Set<String> approvers = storeRepository.getStoreByID(storeId).getStoreOwners();
            ////לוודא עם דיין כי אין את הפונקציה
            BidPurchase.submitBid(storeRepository, storeId, productId, userId, offerPrice, shippingAddress, contactInfo, approvers, shipmentService, paymentService);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to submit bid: " + e.getMessage(), e);
        }
    }

    public void approveBid(String storeId, String productId, String userId, String approverId) {
        try {
            validateApprover(storeId, approverId);
            BidPurchase.approveBid(storeId, productId, userId, approverId);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to approve bid: " + e.getMessage(), e);
        }
    }

    public void rejectBid(String storeId, String productId, String userId, String approverId) {
        try {
            validateApprover(storeId, approverId);
            BidPurchase.rejectBid(storeId, productId, userId, approverId);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to reject bid: " + e.getMessage(), e);
        }
    }

    public void proposeCounterBid(String storeId, String productId, String userId, String approverId, double newAmount) {
        try {
            validateApprover(storeId, approverId);
            BidPurchase.proposeCounterBid(storeId, productId, userId, newAmount);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to propose counter bid: " + e.getMessage(), e);
        }
    }

    public void acceptCounterOffer(String storeId, String productId, String userId) {
        try {
            BidPurchase.acceptCounterOffer(storeId, productId, userId);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to accept counter offer: " + e.getMessage(), e);
        }
    }

    public void declineCounterOffer(String storeId, String productId, String userId) {
        try {
            BidPurchase.declineCounterOffer(storeId, productId, userId);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to decline counter offer: " + e.getMessage(), e);
        }
    }

    public String getBidStatus(String storeId, String productId, String userId) {
        try {
            return BidPurchase.getBidStatus(storeId, productId, userId);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to get bid status: " + e.getMessage(), e);
        }
    }

    public List<Purchase> getPurchasesByUser(String userId) {
        try {
            return purchaseRepository.getPurchasesByUser(userId);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to get purchases by user: " + e.getMessage(), e);
        }
    }

    public List<Purchase> getPurchasesByStore(String storeId) {
        try {
            return purchaseRepository.getPurchasesByStore(storeId);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to get purchases by store: " + e.getMessage(), e);
        }
    }

    private void validateApprover(String storeId, String approverId) {
        User user = userRepository.findById(approverId);
        if (!(user instanceof Subscriber subscriber)) {
            throw new RuntimeException("User is not a subscriber: " + approverId);
        }
        if (!subscriber.isOwner(storeId) && !subscriber.isManager(storeId)) {
            throw new RuntimeException("User is not an owner or manager in store: " + approverId);
        }
        /////////לוודא עם דיין כי אמרו מנהלי או בעלי חנות עם הרשאות מתאימות
    }
}
