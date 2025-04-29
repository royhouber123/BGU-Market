package market.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import market.domain.purchase.Bid;
import market.domain.purchase.BidKey;
import market.domain.purchase.BidPurchase;
import market.domain.store.IStoreRepository;
import market.infrastructure.StoreRepository;
import market.application.External.*;




class BidPurchaseTest {
    private IShipmentService shipmentService;
    private IPaymentService paymentService;
    private IStoreRepository storeRepository;

    
    @BeforeEach
    void setUp() {
        BidPurchase.getBids().clear(); 
        storeRepository = mock(IStoreRepository.class);
        when(storeRepository.updateStockForPurchasedItems(anyMap())).thenReturn(true);
        BidPurchase.setStoreRepository(storeRepository);
        paymentService = mock(IPaymentService.class);
        when(paymentService.processPayment(anyString())).thenReturn(true); 
        BidPurchase.setPaymentService(paymentService); 
        shipmentService = mock(IShipmentService.class);
        when(shipmentService.ship(anyString(), anyString(), anyDouble())).thenReturn("Shipping successful");
        BidPurchase.setShippingService(shipmentService);
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

    @Test
    void testApproveBidSuccess() {
        String storeId = "store1";
        String productId = "product1";
        String userId = "user1";
        String approverId = "owner1"; 
        Set<String> approvers = Set.of(approverId);
        Bid testBid = new Bid(userId, 100.0, "123 Test St", "test@example.com", approvers);
        BidKey key = new BidKey(storeId, productId);
        BidPurchase.getBids().put(key, new ArrayList<>(List.of(testBid)));
        assertDoesNotThrow(() -> {
            BidPurchase.approveBid(storeId, productId, userId, approverId);
        });
        Bid approvedBid = BidPurchase.getBids().get(key).get(0);
        assertTrue(approvedBid.isApproved());
    }

    @Test
    void testRejectBidSuccess() {
        String storeId = "store1";
        String productId = "product1";
        String userId = "user1";
        String approverId = "owner1";
        Set<String> approvers = Set.of(approverId, "manager1");
        Bid bid = new Bid(userId, 100.0, "123 Test St", "test@example.com", approvers);
        BidKey key = new BidKey(storeId, productId);
        BidPurchase.getBids().computeIfAbsent(key, k -> new ArrayList<>()).add(bid);
        assertDoesNotThrow(() -> BidPurchase.rejectBid(
            storeId,
            productId,
            userId,
            approverId
        ));
        Boolean isRejected= bid.isRejected();
        assertTrue(isRejected);
    }

    @Test
    void testProposeCounterBidSuccess() {
        String storeId = "store1";
        String productId = "product1";
        String userId = "user1";
        String approverId = "owner1";
        Set<String> approvers = Set.of(approverId, "manager1");
        Bid bid = new Bid(userId, 100.0, "123 Test St", "test@example.com", approvers);
        BidKey key = new BidKey(storeId, productId);
        BidPurchase.getBids().computeIfAbsent(key, k -> new ArrayList<>()).add(bid);
        double newCounterOffer = 120.0;
        assertDoesNotThrow(() -> BidPurchase.proposeCounterBid(
            storeId,
            productId,
            userId,
            newCounterOffer
        ));
        double counterOfferAmount = bid.getCounterOfferAmount();
        assertEquals(newCounterOffer, counterOfferAmount);
    }

    @Test
    void testAcceptCounterOfferSuccess() {
        String storeId = "store1";
        String productId = "product1";
        String userId = "user1";
        String approverId = "owner1";
        Set<String> approvers = Set.of(approverId, "manager1");
        Bid bid = new Bid(userId, 100.0, "123 Test St", "test@example.com", approvers);
        bid.setCounterOfferAmount(120.0);
        BidKey key = new BidKey(storeId, productId);
        BidPurchase.getBids().computeIfAbsent(key, k -> new ArrayList<>()).add(bid);
            assertDoesNotThrow(() -> BidPurchase.acceptCounterOffer(
            storeId,
            productId,
            userId
        ));
        assertEquals(120.0, bid.getPrice());     
        assertTrue(bid.isApproved());             
        assertFalse(bid.isCounterOffered());      
    }

    @Test
    void testDeclineCounterOfferSuccess() {
        String storeId = "store1";
        String productId = "product1";
        String userId = "user1";
        String approverId = "owner1";
        Set<String> approvers = Set.of(approverId, "manager1");
        Bid bid = new Bid(userId, 100.0, "123 Test St", "test@example.com", approvers);
        bid.setCounterOfferAmount(120.0);
        BidKey key = new BidKey(storeId, productId);
        BidPurchase.getBids().computeIfAbsent(key, k -> new ArrayList<>()).add(bid);
        assertDoesNotThrow(() -> BidPurchase.declineCounterOffer(
            storeId,
            productId,
            userId
        ));
        assertTrue(bid.isRejected());
    }

    @Test
    void testGetBidStatusSuccess() {
        String storeId = "store1";
        String productId = "product1";
        String userId = "user1";
        BidKey key = new BidKey(storeId, productId);
        Set<String> approversPending = Set.of("owner1", "manager1");
        Bid pendingBid = new Bid(userId, 100.0, "123 Test St", "test@example.com", approversPending);
        BidPurchase.getBids().computeIfAbsent(key, k -> new ArrayList<>()).add(pendingBid);
        String pendingStatus = BidPurchase.getBidStatus(storeId, productId, userId);
        assertEquals("Pending Approval", pendingStatus);
        pendingBid.approve("owner1");
        pendingBid.approve("manager1");
        String approvedStatus = BidPurchase.getBidStatus(storeId, productId, userId);
        assertEquals("Approved", approvedStatus);
        Bid counterBid = new Bid("user2", 150.0, "456 Another St", "another@example.com", approversPending);
        BidPurchase.getBids().computeIfAbsent(key, k -> new ArrayList<>()).add(counterBid);
        counterBid.proposeCounterOffer(170.0);
        String counterStatus = BidPurchase.getBidStatus(storeId, productId, "user2");
        assertEquals("Counter Offered: 170.0", counterStatus);
        Bid rejectedBid = new Bid("user3", 90.0, "789 Other St", "other@example.com", approversPending);
        BidPurchase.getBids().computeIfAbsent(key, k -> new ArrayList<>()).add(rejectedBid);
        rejectedBid.reject("owner1");
        String rejectedStatus = BidPurchase.getBidStatus(storeId, productId, "user3");
        assertEquals("Rejected", rejectedStatus);
    }


    // @Test
    // void testPurchaseCreation() {
    //     String userId = "user1";
    //     String storeId = "store1";
    //     String productId = "product1";
    //     double price = 100.0;
    //     String shippingAddress = "123 Test St";
    //     String contactInfo = "test@example.com";

    //     BidPurchase bidPurchase = new BidPurchase();
    //     assertDoesNotThrow(() -> bidPurchase.purchase(userId, storeId, productId, price, shippingAddress, contactInfo));
    // }
}



