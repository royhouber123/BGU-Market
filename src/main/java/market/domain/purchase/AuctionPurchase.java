package market.domain.purchase;
import java.security.Key;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import market.model.*;

public class AuctionPurchase implements IPurchase {

    /// AuctionKey is a combination of storeId and productId
    /// This is used to identify auctions uniquely
    private static class AuctionKey {
        String storeId;
        String productId;

        AuctionKey(String storeId, String productId) {
            this.storeId = storeId;
            this.productId = productId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AuctionKey)) return false;
            AuctionKey other = (AuctionKey) o;
            return storeId.equals(other.storeId) && productId.equals(other.productId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(storeId, productId);
        }
    }

    /// Offer is a class that represents an offer made by a user
    /// It contains the userId and the amount of the offer
    private static class Offer {
        String userId;
        double amount;
    
        Offer(String userId, double amount) {
            this.userId = userId;
            this.amount = amount;
        }
    }


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
    /// This method creates a new auction with the given storeId and productId
    /// and sets the end time for the auction
    public static void openAuction(String storeId, String productId, double startingPrice, long endTimeMillis) {
        AuctionKey key = new AuctionKey(storeId, productId);
        offers.put(key, new ArrayList<>());
        endTimes.put(key, endTimeMillis);
        startingPrices.put(key, startingPrice);
    }



    /// When user submits an offer
    /// This method checks if the auction is active and if the offer is higher than the current maximum.
    /// If the offer is valid, it adds the offer to the list of offers for that auction.
    public static void submitOffer(String storeId, String productId, String userId, double amount) {
        AuctionKey key = new AuctionKey(storeId, productId);
        long now = System.currentTimeMillis();
        if (!endTimes.containsKey(key) || now > endTimes.get(key)) {
            throw new RuntimeException("Auction is not active.");
        }
        List<Offer> offerList = offers.getOrDefault(key, new ArrayList<>());
        double currentMax = offerList.stream()
                .mapToDouble(o -> o.amount)
                .max()
                .orElse(0);
        if (amount <= currentMax) {
            throw new RuntimeException("Offer must be higher than current maximum.");
        }
        offerList.add(new Offer(userId, amount));
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
                .mapToDouble(o -> o.amount)
                .max()
                .orElse(startingPrice); // If no offers were placed, currentMax is the starting price
        status.put("currentMaxOffer", currentMax);
        long now = System.currentTimeMillis();
        long end = endTimes.getOrDefault(key, now);
        long timeLeftMillis = Math.max(0, end - now);
        status.put("timeLeftMillis", timeLeftMillis);
        return status;
    }



    /// When store closes auction??? when? and how?
    /// This method returns the userId of the winning offer.
    /// or null if no offers were placed.
    /// It also removes the auction from the maps
    public static String closeAuction(String storeId, String productId) {
        AuctionKey key = new AuctionKey(storeId, productId);
        long now = System.currentTimeMillis();
        if (!endTimes.containsKey(key))
            throw new RuntimeException("Auction not found.");
        if (now < endTimes.get(key))
            throw new RuntimeException("Auction has not ended yet.");
        List<Offer> offerList = offers.getOrDefault(key, new ArrayList<>());
        offers.remove(key);
        endTimes.remove(key);
        if (offerList.isEmpty()) {
            return null; // No offers were placed so no one wins
        }
        return offerList.stream()
                .max(Comparator.comparingDouble(o -> o.amount))
                .map(o -> o.userId)
                .orElse(null);
    }



    @Override
    public Purchase purchase(String userId, ShoppingCart cart, String shippingAddress, String contactInfo) {
        if (cart.getStoreBags().size() != 1)
            throw new RuntimeException("Auction purchase must be for one store.");
        StoreBag bag = cart.getStoreBags().get(0);
        if (bag.getItems().size() != 1)
            throw new RuntimeException("Auction purchase must contain one product.");
        CartItem item = new ArrayList<>(bag.getItems()).get(0);
        PurchasedProduct product = new PurchasedProduct(
                item.getProductId(),
                bag.getStoreId(),
                1, //always 1 for auction purchase
                item.getUnitPrice() // price is set when auction ends
        );
        return new Purchase(userId, List.of(product), product.getTotalPrice(), shippingAddress, contactInfo);
    }
}
