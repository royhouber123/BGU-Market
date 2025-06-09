package market.domain;

import market.application.External.IPaymentService;
import market.application.External.IShipmentService;
import market.domain.purchase.*;
import market.domain.store.IStoreRepository;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class BidPurchaseTest {

    private IShipmentService shipmentService;
    private IPaymentService paymentService;
    private IStoreRepository storeRepository;
    private IPurchaseRepository purchaseRepository;

    private String storeId;
    private String productId;
    private String userId;
    private BidKey bidKey;
    private Set<String> approvers;
    
    // Payment details for tests
    private String currency = "USD";
    private String cardNumber = "4111111111111111";
    private String month = "12";
    private String year = "2025";
    private String holder = "John Doe";
    private String ccv = "123";

    @BeforeEach
    void setUp() {
        storeId = "store1";
        productId = "product1";
        userId = "user1";
        bidKey = new BidKey(storeId, productId);
        approvers = Set.of("owner1", "manager1");

        storeRepository = mock(IStoreRepository.class);
        when(storeRepository.updateStockForPurchasedItems(anyMap())).thenReturn(true);
        BidPurchase.setStoreRepository(storeRepository);

        purchaseRepository = mock(IPurchaseRepository.class);
        BidPurchase.setPurchaseRepository(purchaseRepository);

        paymentService = mock(IPaymentService.class);
        // Updated to match new interface: processPayment(String currency, double amount, String cardNumber, String month, String year, String holder, String ccv)
        when(paymentService.processPayment(anyString(), anyDouble(), anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn("payment-id-123");
        when(paymentService.cancelPayment(anyString())).thenReturn(true);
        BidPurchase.setPaymentService(paymentService);

        shipmentService = mock(IShipmentService.class);
        // Updated to match new interface: ship(String name, String address, String city, String country, String zip)
        when(shipmentService.ship(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn("tracking-id-123");
        when(shipmentService.cancel(anyString())).thenReturn(true);
        BidPurchase.setShippingService(shipmentService);
    }

    @AfterEach
    void tearDown() {
        BidPurchase.getBids().clear();
    }

    private Bid createAndAddBid(double price, Set<String> approversOverride) {
        // Updated Bid constructor to include payment details
        Bid bid = new Bid(userId, price, "John Doe, 123 Main St, New York, USA, 12345", "contact@example.com", approversOverride, currency, cardNumber, month, year, holder, ccv);
        BidPurchase.getBids().computeIfAbsent(bidKey, k -> new ArrayList<>()).add(bid);
        return bid;
    }

    private Bid createAndAddBid(double price) {
        return createAndAddBid(price, approvers);
    }

    @Test
    void submitBid_validBid_shouldAddToMap() {
        assertDoesNotThrow(() -> BidPurchase.submitBid(
                storeRepository, storeId, productId, userId, 100.0, "John Doe, 123 Main St, New York, USA, 12345", "contact@example.com", approvers, shipmentService, paymentService, purchaseRepository, currency, cardNumber, month, year, holder, ccv));
        assertTrue(BidPurchase.getBids().containsKey(bidKey));
    }

    @Test
    void submitBid_negativeAmount_shouldThrow() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> BidPurchase.submitBid(
                storeRepository, storeId, productId, userId, -50.0, "John Doe, 123 Main St, New York, USA, 12345", "contact@example.com", approvers, shipmentService, paymentService, purchaseRepository, currency, cardNumber, month, year, holder, ccv));
        assertEquals("Bid must be a positive value.", ex.getMessage());
    }

    @Test
    void approveBid_allApprovers_shouldCreatePurchase() {
        Bid bid = createAndAddBid(100.0);
        assertDoesNotThrow(() -> BidPurchase.approveBid(storeId, productId, userId, "owner1"));
        assertFalse(bid.isApproved());
        BidPurchase.approveBid(storeId, productId, userId, "manager1");
        assertTrue(bid.isApproved());
        
        // Verify that payment and shipment services were called with correct parameters
        verify(paymentService).processPayment(eq(currency), eq(100.0), eq(cardNumber), eq(month), eq(year), eq(holder), eq(ccv));
        verify(shipmentService).ship(eq("John Doe"), eq("123 Main St"), eq("New York"), eq("USA"), eq("12345"));
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
        // Fixed: removed the approver parameter as it's not in the method signature
        BidPurchase.proposeCounterBid(storeId, productId, userId, 120.0);
        assertEquals(120.0, bid.getCounterOfferAmount());
        assertTrue(bid.isCounterOffered());
    }

    @Test
    void proposeCounterBid_negativeAmount_shouldThrow() {
        createAndAddBid(100.0);
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                // Fixed: removed the approver parameter as it's not in the method signature
                BidPurchase.proposeCounterBid(storeId, productId, userId, -20.0));
        assertEquals("Counter offer must be a positive value.", ex.getMessage());
    }

    @Test
    void acceptCounterOffer_shouldApproveBid() {
        Bid bid = createAndAddBid(100.0);
        bid.proposeCounterOffer(120.0);
        BidPurchase.acceptCounterOffer(storeId, productId, userId);
        assertTrue(bid.isApproved());
        assertEquals(120.0, bid.price);
        assertFalse(bid.isCounterOffered());
        
        // Verify that payment and shipment services were called with updated price
        verify(paymentService).processPayment(eq(currency), eq(120.0), eq(cardNumber), eq(month), eq(year), eq(holder), eq(ccv));
        verify(shipmentService).ship(eq("John Doe"), eq("123 Main St"), eq("New York"), eq("USA"), eq("12345"));
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
        bid.proposeCounterOffer(120.0);
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

        Bid counterBid = new Bid("user2", 150.0, "John Doe, 123 Main St, New York, USA, 12345", "contact@example.com", approvers, currency, cardNumber, month, year, holder, ccv);
        counterBid.proposeCounterOffer(170.0);
        BidPurchase.getBids().get(bidKey).add(counterBid);
        assertEquals("Counter Offered: 170.0", BidPurchase.getBidStatus(storeId, productId, "user2"));

        Bid rejectedBid = new Bid("user3", 90.0, "John Doe, 123 Main St, New York, USA, 12345", "contact@example.com", approvers, currency, cardNumber, month, year, holder, ccv);
        rejectedBid.reject("owner1");
        BidPurchase.getBids().get(bidKey).add(rejectedBid);
        assertEquals("Rejected", BidPurchase.getBidStatus(storeId, productId, "user3"));
    }

    @Test
    void getBidStatus_noBid_shouldReturnNoBidFound() {
        String status = BidPurchase.getBidStatus("storeX", "productX", "nonexistentUser");
        assertEquals("No Bid Found", status);
    }

    @Test
    void purchase_validBid_shouldCreatePurchaseWithCorrectDetails() {
        BidPurchase bidPurchase = new BidPurchase();
        
        Purchase purchase = bidPurchase.purchase(
            userId, 
            storeId, 
            productId, 
            100.0, 
            "John Doe, 123 Main St, New York, USA, 12345", 
            "contact@example.com",
            currency,
            cardNumber,
            month,
            year,
            holder,
            ccv
        );

        assertNotNull(purchase);
        assertEquals(userId, purchase.getUserId());
        assertEquals(100.0, purchase.getTotalPrice());
        assertEquals("John Doe, 123 Main St, New York, USA, 12345", purchase.getShippingAddress());
        assertEquals("contact@example.com", purchase.getContactInfo());

        // Verify payment and shipment were processed
        verify(paymentService).processPayment(eq(currency), eq(100.0), eq(cardNumber), eq(month), eq(year), eq(holder), eq(ccv));
        verify(shipmentService).ship(eq("John Doe"), eq("123 Main St"), eq("New York"), eq("USA"), eq("12345"));
    }

    @Test
    void purchase_paymentFailure_shouldThrowException() {
        // Configure payment service to throw exception
        when(paymentService.processPayment(anyString(), anyDouble(), anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Payment failed: Insufficient funds"));

        BidPurchase bidPurchase = new BidPurchase();

        Exception ex = assertThrows(RuntimeException.class, () -> {
            bidPurchase.purchase(
                userId, 
                storeId, 
                productId, 
                100.0, 
                "John Doe, 123 Main St, New York, USA, 12345", 
                "contact@example.com",
                currency,
                cardNumber,
                month,
                year,
                holder,
                ccv
            );
        });

        assertTrue(ex.getMessage().contains("Payment failed"));
        // Verify payment was attempted
        verify(paymentService).processPayment(eq(currency), eq(100.0), eq(cardNumber), eq(month), eq(year), eq(holder), eq(ccv));
        // Verify shipment was not attempted after payment failure
        verify(shipmentService, never()).ship(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void purchase_shipmentFailure_shouldCancelPaymentAndThrowException() {
        // Configure shipment service to return null (failure)
        when(shipmentService.ship(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(null);

        BidPurchase bidPurchase = new BidPurchase();

        Exception ex = assertThrows(RuntimeException.class, () -> {
            bidPurchase.purchase(
                userId, 
                storeId, 
                productId, 
                100.0, 
                "John Doe, 123 Main St, New York, USA, 12345", 
                "contact@example.com",
                currency,
                cardNumber,
                month,
                year,
                holder,
                ccv
            );
        });

        assertTrue(ex.getMessage().contains("Shipment failed"));
        // Verify payment was attempted and then cancelled
        verify(paymentService).processPayment(eq(currency), eq(100.0), eq(cardNumber), eq(month), eq(year), eq(holder), eq(ccv));
        verify(paymentService).cancelPayment("payment-id-123");
        // Verify shipment was attempted
        verify(shipmentService).ship(eq("John Doe"), eq("123 Main St"), eq("New York"), eq("USA"), eq("12345"));
    }
}
