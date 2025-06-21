package market.application;

import market.application.PurchaseService;
import market.application.StoreService;
import market.application.UserService;
import market.domain.purchase.Purchase;
import market.domain.store.Listing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import support.AcceptanceTestSpringBase;

import java.sql.DriverManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//@EnabledIf("market.application.UserServiceTests#isMySQLAvailable")
public class PurchaseServiceTests extends AcceptanceTestSpringBase {

    String user, approver, seller, bidder;

    @BeforeEach
    void setUp() throws Exception {
        user = "user";
        approver = "approver";
        seller = "seller";
        bidder = "bidder";

        userService.register(user, "pw");
        userService.register(approver, "pw");
        userService.register(seller, "pw");
        userService.register(bidder, "pw");
    }


    @Test
    void testBidApprovedPurchaseIsSavedToDatabase() throws Exception {
        String storeId = storeService.createStore("BookStore", approver).storeId();
        String productId = storeService.addNewListing(approver, storeId,"1", "Book", "Books", "Interesting book", 5, 50.0, "BID");

        purchaseService.submitBid(storeId, productId, user, 40.0, "TLV", "user@a.com");
        purchaseService.approveBid(storeId, productId, user, approver);

        List<Purchase> purchases = purchaseService.getPurchasesByUser(user);
        assertEquals(1, purchases.size());
        assertEquals(user, purchases.get(0).getUserId());
    }

    @Test
    void testRejectedBidNotSavedToDatabase() throws Exception {
        String storeId = storeService.createStore("RejectStore", approver).storeId();
        String productId = storeService.addNewListing(approver, storeId, "2", "Item", "Misc", "Just an item", 3, 30.0, "BID");

        purchaseService.submitBid(storeId, productId, user, 25.0, "TLV", "u@a.com");
        purchaseService.rejectBid(storeId, productId, user, approver);

        List<Purchase> purchases = purchaseService.getPurchasesByUser(user);
        assertEquals(0, purchases.size());
    }

    @Test
    void testCounterOfferAcceptedSavedToDatabase() throws Exception {
        String storeId = storeService.createStore("CounterStore", approver).storeId();
        String productId = storeService.addNewListing(approver, storeId, "3", "Painting", "Art", "Oil painting", 2, 200.0, "BID");

        purchaseService.submitBid(storeId, productId, user, 150.0, "TLV", "u@a.com");
        purchaseService.proposeCounterBid(storeId, productId, user, approver, 180.0);
        purchaseService.acceptCounterOffer(storeId, productId, user);

        List<Purchase> purchases = purchaseService.getPurchasesByUser(user);
        assertEquals(1, purchases.size());
    }

    @Test
    void testAuctionWinnerPurchaseIsSavedToDatabase() throws Exception {
        String storeId = storeService.createStore("AuctionHouse", seller).storeId();
        long endTime = System.currentTimeMillis() + 2000;
        purchaseService.openAuction(seller, storeId, "2", "Art", "Decor", "Rare sculpture", 100, endTime);

        List<Listing> listings = storeService.getListingRepository().getListingsByStoreId(storeId);
        String actualProductId = listings.get(0).getProductId();
        purchaseService.submitOffer(storeId, actualProductId, bidder, 150, "Herzliya", "bidder@a.com");

        Thread.sleep(2500); // Wait for auction to end

        List<Purchase> purchases = purchaseService.getPurchasesByUser(bidder);
        assertEquals(1, purchases.size());
        assertEquals(bidder, purchases.get(0).getUserId());
    }

    // static boolean isMySQLAvailable() {
    //     try {
    //         DriverManager.getConnection("jdbc:mysql://localhost:3306/?useSSL=false", "bgu", "changeme").close();
    //         return true;
    //     } catch (Exception e) {
    //         System.out.println("⚠️  MySQL not available – skipping UserServiceTests");
    //         return false;
    //     }
    // }
} 
