package market.domain;

import market.application.External.IPaymentService;
import market.application.External.IShipmentService;
import market.application.External.PaymentService;
import market.application.External.ShipmentService;
import market.domain.purchase.*;
import market.domain.store.IListingRepository;
import market.infrastructure.IJpaRepository.IAuctionJpaRepository;
import market.infrastructure.*;
import utils.ApiResponse;
import market.application.NotificationService;


import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class AuctionPurchaseTest {

    private IShipmentService shipmentService;
    private IPaymentService paymentService;
    private IListingRepository listingRepository;
    private IPurchaseRepository purchaseRepository;
    private IAuctionRepository auctionRepository;
    private NotificationService notificationService;
    private String storeId;
    private String productId;
    private double startingPrice;
    private long endTimeMillis;

    @BeforeEach
    void setUp() {
        listingRepository = mock(IListingRepository.class);
        when(listingRepository.updateOrRestoreStock(anyMap(), eq(false))).thenReturn(true);
        AuctionPurchase.setListingRepository(listingRepository);

        purchaseRepository = mock(IPurchaseRepository.class);
        AuctionPurchase.setPurchaseRepository(purchaseRepository);

        paymentService = mock(IPaymentService.class);
        when(paymentService.processPayment(anyString())).thenReturn(ApiResponse.ok(true));

        shipmentService = mock(IShipmentService.class);
        when(shipmentService.ship(anyString(), anyString(), anyDouble())).thenReturn(ApiResponse.ok("trackingId"));

        auctionRepository = new AuctionRepository();
        AuctionPurchase.setAuctionRepository(auctionRepository);

        notificationService = mock(NotificationService.class);
        doNothing().when(notificationService).sendNotification(anyString(), anyString());
        AuctionPurchase.setNotificationService(notificationService);

        storeId = "store1";
        productId = "prod1";
        startingPrice = 100.0;
        endTimeMillis = System.currentTimeMillis() + 10000;
    }

    @AfterEach
    void tearDown() {
        AuctionPurchase.getOffers().clear();
        AuctionPurchase.getEndTimes().clear();
        AuctionPurchase.getStartingPrices().clear();
    }

    @Test
    void openAuction_validInput_shouldCreateAuction() {
        AuctionPurchase.openAuction(listingRepository, storeId, productId, startingPrice, endTimeMillis, shipmentService, paymentService, purchaseRepository, notificationService, auctionRepository);
        AuctionKey key = new AuctionKey(storeId, productId);
        assertTrue(AuctionPurchase.getOffers().containsKey(key));
    }

    @Test
    void openAuction_pastEndTime_shouldNotScheduleAuction() {
        long pastEndTime = System.currentTimeMillis() - 1000;
        AuctionPurchase.openAuction(listingRepository, storeId, productId, startingPrice, pastEndTime, shipmentService, paymentService, purchaseRepository, notificationService, auctionRepository);
        AuctionKey key = new AuctionKey(storeId, productId);
        assertFalse(AuctionPurchase.getOffers().containsKey(key));
    }

    @Test
    void submitOffer_validOffer_shouldAddOffer() {
        AuctionPurchase.openAuction(listingRepository, storeId, productId, startingPrice, endTimeMillis, shipmentService, paymentService, purchaseRepository, notificationService, auctionRepository);
        AuctionPurchase.submitOffer(storeId, productId, "user1", 150.0, "addr", "contact");
        AuctionKey key = new AuctionKey(storeId, productId);
        List<Offer> offers = AuctionPurchase.getOffers().get(key);
        assertEquals(1, offers.size());
    }

    @Test
    void submitOffer_lowerThanCurrentMax_shouldFail() {
        AuctionPurchase.openAuction(listingRepository, storeId, productId, startingPrice, endTimeMillis, shipmentService, paymentService, purchaseRepository, notificationService, auctionRepository);
        AuctionPurchase.submitOffer(storeId, productId, "user1", 150.0, "addr", "contact");
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                AuctionPurchase.submitOffer(storeId, productId, "user2", 140.0, "addr", "contact")
        );
        assertEquals("Offer must be higher than current maximum.", ex.getMessage());
    }

    @Test
    void getAuctionStatus_noOffers_shouldReturnStartingPriceAsMax() {
        AuctionPurchase.openAuction(listingRepository, storeId, productId, startingPrice, endTimeMillis, shipmentService, paymentService, purchaseRepository, notificationService, auctionRepository);
        Map<String, Object> status = AuctionPurchase.getAuctionStatus(storeId, productId);
        assertEquals(startingPrice, status.get("currentMaxOffer"));
    }

    @Test
    void getAuctionStatus_withOffers_shouldReturnMaxOffer() {
        AuctionPurchase.openAuction(listingRepository, storeId, productId, startingPrice, endTimeMillis, shipmentService, paymentService, purchaseRepository, notificationService, auctionRepository);
        AuctionPurchase.submitOffer(storeId, productId, "user1", 200.0, "addr", "contact");
        Map<String, Object> status = AuctionPurchase.getAuctionStatus(storeId, productId);
        assertEquals(200.0, status.get("currentMaxOffer"));
    }

    @Test
    void closeAuction_manualCall_shouldReturnPurchase() {
        long fakeEndTime = System.currentTimeMillis() - 100; // already passed
        AuctionKey key = new AuctionKey(storeId, productId);

        AuctionPurchase.getOffers().put(key, List.of(new Offer("user1", 200.0, "addr", "contact")));
        AuctionPurchase.getEndTimes().put(key, fakeEndTime);
        AuctionPurchase.getStartingPrices().put(key, startingPrice);


        AuctionEntity entity = new AuctionEntity(storeId, productId, startingPrice, fakeEndTime);
        auctionRepository.save(entity);

        Purchase p = AuctionPurchase.closeAuction(storeId, productId, shipmentService, paymentService);
        assertNotNull(p);
        assertEquals("user1", p.getUserId());
    }

    @Test
    void auction_shouldAutoCloseAfterEndTime() throws InterruptedException {
        long shortEndTime = System.currentTimeMillis() + 1500;
        AuctionPurchase.openAuction(listingRepository, storeId, productId, startingPrice, shortEndTime, shipmentService, paymentService, purchaseRepository, notificationService, auctionRepository);
        AuctionPurchase.submitOffer(storeId, productId, "user1", 200.0, "addr", "contact");
        
        // Wait for auction to end plus buffer for cleanup
        Thread.sleep(1700);
        
        AuctionKey key = new AuctionKey(storeId, productId);
        
        // Verify auction has been cleaned up
        assertFalse(AuctionPurchase.getOffers().containsKey(key), "Auction should have been closed and cleaned up");
    }

    @Test
    void closeAuction_beforeEnd_shouldThrowException() {
        AuctionPurchase.openAuction(listingRepository, storeId, productId, startingPrice, endTimeMillis, shipmentService, paymentService, purchaseRepository, notificationService, auctionRepository);
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                AuctionPurchase.closeAuction(storeId, productId, shipmentService, paymentService)
        );
        assertEquals("Auction has not ended yet.", ex.getMessage());
    }
}
