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

    public Purchase executePurchase(String userId, ShoppingCart cart, String shippingAddress, String contactInfo, PurchaseType type) {

        // Step 1: Apply store policies & discounts
        List<StoreBag> validatedBags = new ArrayList<>();

        for (StoreBag bag : cart.getStoreBags()) {
            String storeId = bag.getStoreId();
            Store store = storeService.getStoreById(storeId);

            // Validate purchase policy
            PurchasePolicy purchasePolicy = store.getPurchasePolicy();
            purchasePolicy.validate(userId, bag);

            // Apply discounts
            DiscountPolicy discountPolicy = store.getDiscountPolicy();
            discountPolicy.apply(bag);

            validatedBags.add(bag);
        }

        ShoppingCart updatedCart = new ShoppingCart(validatedBags);

        // Step 2: Select strategy
        IPurchase strategy = selectStrategy(type);

        // Step 3: Execute purchase
        Purchase purchase = strategy.purchase(userId, updatedCart, shippingAddress, contactInfo);

        // Step 4: Save purchase
        purchaseRepository.save(purchase);

        return purchase;
    }

    public List<Purchase> getPurchasesByUser(String userId) {
        return purchaseRepository.getPurchasesByUser(userId);
    }
    
    public List<Purchase> getPurchasesByStore(String storeId) {
        return purchaseRepository.getPurchasesByStore(storeId);
    }


    

    //TODO
    //PUBLIC VOID SUBMIT OFFER (USER_ID , PRICE)


    

    private IPurchase selectStrategy(PurchaseType type) {
        return switch (type) {
            case REGULAR -> new RegularPurchase();
            case BID -> new BidPurchase();
            case AUCTION -> new AuctionPurchase();
            case RAFFLE -> new RafflePurchase();
        };
    }
}
