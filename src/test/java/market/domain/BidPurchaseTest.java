package market.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Set;

import market.domain.purchase.BidKey;
import market.domain.purchase.BidPurchase;
import market.domain.store.IStoreRepository;
import market.infrastructure.StoreRepository;
import market.application.External.PaymentService;
import market.application.External.ShipmentService;




class BidPurchaseTest {
    private ShipmentService shipmentService;
    private PaymentService paymentService;
    private IStoreRepository storeRepository;

    @BeforeEach
    void setUp() {
        shipmentService = new ShipmentService();
        paymentService = new PaymentService();
        storeRepository = new StoreRepository();
    }

    @Test
    void testSubmitBidSuccess() {
        String storeId = "store1";
        String productId = "product1";
        String userId = "user1";
        double price = 100.0;
        String shippingAddress = "123 Test St";
        String contactInfo = "test@example.com";
        Set<String> approvers = Set.of("owner1", "manager1");
        assertDoesNotThrow(() -> BidPurchase.submitBid(
            storeRepository,
            storeId,
            productId,
            userId,
            price,
            shippingAddress,
            contactInfo,
            approvers,
            shipmentService,
            paymentService
        ));
        BidKey key = new BidKey(storeId, productId);
        assertTrue(BidPurchase.getBids().containsKey(key));
    }

    ////////////continue from here
    @Test
    void testApproveBidSuccess() {
        String storeId = "store1";
        String productId = "product1";
        String userId = "user1";
        String approverId = "owner1";

        // Mock behavior for stock update
        when(mockStoreRepository.updateStockForPurchasedItems(anyMap())).thenReturn(true);

        assertDoesNotThrow(() -> BidPurchase.approveBid(storeId, productId, userId, approverId));
    }

    @Test
    void testRejectBidSuccess() {
        String storeId = "store1";
        String productId = "product1";
        String userId = "user1";
        String approverId = "owner1";

        assertDoesNotThrow(() -> BidPurchase.rejectBid(storeId, productId, userId, approverId));
    }

    @Test
    void testProposeCounterBidSuccess() {
        String storeId = "store1";
        String productId = "product1";
        String userId = "user1";
        double newAmount = 120.0;

        assertDoesNotThrow(() -> BidPurchase.proposeCounterBid(storeId, productId, userId, newAmount));
    }

    @Test
    void testAcceptCounterOfferSuccess() {
        String storeId = "store1";
        String productId = "product1";
        String userId = "user1";

        // Mock behavior for stock update
        when(mockStoreRepository.updateStockForPurchasedItems(anyMap())).thenReturn(true);

        assertDoesNotThrow(() -> BidPurchase.acceptCounterOffer(storeId, productId, userId));
    }

    @Test
    void testDeclineCounterOfferSuccess() {
        String storeId = "store1";
        String productId = "product1";
        String userId = "user1";

        assertDoesNotThrow(() -> BidPurchase.declineCounterOffer(storeId, productId, userId));
    }

    @Test
    void testGetBidStatusSuccess() {
        String storeId = "store1";
        String productId = "product1";
        String userId = "user1";

        String status = BidPurchase.getBidStatus(storeId, productId, userId);
        assertNotNull(status);
    }

    @Test
    void testPurchaseCreation() {
        String userId = "user1";
        String storeId = "store1";
        String productId = "product1";
        double price = 100.0;
        String shippingAddress = "123 Test St";
        String contactInfo = "test@example.com";

        BidPurchase bidPurchase = new BidPurchase();
        assertDoesNotThrow(() -> bidPurchase.purchase(userId, storeId, productId, price, shippingAddress, contactInfo));
    }

    }

    @Test
    void testProposeCounterBidSuccess() {
        // TODO: implement
    }

    @Test
    void testAcceptCounterOfferSuccess() {
        // TODO: implement
    }

    @Test
    void testDeclineCounterOfferSuccess() {
        // TODO: implement
    }

    @Test
    void testGetBidStatusSuccess() {
        // TODO: implement
    }

    @Test
    void testPurchaseCreation() {
        // TODO: implement
    }
}


