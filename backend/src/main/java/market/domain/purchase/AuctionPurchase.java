package market.domain.purchase;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import market.domain.store.*;
import market.application.External.IPaymentService;
import market.application.External.IShipmentService;

import java.util.Timer;


public class AuctionPurchase {

    //to update and check stock
    private static IStoreRepository storeRepository;
    private static IPurchaseRepository purchaseRepository;


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
    public static void openAuction(IStoreRepository rep, String storeId, String productId, double startingPrice, long endTimeMillis, IShipmentService shipmentService, IPaymentService paymentService, IPurchaseRepository purchaseRep) {
        purchaseRepository = purchaseRep;
        long delay = endTimeMillis - System.currentTimeMillis();
        if (delay <= 0) return;
        storeRepository = rep;
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
    public static void submitOffer(String storeId, String productId, String userId, double price, String shippingAddress, String contactInfo, String currency, String cardNumber, String month, String year, String holder, String ccv) {
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
        offerList.add(new Offer(userId, price, shippingAddress, contactInfo, currency, cardNumber, month, year, holder, ccv));
        offers.put(key, offerList);
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
        boolean updatedStock = storeRepository.updateStockForPurchasedItems(listForUpdateStock);
        if (!updatedStock) {
            throw new RuntimeException("Failed to update stock for auction purchase.");
        }
        Purchase p = new AuctionPurchase().purchase(
                        winner.userId,
                        storeId,
                        productId,
                        winner.price,
                        winner.shippingAddress,
                        winner.contactInfo,
                        shipmentService,
                        paymentService,
                        winner.currency,
                        winner.cardNumber,
                        winner.month,
                        winner.year,
                        winner.holder,
                        winner.ccv
                    );
        return p;
    }
    
    
    public Purchase purchase(String userId, String storeId, String productId, double price, String shippingAddress, String contactInfo, IShipmentService shipmentService, IPaymentService paymentService) {
        PurchasedProduct product = new PurchasedProduct(
                productId,
                storeId,
                1, //always 1 for auction purchase
                price // price is set when auction ends
        );
        
        // Updated method calls to match interface signatures
        // IPaymentService.processPayment(String currency, double amount, String cardNumber, String month, String year, String holder, String ccv)
        // For auction, we'll need to get payment details from the user or use default values
        // This assumes you have payment details available - you may need to modify this based on your data structure
        String paymentId = paymentService.processPayment(
            "USD",           // currency - default or from system config
            price,           // amount
            "4111111111111111", // cardNumber - this should come from user input
            "12",            // month - this should come from user input
            "2025",          // year - this should come from user input
            userId,          // holder - using userId as holder name
            "123"            // ccv - this should come from user input
        );
        
        // Parse shipping address to extract components (assuming format: "Name, Address, City, Country, ZIP")
        String[] addressParts = shippingAddress.split(", ");
        String name = addressParts.length > 0 ? addressParts[0] : userId;
        String address = addressParts.length > 1 ? addressParts[1] : shippingAddress;
        String city = addressParts.length > 2 ? addressParts[2] : "Unknown";
        String country = addressParts.length > 3 ? addressParts[3] : "Unknown";
        String zip = addressParts.length > 4 ? addressParts[4] : "00000";
        
        // IShipmentService.ship(String name, String address, String city, String country, String zip)
        String trackingId = shipmentService.ship(name, address, city, country, zip);
        
        Purchase newP = new Purchase(userId, List.of(product), price, shippingAddress, contactInfo);
        purchaseRepository.save(newP);
        return newP;
    }

    // Update the purchase method to include payment details in constructor parameters
    public Purchase purchase(String userId, String storeId, String productId, double price, String shippingAddress, String contactInfo, IShipmentService shipmentService, IPaymentService paymentService, String currency, String cardNumber, String month, String year, String holder, String ccv) {
        PurchasedProduct product = new PurchasedProduct(
                productId,
                storeId,
                1, //always 1 for auction purchase
                price // price is set when auction ends
        );
        
        // Updated method calls to match interface signatures
        String paymentId = paymentService.processPayment(currency, price, cardNumber, month, year, holder, ccv);
        
        // Parse shipping address to extract components
        String[] addressParts = shippingAddress.split(", ");
        String name = addressParts.length > 0 ? addressParts[0] : holder; // Use holder name if available
        String address = addressParts.length > 1 ? addressParts[1] : shippingAddress;
        String city = addressParts.length > 2 ? addressParts[2] : "Unknown";
        String country = addressParts.length > 3 ? addressParts[3] : "Unknown";
        String zip = addressParts.length > 4 ? addressParts[4] : "00000";
        
        String trackingId = shipmentService.ship(name, address, city, country, zip);
        
        Purchase newP = new Purchase(userId, List.of(product), price, shippingAddress, contactInfo);
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

    public static void setStoreRepository(IStoreRepository storeRepository) {
        AuctionPurchase.storeRepository = storeRepository;
    }

    public static void setPurchaseRepository(IPurchaseRepository purchaseRepository) {
        AuctionPurchase.purchaseRepository = purchaseRepository;
    }
}
