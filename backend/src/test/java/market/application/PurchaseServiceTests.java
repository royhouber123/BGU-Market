package market.application;

import market.application.PurchaseService;
import market.application.StoreService;
import market.application.UserService;
import market.domain.purchase.AuctionEntity;
import market.domain.purchase.AuctionPurchase;
import market.domain.purchase.Bid;
import market.domain.purchase.BidEntity;
import market.domain.purchase.Offer;
import market.domain.purchase.Purchase;
import market.domain.store.Listing;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import support.AcceptanceTestSpringBase;

import java.sql.DriverManager;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;

@EnabledIf("market.application.UserServiceTests#isMySQLAvailable")
@Transactional
public class PurchaseServiceTests extends AcceptanceTestSpringBase {


    @BeforeEach
    void setUp() {
        String user = "user";
        String approver = "approver";
        String seller = "seller";
        String bidder = "bidder";

        userService.register(user, "pw");
        userService.register(approver, "pw");
        userService.register(seller, "pw");
        userService.register(bidder, "pw");
    }

    @AfterEach
    void tearDown() {
        userService.deleteUser("user");
        userService.deleteUser("approver");
        userService.deleteUser("seller");
        userService.deleteUser("bidder");
    }


    @Commit
    @Test
    void testBidApprovedPurchaseIsSavedToDatabase() throws Exception {
        String storeId = storeService.createStore("BookStore", "approver").storeId();
        String productId = storeService.addNewListing("approver", storeId,"1", "Book", "Books", "Interesting book", 5, 50.0, "BID");

        purchaseService.submitBid(storeId, productId, "user", 40.0, "TLV", "user@a.com");
        
        BidEntity bidEntityBefore = bidRepository.findByStoreIdAndProductId(storeId, productId)
            .orElse(null);
        assertNotNull(bidEntityBefore, "BidEntity should exist in DB after submitBid");

        Bid bidBefore = bidEntityBefore.getBids().stream()
            .filter(b -> b.getUserId().equals("user"))
            .findFirst()
            .orElse(null);
        assertNotNull(bidBefore, "Bid should exist in BidEntity for user after submitBid");
        assertFalse(bidBefore.isApproved(), "Bid should NOT be approved before approveBid");
        
        purchaseService.approveBid(storeId, productId, "user", "approver");

        List<Purchase> purchases = purchaseService.getPurchasesByUser("user");
        assertNotNull(purchases);
        assertEquals("user", purchases.get(0).getUserId());

        BidEntity bidEntity = bidRepository.findByStoreIdAndProductId(storeId, productId)
                .orElse(null);
        assertNotNull(bidEntity, "BidEntity should exist in DB");
    
        Bid bid = bidEntity.getBids().stream()
            .filter(b -> b.getUserId().equals("user"))
            .findFirst()
            .orElse(null);
        assertNotNull(bid, "Bid should exist in BidEntity for user");
    
        assertTrue(bid.isApproved(), "Bid should be approved");
        assertFalse(bid.isRejected(), "Bid should not be rejected");

        bidRepository.deleteByStoreIdAndProductId(storeId, productId);
    }

    @Commit
    @Test
    void testRejectedBidSavedToDatabase() throws Exception {
        String storeId = storeService.createStore("RejectStore", "approver").storeId();
        String productId = storeService.addNewListing("approver", storeId, "2", "Item", "Misc", "Just an item", 3, 30.0, "BID");

        purchaseService.submitBid(storeId, productId, "user", 25.0, "TLV", "u@a.com");
        purchaseService.rejectBid(storeId, productId, "user", "approver");

        BidEntity bidEntity = bidRepository.findByStoreIdAndProductId(storeId, productId)
            .orElse(null);
        assertNotNull(bidEntity, "BidEntity should exist in DB after bid submission and rejection");

        Bid bid = bidEntity.getBids().stream()
            .filter(b -> b.getUserId().equals("user"))
            .findFirst()
            .orElse(null);
        assertNotNull(bid, "Bid should exist in BidEntity for user after rejection");
        assertTrue(bid.isRejected(), "Bid should be marked as rejected");
        assertFalse(bid.isApproved(), "Bid should NOT be approved after rejection");

        bidRepository.deleteByStoreIdAndProductId(storeId, productId);
    }

