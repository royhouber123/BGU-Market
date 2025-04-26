package market.application;

import market.domain.purchase.*;
import market.domain.user.ShoppingCart;
import market.domain.user.StoreBag;
import market.model.*;
import market.services.StoreService;
import market.services.Store;
import market.domain.policy.PurchasePolicy;
import market.domain.policy.DiscountPolicy;

import java.util.ArrayList;
import java.util.List;

public class PurchaseService {

    private final StoreService storeService;
    private final IPurchaseRepository purchaseRepository;

    public PurchaseService(StoreService storeService, IPurchaseRepository purchaseRepository) {
        this.storeService = storeService;
        this.purchaseRepository = purchaseRepository;
    }

    // for regular purchase (without offer price)
    public Purchase executePurchase(String userId, ShoppingCart cart, String shippingAddress, String contactInfo, PurchaseType type) {
        return executePurchase(userId, cart, shippingAddress, contactInfo, type, 0);        
    }

    public Purchase executePurchase(String userId, ShoppingCart cart, String shippingAddress, String contactInfo, PurchaseType type, double offerPrice) {
        switch (type) {
            case REGULAR -> {
                List<PurchasedProduct> purchasedItems = new ArrayList<>();
                for (StoreBag bag : cart.getStoreBags()) {
                    String storeId = bag.getStoreId();
                    DiscountPolicy discountPolicy = storeService.getDiscountPolicy();
                    PurchasePolicy purchasePolicies = storeService.getPurchasePolicies();
                    for (Map.Entry<String, Integer> item : bag.getItems()) {
                        String itemId = item.getKey();
                        double unitPrice = StoreService.getProductPrice(storeId, itemId);
                        if(purchasePolicies.validate(userId, itemId)) {
                            throw new IllegalArgumentException("Purchase policy validation failed for item: " + itemId);
                        }
                        double discount = discountPolicy.getDiscount(userId, itemId);
                        Integer quantity = item.getValue();
                        PurchasedProduct product = new PurchasedProduct(itemId, storeId, quantity, unitPrice, discount);
                        purchasedItems.add(product);
                    }
                }
                RegularPurchase regularPurchase = new RegularPurchase();
                return regularPurchase.purchase(userId, purchasedItems, shippingAddress, contactInfo);
            }
            case AUCTION -> {
                AuctionPurchase auctionPurchase = new AuctionPurchase();

                StoreBag bag = cart.getStoreBags().get(0);
                String storeId = bag.getStoreId();
                String productId = bag.getItems().get(0).getProductId();
        
                AuctionPurchase.submitOffer(storeId, productId, userId, offerPrice, shippingAddress, contactInfo);
            }
            case BID -> {
                BidPurchase bidPurchase = new BidPurchase();
                StoreBag bag = cart.getStoreBags().get(0);
                String storeId = bag.getStoreId();
                String productId = bag.getItems().get(0).getProductId();
                Set<String> approvers = storeService.getStoreOwners(storeId);
                bidPurchase.submitBid(storeId, productId, userId, offerPrice, shippingAddress, contactInfo, approvers);
            }
            case RAFFLE -> {
                RafflePurchase rafflePurchase = new RafflePurchase();
                return handleRafflePurchase(rafflePurchase, userId, cart, shippingAddress, contactInfo);
            }
            default -> return null;
        }
    }

    public List<Purchase> getPurchasesByUser(String userId) {
        return purchaseRepository.getPurchasesByUser(userId);
    }
    
    public List<Purchase> getPurchasesByStore(String storeId) {
        return purchaseRepository.getPurchasesByStore(storeId);
    }



    private Purchase handleRafflePurchase(RafflePurchase rafflePurchase, String userId, ShoppingCart cart, String shippingAddress, String contactInfo) {
        throw new UnsupportedOperationException("Raffle purchase flow not implemented yet.");
    }
}
