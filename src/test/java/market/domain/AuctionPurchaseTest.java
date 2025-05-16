package market.domain;

import market.application.External.IPaymentService;
import market.application.External.IShipmentService;
import market.application.External.PaymentService;
import market.application.External.ShipmentService;
import market.domain.purchase.*;
import market.domain.store.IStoreRepository;
import utils.ApiResponse;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class AuctionPurchaseTest {

    private IShipmentService shipmentService;
    private IPaymentService paymentService;
    private IStoreRepository storeRepository;
    private String storeId;
    private String productId;
    private double startingPrice;
    private long endTimeMillis;

    @BeforeEach
    void setUp() {
        storeRepository = mock(IStoreRepository.class);
        when(storeRepository.updateStockForPurchasedItems(anyMap())).thenReturn(true);
        BidPurchase.setStoreRepository(storeRepository);

        paymentService = mock(IPaymentService.class);
        when(paymentService.processPayment(anyString())).thenReturn(ApiResponse.ok(true));
        BidPurchase.setPaymentService(paymentService);

        shipmentService = mock(IShipmentService.class);
        when(shipmentService.ship(anyString(), anyString(), anyDouble())).thenReturn(ApiResponse.ok("trackingId"));
        BidPurchase.setShippingService(shipmentService);

        storeId = "store1";
        productId = "prod1";
        startingPrice = 100.0;
        endTimeMillis = System.currentTimeMillis() + 2000;
    }

    @AfterEach
    void tearDown() {
        AuctionPurchase.getOffers().clear();
        AuctionPurchase.getEndTimes().clear();
        AuctionPurchase.getStartingPrices().clear();
    }

    @Test
    void openAuction_validInput_shouldCreateAuction() {
        AuctionPurchase.openAuction(storeRepository, storeId, productId, startingPrice, endTimeMillis, shipmentService, paymentService);
        AuctionKey key = new AuctionKey(storeId, productId);
        assertTrue(AuctionPurchase.getOffers().containsKey(key));
    }

    @Test
    void openAuction_pastEndTime_shouldNotScheduleAuction() {
        long pastEndTime = System.currentTimeMillis() - 1000;
        AuctionPurchase.openAuction(storeRepository, storeId, productId, startingPrice, pastEndTime, shipmentService, paymentService);
        AuctionKey key = new AuctionKey(storeId, productId);
        assertFalse(AuctionPurchase.getOffers().containsKey(key));
    }

    @Test
    void submitOffer_validOffer_shouldAddOffer() {
        AuctionPurchase.openAuction(storeRepository, storeId, productId, startingPrice, endTimeMillis, shipmentService, paymentService);
        AuctionPurchase.submitOffer(storeId, productId, "user1", 150.0, "addr", "contact");
        AuctionKey key = new AuctionKey(storeId, productId);
        List<Offer> offers = AuctionPurchase.getOffers().get(key);
        assertEquals(1, offers.size());
    }

    @Test
    void submitOffer_lowerThanCurrentMax_shouldFail() {
        AuctionPurchase.openAuction(storeRepository, storeId, productId, startingPrice, endTimeMillis, shipmentService, paymentService);
        AuctionPurchase.submitOffer(storeId, productId, "user1", 150.0, "addr", "contact");
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                AuctionPurchase.submitOffer(storeId, productId, "user2", 140.0, "addr", "contact")
        );
        assertEquals("Offer must be higher than current maximum.", ex.getMessage());
    }

    @Test
    void getAuctionStatus_noOffers_shouldReturnStartingPriceAsMax() {
        AuctionPurchase.openAuction(storeRepository, storeId, productId, startingPrice, endTimeMillis, shipmentService, paymentService);
        Map<String, Object> status = AuctionPurchase.getAuctionStatus(storeId, productId);
        assertEquals(startingPrice, status.get("currentMaxOffer"));
    }

    @Test
    void getAuctionStatus_withOffers_shouldReturnMaxOffer() {
        AuctionPurchase.openAuction(storeRepository, storeId, productId, startingPrice, endTimeMillis, shipmentService, paymentService);
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

        Purchase p = AuctionPurchase.closeAuction(storeId, productId, shipmentService, paymentService);
        assertNotNull(p);
        assertEquals("user1", p.getUserId());
    }

    @Test
    void auction_shouldAutoCloseAfterEndTime() throws InterruptedException {
        long shortEndTime = System.currentTimeMillis() + 500;
        AuctionPurchase.openAuction(storeRepository, storeId, productId, startingPrice, shortEndTime, shipmentService, paymentService);
        AuctionPurchase.submitOffer(storeId, productId, "user1", 200.0, "addr", "contact");
        Thread.sleep(600);
        AuctionKey key = new AuctionKey(storeId, productId);
        assertFalse(AuctionPurchase.getOffers().containsKey(key));
    }

    @Test
    void closeAuction_beforeEnd_shouldThrowException() {
        AuctionPurchase.openAuction(storeRepository, storeId, productId, startingPrice, endTimeMillis, shipmentService, paymentService);
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                AuctionPurchase.closeAuction(storeId, productId, shipmentService, paymentService)
        );
        assertEquals("Auction has not ended yet.", ex.getMessage());
    }
}
