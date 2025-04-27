package market.domain;

import market.domain.store.Listing;
import market.domain.store.StoreProductManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import market.domain.purchase.PurchaseType;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class StoreProductManagerUnitTests {
    private StoreProductManager manager;

    @BeforeEach
    void setUp() {
        manager = new StoreProductManager("store123");
    }

    // Corrected createListing to match your Listing constructor
    private Listing createListing(String productId, String productName) {
        return new Listing(
                "store123",           // storeId
                productId,             // productId
                productName,           // productName
                "Test Description",    // productDescription
                10,                    // quantityAvailable
                PurchaseType.REGULAR,  // purchaseType
                100                    // price
        );
    }

    @Test
    void testAddListingSuccess() {
        Listing listing = createListing("p1", "ProductA");
        boolean added = manager.addListing(listing);

        assertTrue(added);
        assertEquals(listing, manager.getListingById(listing.getListingId()));
    }

    @Test
    void testAddDuplicateListingFails() {
        Listing listing = createListing("p2", "ProductB");
        manager.addListing(listing);

        boolean addedAgain = manager.addListing(listing);
        assertFalse(addedAgain);
    }

    @Test
    void testAddListingWithWrongStoreIdFails() {
        Listing wrongStoreListing = new Listing(
                "wrongStore", "p3", "ProductC", "Wrong description", 5, PurchaseType.REGULAR, 50
        );

        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            manager.addListing(wrongStoreListing);
        });
        assertTrue(e.getMessage().contains("storeId does not match"));
    }

    @Test
    void testRemoveListingSuccess() {
        Listing listing = createListing("p4", "ProductD");
        manager.addListing(listing);

        boolean removed = manager.removeListing(listing.getListingId());

        assertTrue(removed);
        assertNull(manager.getListingById(listing.getListingId()));
    }

    @Test
    void testRemoveListingNotExist() {
        boolean removed = manager.removeListing("nonExistentId");
        assertFalse(removed);
    }

    @Test
    void testGetListingsByProductName() {
        Listing l1 = createListing("p5", "Phone");
        Listing l2 = createListing("p6", "Phone");

        manager.addListing(l1);
        manager.addListing(l2);

        List<Listing> listings = manager.getListingsByProductName("Phone");

        assertEquals(2, listings.size());
        assertTrue(listings.contains(l1));
        assertTrue(listings.contains(l2));
    }

    @Test
    void testGetListingsByProductId() {
        Listing listing = createListing("p7", "Laptop");
        manager.addListing(listing);

        List<Listing> listings = manager.getListingsByProductId("p7");

        assertEquals(1, listings.size());
        assertEquals(listing, listings.get(0));
    }

    @Test
    void testGetAllListings() {
        Listing l1 = createListing("p8", "Tablet");
        Listing l2 = createListing("p9", "Camera");

        manager.addListing(l1);
        manager.addListing(l2);

        List<Listing> allListings = manager.getAllListings();

        assertEquals(2, allListings.size());
        assertTrue(allListings.contains(l1));
        assertTrue(allListings.contains(l2));
    }

    @Test
    void testPurchaseFromListingSuccess() throws Exception {
        Listing listing = createListing("p10", "Monitor");
        manager.addListing(listing);

        boolean purchased = manager.purchaseFromListing(listing.getListingId(), 2);

        assertTrue(purchased);
        assertEquals(8, manager.getListingById(listing.getListingId()).getQuantityAvailable()); // 10 - 2 = 8
    }

    @Test
    void testPurchaseFromNonExistentListingThrows() {
        Exception e = assertThrows(Exception.class, () -> {
            manager.purchaseFromListing("nonExistentId", 1);
        });
        assertTrue(e.getMessage().contains("does not exist"));
    }
}