    @Commit
    @Test
    void testCounterOfferAcceptedSavedToDatabase() throws Exception {
        String storeId = storeService.createStore("CounterStore", "approver").storeId();
        String productId = storeService.addNewListing("approver", storeId, "3", "Painting", "Art", "Oil painting", 2, 200.0, "BID");

        purchaseService.submitBid(storeId, productId, "user", 150.0, "TLV", "u@a.com");
        purchaseService.proposeCounterBid(storeId, productId, "user", "approver", 180.0);
        purchaseService.acceptCounterOffer(storeId, productId, "user");
        
        List<Purchase> purchases = purchaseService.getPurchasesByUser("user");
        assertNotNull(purchases);

        BidEntity bidEntity = bidRepository.findByStoreIdAndProductId(storeId, productId)
            .orElse(null);
        assertNotNull(bidEntity, "BidEntity should exist in DB after counter-offer accepted");
    
        Bid bid = bidEntity.getBids().stream()
            .filter(b -> b.getUserId().equals("user"))
            .findFirst()
            .orElse(null);
        assertNotNull(bid, "Bid should exist in BidEntity for user after counter-offer accepted");
        assertTrue(bid.isApproved(), "Bid should be approved after counter-offer accepted");
        assertFalse(bid.isRejected(), "Bid should not be rejected after counter-offer accepted");
        assertEquals(180.0, bid.getPrice(), 0.01, "Bid price should be updated to counter-offer amount");
        assertFalse(bid.isCounterOffered(), "Bid should not be marked as counter-offered after acceptance");
        bidRepository.deleteByStoreIdAndProductId(storeId, productId);
    }

    // @Commit
    // @Test
    // void testAuctionWinnerPurchaseIsSavedToDatabase() throws Exception {
    //     String storeId = storeService.createStore("AuctionHouse", "seller").storeId();

    //     System.out.println("nsjdnjsjdv");
    //     long endTime = System.currentTimeMillis() + 3000; 
    //     purchaseService.openAuction("seller", storeId, "2", "Art", "Decor", "Rare sculpture", 100, endTime);

    //     List<Listing> listings = listingRepository.getListingsByStoreId(storeId);
    //     String actualListingId = null;
    //     for (Listing listing : listings) {
    //         if (listing.getPurchaseType().toString().equals("AUCTION")) {
    //             actualListingId=listing.getListingId();
    //             break;
    //         }
    //     }
    //     assertNotNull(actualListingId, "Auction listing should be created in DB");


    //     purchaseService.submitOffer(storeId, actualListingId, "bidder", 150, "Herzliya", "bidder@a.com");

    //     Thread.sleep(10000); // Wait for auction to end
    //     //AuctionPurchase.closeAuction(storeId, actualListingId, shipmentService, paymentService);

    //     List<Purchase> purchases = purchaseService.getPurchasesByUser("bidder");
    //     assertEquals(1, purchases.size());
    //     assertEquals("bidder", purchases.get(0).getUserId());
    
    //     AuctionEntity auctionEntity = auctionRepository.findByStoreIdAndProductId(storeId, actualListingId)
    //         .orElse(null);
    //     assertNotNull(auctionEntity, "AuctionEntity should exist in DB after auction ends");

    //     Offer winnerOffer = auctionEntity.getOffers().stream()
    //         .filter(o -> o.getUserId().equals("bidder"))
    //         .findFirst()
    //         .orElse(null);
    //     assertNotNull(winnerOffer, "Winner's offer should exist in AuctionEntity");
    //     assertEquals("bidder", winnerOffer.getUserId(), "Winner should be the bidder");
    //     assertEquals(150.0, winnerOffer.getPrice(), 0.01, "Winner's offer price should match submitted offer");
    
    // }

    static boolean isMySQLAvailable() {
        try {
            DriverManager.getConnection("jdbc:mysql://localhost:3306/?useSSL=false", "bgu", "changeme").close();
            return true;
        } catch (Exception e) {
            System.out.println("⚠️  MySQL not available – skipping UserServiceTests");
            return false;
        }
    }
} 
