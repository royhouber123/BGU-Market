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
    void testOpenAuctionInitialStatus() {
        String storeId = "store1";
        String productId = "prod1";
        double startingPrice = 100.0;
        long endTimeMillis = System.currentTimeMillis() + 5000; // 5 שניות מהעכשיו
        ShipmentService shipmentService = new ShipmentService();
        PaymentService paymentService = new PaymentService();
    
        assertDoesNotThrow(() -> {
            AuctionPurchase.openAuction(this.storeRepository, storeId, productId, startingPrice, endTimeMillis, shipmentService, paymentService);
        });
    
        // בודקים שנוצר המכרז נכון
        Map<String, Object> auctionStatus = AuctionPurchase.getAuctionStatus(storeId, productId);
        assertNotNull(auctionStatus);
        assertEquals(startingPrice, auctionStatus.get("startingPrice"));
        assertEquals(startingPrice, auctionStatus.get("currentMaxOffer")); // אין עדיין הצעות
        assertTrue((long)auctionStatus.get("timeLeftMillis") > 0); // נותר זמן
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
            Thread.sleep(50);
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
        long auctionDurationMillis = 2000; // נניח מכירה של 2 שניות
        long endTimeMillis = System.currentTimeMillis() + auctionDurationMillis;
        ShipmentService shipmentService = new ShipmentService();
        PaymentService paymentService = new PaymentService();

        // Open an auction
        assertDoesNotThrow(() -> {
            AuctionPurchase.openAuction(this.storeRepository, storeId, productId, startingPrice, endTimeMillis, shipmentService, paymentService);
        });

        // Immediately get auction status
        Map<String, Object> auctionStatus = AuctionPurchase.getAuctionStatus(storeId, productId);

        // Verify auction status right after opening
        assertNotNull(auctionStatus);

        assertTrue(auctionStatus.containsKey("startingPrice"));
        assertTrue(auctionStatus.containsKey("currentMaxOffer"));
        assertTrue(auctionStatus.containsKey("timeLeftMillis"));

        assertEquals(startingPrice, (double) auctionStatus.get("startingPrice"));
        assertEquals(startingPrice, (double) auctionStatus.get("currentMaxOffer")); // אין הצעות עדיין

        long timeLeftMillis = (long) auctionStatus.get("timeLeftMillis");
        assertTrue(timeLeftMillis > 0, "Auction should still be active immediately after opening");

        // עכשיו נחכה שהמכרז יסתיים
        try {
            Thread.sleep(auctionDurationMillis + 200); // מחכים קצת יותר מהזמן של המכרז
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // אם מישהו עוצר את הת'רד, לא להתעלם
        }

        // אחרי שהסתיים – בודקים שוב את הסטטוס
        Map<String, Object> auctionStatusAfterEnd = AuctionPurchase.getAuctionStatus(storeId, productId);

        long timeLeftAfterEnd = (long) auctionStatusAfterEnd.get("timeLeftMillis");
        assertEquals(0, timeLeftAfterEnd, "Auction should be closed after end time");
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
