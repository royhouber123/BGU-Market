package market.domain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import market.application.External.PaymentService;
import market.domain.purchase.*;
import market.domain.store.IStoreRepository;
import market.infrastructure.StoreRepository;
import market.application.External.ShipmentService;


class AuctionPurchaseTest {

    //AuctionPurchase auctionPurchase;
    IStoreRepository storeRepository;   
    @BeforeEach
    void setUp() {
        // No specific setup needed for now
        //auctionPurchase = new AuctionPurchase();
        storeRepository = new StoreRepository(); // Replace with a mock or concrete implementation
    }

    @Test
    void testOpenAuctionSuccess() {
        String storeId = "store1";
        String productId = "prod1";
        double startingPrice = 100.0;
        long endTimeMillis = System.currentTimeMillis() + 1 * 60 * 1000; // one minute from now
        ShipmentService shipmentService = new ShipmentService();
        PaymentService paymentService = new PaymentService();
        assertDoesNotThrow(() -> {
            AuctionPurchase.openAuction(this.storeRepository, storeId, productId, startingPrice, endTimeMillis, shipmentService, paymentService);
        });
        Map<AuctionKey, List<Offer>> offers = AuctionPurchase.getOffers();
        AuctionKey auctionKey = new AuctionKey(storeId, productId);
        assertTrue(offers.containsKey(auctionKey));
    }

    @Test
    void testSubmitOfferSuccess() {
        String storeId = "store1";
        String productId = "prod1";
        String userId = "user1";
        double startingPrice = 100.0;
        double offerPrice = 150.0;
        String shippingAddress = "123 Main St";
        String contactInfo = "555-555-5555";
        long endTimeMillis = System.currentTimeMillis() + 1 * 60 * 1000; // one minute from now
        ShipmentService shipmentService = new ShipmentService();
        PaymentService paymentService = new PaymentService();
    
        // Open an auction
        assertDoesNotThrow(() -> {
            AuctionPurchase.openAuction(this.storeRepository, storeId, productId, startingPrice, endTimeMillis, shipmentService, paymentService);
        });
    
        // Submit an offer
        assertDoesNotThrow(() -> {
            AuctionPurchase.submitOffer(storeId, productId, userId, offerPrice, shippingAddress, contactInfo);
        });
    
        // Verify the offer was added
        Map<AuctionKey, List<Offer>> offers = AuctionPurchase.getOffers();
        AuctionKey auctionKey = new AuctionKey(storeId, productId);
        assertTrue(offers.containsKey(auctionKey));
        List<Offer> offerList = offers.get(auctionKey);
        assertFalse(offerList.isEmpty());
        assertEquals(offerPrice, offerList.get(0).getPrice());
        assertEquals(userId, offerList.get(0).getUserId());
    }

    @Test
    void testGetAuctionStatusSuccess() {
        String storeId = "store1";
        String productId = "prod1";
        double startingPrice = 100.0;
        long endTimeMillis = System.currentTimeMillis() + 1 * 60 * 1000; // one minute from now
        ShipmentService shipmentService = new ShipmentService();
        PaymentService paymentService = new PaymentService();

        // Open an auction
        assertDoesNotThrow(() -> {
            AuctionPurchase.openAuction(this.storeRepository, storeId, productId, startingPrice, endTimeMillis, shipmentService, paymentService);
        });

        // Get auction status
        Map<String, Object> auctionStatus = AuctionPurchase.getAuctionStatus(storeId, productId);

        // Verify auction status
        assertNotNull(auctionStatus);
        assertTrue(auctionStatus.containsKey("startingPrice"));
        assertTrue(auctionStatus.containsKey("currentMaxOffer"));
        assertTrue(auctionStatus.containsKey("timeLeftMillis"));

        assertEquals(100.0, auctionStatus.get("startingPrice")); // Auction should be open
        assertEquals(0.0, auctionStatus.get("currentMaxOffer")); // No bids yet
        assertEquals(endTimeMillis, auctionStatus.get("timeLeftMillis"));

        
    }

    @Test
    void testCloseAuctionSuccess() {
        String storeId = "store1";
        String productId = "prod1";
        String userId = "user1";
        double startingPrice = 100.0;
        double winningPrice = 150.0;
        String shippingAddress = "123 Main St";
        String contactInfo = "555-555-5555";
        long endTimeMillis = System.currentTimeMillis() + 1 * 60 * 1000; // one minute from now
        ShipmentService shipmentService = new ShipmentService();
        PaymentService paymentService = new PaymentService();

        // Open an auction
        assertDoesNotThrow(() -> {
            AuctionPurchase.openAuction(this.storeRepository, storeId, productId, startingPrice, endTimeMillis, shipmentService, paymentService);
        });

        // Submit a winning offer
        assertDoesNotThrow(() -> {
            AuctionPurchase.submitOffer(storeId, productId, userId, winningPrice, shippingAddress, contactInfo);
        });

        // Close the auction
        assertDoesNotThrow(() -> {
            Purchase purchase = AuctionPurchase.closeAuction(storeId, productId, shipmentService, paymentService);

            // Verify the purchase details
            assertNotNull(purchase);
            assertEquals(userId, purchase.getUserId());
            assertEquals(winningPrice, purchase.getTotalPrice());
            assertEquals(1, purchase.getProducts().size());
            assertEquals(productId, purchase.getProducts().get(0).getProductId());
            assertEquals(shippingAddress, purchase.getShippingAddress());
            assertEquals(contactInfo, purchase.getContactInfo());

        });

        // Verify the auction is no longer open
        Map<String, Object> auctionStatus = AuctionPurchase.getAuctionStatus(storeId, productId);
        assertNotNull(auctionStatus);
        assertEquals(0, auctionStatus.get("timeLeftMillis")); // Auction should be closed

    }

    @Test
    void testPurchaseCreation() {
        String storeId = "store1";
        String productId = "prod1";
        String userId = "user1";
        double price = 150.0;
        String shippingAddress = "123 Main St";
        String contactInfo = "555-555-5555";
        ShipmentService shipmentService = new ShipmentService();
        PaymentService paymentService = new PaymentService();

        // Create a purchase
        Purchase purchase = new AuctionPurchase().purchase(
            userId, storeId, productId, price, shippingAddress, contactInfo, shipmentService, paymentService
        );

        // Verify the purchase details
        assertNotNull(purchase);
        assertEquals(userId, purchase.getUserId());
        assertEquals(price, purchase.getTotalPrice());
        assertEquals(1, purchase.getProducts().size());
        assertEquals(productId, purchase.getProducts().get(0).getProductId());
        assertEquals(storeId, purchase.getProducts().get(0).getStoreId());
        assertEquals(shippingAddress, purchase.getShippingAddress());
        assertEquals(contactInfo, purchase.getContactInfo());

    }
}
