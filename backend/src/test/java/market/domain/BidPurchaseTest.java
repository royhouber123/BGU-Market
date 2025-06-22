package market.domain;

import market.application.External.IPaymentService;
import market.application.External.IShipmentService;
import market.application.NotificationService;
import market.domain.purchase.*;
import market.domain.store.IListingRepository;
import market.infrastructure.AuctionRepository;
import market.infrastructure.BidRepository;
import utils.ApiResponse;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class BidPurchaseTest {

    private IShipmentService shipmentService;
    private IPaymentService paymentService;
    private IListingRepository listingRepository;
    private IPurchaseRepository purchaseRepository;
    private IBidRepository bidRepository;
    private NotificationService notificationService;

    private String storeId;
    private String productId;
    private String userId;
    private BidKey bidKey;
    private Set<String> approvers;

    @BeforeEach
    void setUp() {
        storeId = "store1";
        productId = "product1";
        userId = "user1";
        bidKey = new BidKey(storeId, productId);
        approvers = Set.of("owner1", "manager1");

        listingRepository = mock(IListingRepository.class);
        when(listingRepository.updateOrRestoreStock(anyMap(), eq(false))).thenReturn(true);
        BidPurchase.setListingRepository(listingRepository);

        purchaseRepository = mock(IPurchaseRepository.class);
        BidPurchase.setPurchaseRepository(purchaseRepository);

        bidRepository = new BidRepository();
        BidPurchase.setBidRepository(bidRepository);

        paymentService = mock(IPaymentService.class);
        when(paymentService.processPayment(anyString())).thenReturn(ApiResponse.ok(true));
        BidPurchase.setPaymentService(paymentService);

        shipmentService = mock(IShipmentService.class);
        when(shipmentService.ship(anyString(), anyString(), anyDouble())).thenReturn(ApiResponse.ok("trackingId"));
        BidPurchase.setShippingService(shipmentService);

        notificationService = mock(NotificationService.class);
        doNothing().when(notificationService).sendNotification(anyString(), anyString());
        BidPurchase.setNotificationService(notificationService);
    }

    @AfterEach
    void tearDown() {
        BidPurchase.getBids().clear();
    }

    private Bid createAndAddBid(double price, Set<String> approversOverride) {
        Bid bid = new Bid(userId, price, "addr", "contact", approversOverride);
        BidPurchase.getBids().computeIfAbsent(bidKey, k -> new ArrayList<>()).add(bid);
        
        BidEntity entity = bidRepository.findByStoreIdAndProductId(storeId, productId)
            .orElseGet(() -> new BidEntity(storeId, productId));
        entity.getBids().add(bid);
        bidRepository.save(entity);
        
        return bid;
    }

    private Bid createAndAddBid(double price) {
        return createAndAddBid(price, approvers);
    }

    @Test
    void submitBid_validBid_shouldAddToMap() {
        assertDoesNotThrow(() -> BidPurchase.submitBid(
                listingRepository, storeId, productId, userId, 100.0, "addr", "contact", approvers, shipmentService, paymentService, purchaseRepository, notificationService, bidRepository));
        assertTrue(BidPurchase.getBids().containsKey(bidKey));
    }

    @Test
    void submitBid_negativeAmount_shouldThrow() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> BidPurchase.submitBid(
                listingRepository, storeId, productId, userId, -50.0, "addr", "contact", approvers, shipmentService, paymentService, purchaseRepository, notificationService, bidRepository));
        assertEquals("Bid must be a positive value.", ex.getMessage());
    }

    @Test
    void approveBid_allApprovers_shouldCreatePurchase() {
        Bid bid = createAndAddBid(100.0);
        assertDoesNotThrow(() -> BidPurchase.approveBid(storeId, productId, userId, "owner1"));
        assertFalse(bid.isApproved());
        BidPurchase.approveBid(storeId, productId, userId, "manager1");
        assertTrue(bid.isApproved());
    }

    @Test
    void approveBid_notAnApprover_shouldThrow() {
        Set<String> onlyOwner = Set.of("owner1");
        createAndAddBid(100.0, onlyOwner);
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                BidPurchase.approveBid(storeId, productId, userId, "notAnApprover"));
        assertEquals("Approver is not authorized.", ex.getMessage());
    }

    @Test
    void rejectBid_shouldMarkAsRejected() {
        Bid bid = createAndAddBid(100.0);
        assertDoesNotThrow(() -> BidPurchase.rejectBid(storeId, productId, userId, "owner1"));
        assertTrue(bid.isRejected());
    }

    @Test
    void proposeCounterBid_validBid_shouldSetCounterOffer() {
        Bid bid = createAndAddBid(100.0);
        BidPurchase.proposeCounterBid(storeId, productId, userId, 120.0);
        assertEquals(120.0, bid.getCounterOfferAmount());
    }

    @Test
    void proposeCounterBid_negativeAmount_shouldThrow() {
        createAndAddBid(100.0);
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                BidPurchase.proposeCounterBid(storeId, productId, userId, -20.0));
        assertEquals("Counter offer must be a positive value.", ex.getMessage());
    }

    @Test /// 
    void acceptCounterOffer_shouldApproveBid() {
        Bid bid = createAndAddBid(100.0);
        bid.setCounterOfferAmount(120.0);
        BidPurchase.acceptCounterOffer(storeId, productId, userId);
        assertTrue(bid.isApproved());
        //assertEquals(120.0, bid.getPrice());
        //assertFalse(bid.isCounterOffered());
    }

    @Test
    void acceptCounterOffer_withoutCounter_shouldThrow() {
        createAndAddBid(100.0);
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                BidPurchase.acceptCounterOffer(storeId, productId, userId));
        assertEquals("No counter-offer found for user.", ex.getMessage());
    }

    @Test
    void declineCounterOffer_shouldRejectBid() {
        Bid bid = createAndAddBid(100.0);
        bid.setCounterOfferAmount(120.0);
        BidPurchase.declineCounterOffer(storeId, productId, userId);
        assertTrue(bid.isRejected());
    }

    @Test
    void declineCounterOffer_withoutCounter_shouldThrow() {
        createAndAddBid(100.0);
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                BidPurchase.declineCounterOffer(storeId, productId, userId));
        assertEquals("No counter-offer found for user.", ex.getMessage());
    }

    @Test
    void getBidStatus_variousStatuses_shouldReturnCorrectStatus() {
        Bid bid = createAndAddBid(100.0);
        assertEquals("Pending Approval", BidPurchase.getBidStatus(storeId, productId, userId));

        bid.approve("owner1");
        bid.approve("manager1");
        assertEquals("Approved", BidPurchase.getBidStatus(storeId, productId, userId));

        Bid counterBid = new Bid("user2", 150.0, "addr", "contact", approvers);
        counterBid.proposeCounterOffer(170.0);
        BidPurchase.getBids().get(bidKey).add(counterBid);
        assertEquals("Counter Offered: 170.0", BidPurchase.getBidStatus(storeId, productId, "user2"));

        Bid rejectedBid = new Bid("user3", 90.0, "addr", "contact", approvers);
        rejectedBid.reject("owner1");
        BidPurchase.getBids().get(bidKey).add(rejectedBid);
        assertEquals("Rejected", BidPurchase.getBidStatus(storeId, productId, "user3"));
    }

    @Test
    void getBidStatus_noBid_shouldReturnNoBidFound() {
        String status = BidPurchase.getBidStatus("storeX", "productX", "nonexistentUser");
        assertEquals("No Bid Found", status);
    }
}
