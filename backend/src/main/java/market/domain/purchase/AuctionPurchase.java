package market.domain.purchase;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import javax.management.Notification;

import market.domain.store.*;
import market.application.External.IPaymentService;
import market.application.External.IShipmentService;
import market.application.NotificationService;


import java.util.Timer;


public class AuctionPurchase {

    //to update and check stock
    private static IListingRepository listingRepository;
    private static IPurchaseRepository purchaseRepository;
    private static NotificationService notificationService;


    /// Map to store offers for each auction
    /// The key is a combination of storeId and productId
    private static final Map<AuctionKey, List<Offer>> offers = new HashMap<>();


    /// Map to store end times for each auction
    /// The key is a combination of storeId and productId
    private static final Map<AuctionKey, Long> endTimes = new HashMap<>();


    /// Map to store starting prices for each auction
    /// The key is a combination of storeId and productId
    private static final Map<AuctionKey, Double> startingPrices = new HashMap<>();


        /// When store opens auction
    /// This method takes storeId, productId, starting price, and end time in milliseconds
    /// It creates a new auction and schedules it to close at the end time
    /// It also initializes the offers list for that auction
    public static void openAuction(IListingRepository rep, String storeId, String productId, double startingPrice, long endTimeMillis, IShipmentService shipmentService, IPaymentService paymentService, IPurchaseRepository purchaseRep, NotificationService notifService) {
        purchaseRepository = purchaseRep;
        notificationService = notifService;
        long currentTime = System.currentTimeMillis();
        long delay = endTimeMillis - currentTime;
        
        if (delay <= 1000) { // Allow at least 1 second
            return;
        }
        
        listingRepository = rep;
        AuctionKey key = new AuctionKey(storeId, productId);
        offers.put(key, new ArrayList<>());
        endTimes.put(key, endTimeMillis);
        startingPrices.put(key, startingPrice);
        
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    closeAuction(storeId, productId, shipmentService, paymentService);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, delay);
    }



    /// When user submits an offer
    /// This method checks if the auction is active and if the offer is higher than the current maximum.
    /// If the offer is valid, it adds the offer to the list of offers for that auction.
    public static void submitOffer(String storeId, String productId, String userId, double price, String shippingAddress, String contactInfo) {
        AuctionKey key = new AuctionKey(storeId, productId);
        long now = System.currentTimeMillis();
        if (!endTimes.containsKey(key) || now > endTimes.get(key)) {
            throw new RuntimeException("Auction is not active.");
        }
        List<Offer> offerList = offers.getOrDefault(key, new ArrayList<>());
        double currentMax = offerList.stream()
                .mapToDouble(o -> o.price)
                .max()
                .orElse(0);
        if (price <= currentMax) {
            throw new RuntimeException("Offer must be higher than current maximum.");
        }
        offerList.add(new Offer(userId, price, shippingAddress, contactInfo));
        offers.put(key, offerList);
        for (Offer offer : offerList) {
            notificationService.sendNotification(offer.getUserId(), "New offer placed for auction: " + productId + " in store: " + storeId + ". Your offer: " + price + ". Current max offer: " + currentMax);
        }
    }



    /// When user wants to see the auction status
    /// This method returns the starting price, current maximum offer, and time left for the auction.
    public static Map<String, Object> getAuctionStatus(String storeId, String productId) {
        AuctionKey key = new AuctionKey(storeId, productId);
        Map<String, Object> status = new HashMap<>();
        
        double startingPrice = startingPrices.getOrDefault(key, 0.0);
        status.put("startingPrice", startingPrice);
        List<Offer> offerList = offers.getOrDefault(key, new ArrayList<>());
        double currentMax = offerList.stream()
                .mapToDouble(o -> o.price)
                .max()
                .orElse(startingPrice); // If no offers were placed, currentMax is the starting price
        status.put("currentMaxOffer", currentMax);
        long now = System.currentTimeMillis();
        long end = endTimes.getOrDefault(key, now);
        long timeLeftMillis = Math.max(0, end - now);
        status.put("timeLeftMillis", timeLeftMillis);
        
        return status;
    }



    /// When time ends for the auction
    /// This method checks if the auction is active and if it is, it closes the auction.
    /// It finds the winner by comparing the offers and creates a Purchase object for the winner.
    /// It also removes the auction from the maps.
    /// If no offers were placed, it indicates that the auction closed with no offers.
    public static Purchase closeAuction(String storeId, String productId, IShipmentService shipmentService, IPaymentService paymentService) {
        AuctionKey key = new AuctionKey(storeId, productId);
        long now = System.currentTimeMillis();
        if (!endTimes.containsKey(key)) {
            throw new RuntimeException("Auction is not active.");
        }
        if (now < endTimes.get(key)) {
            throw new RuntimeException("Auction has not ended yet.");
        }
        List<Offer> offerList = offers.getOrDefault(key, new ArrayList<>());
        offers.remove(key);
        endTimes.remove(key);
        startingPrices.remove(key);
        if (offerList.isEmpty()) {
            System.out.println("Auction closed with no offers.");
            return null;
        }
        Offer winner = offerList.stream()
                .max(Comparator.comparingDouble(o -> o.price))
                .orElseThrow();
        Map<String, Map<String, Integer>> listForUpdateStock = new HashMap<>();
        Map<String, Integer> productMap = new HashMap<>();
        productMap.put(productId, 1); // Assuming quantity is 1 for auction purchase
        listForUpdateStock.put(storeId, productMap);
        boolean updatedStock = listingRepository.updateOrRestoreStock(listForUpdateStock, false); 
        if (!updatedStock) {
            throw new RuntimeException("Failed to update stock for auction purchase.");
        }
        // Notify all users about the auction result
        notificationService.sendNotification(winner.userId, "Congratulations! You won the auction for product: " + productId + " in store: " + storeId + ". Your winning offer: " + winner.price);
        for(Offer offer : offerList) {
            if (!offer.userId.equals(winner.userId)) {
                notificationService.sendNotification(offer.userId, "You lost the auction for product: " + productId + " in store: " + storeId + ". Your offer: " + offer.price + ". Winning offer: " + winner.price);
            }
        }
        Purchase p = new AuctionPurchase().purchase(
                        winner.userId,
                        storeId,
                        productId,
                        winner.price,
                        winner.shippingAddress,
                        winner.contactInfo,
                        shipmentService,
                        paymentService
                    );
        
        return p;
    }
    
    
    public Purchase purchase(String userId, String storeId, String productId, double price, String shippingAddress, String paymentDetails, IShipmentService shipmentService, IPaymentService paymentService) {
        PurchasedProduct product = new PurchasedProduct(
                productId,
                storeId,
                1, //always 1 for auction purchase
                price // price is set when auction ends
        );
        paymentService.processPayment(paymentDetails);
        shipmentService.ship(shippingAddress, userId, 1); // Assuming weight is 1 for simplicity
        Purchase newP=new Purchase(userId, List.of(product), price, shippingAddress, paymentDetails);
        purchaseRepository.save(newP);
        return newP;
    }

    public static Map<AuctionKey, List<Offer>> getOffers() {
        return offers;
    }

    public static Map<AuctionKey, Long> getEndTimes() {
        return endTimes;
    }
    
    public static Map<AuctionKey, Double> getStartingPrices() {
        return startingPrices;
    }

    public static void setListingRepository(IListingRepository listingRepository) {
        AuctionPurchase.listingRepository = listingRepository;
    }

    public static void setPurchaseRepository(IPurchaseRepository purchaseRepository) {
        AuctionPurchase.purchaseRepository = purchaseRepository;
    }
    
    public static void setNotificationService(NotificationService notificationService2) {
        AuctionPurchase.notificationService = notificationService2;
    }
}
