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

    AuctionPurchase auctionPurchase;
    IStoreRepository storeRepository;   
    @BeforeEach
    void setUp() {
        // No specific setup needed for now
        auctionPurchase = new AuctionPurchase();
        storeRepository = new StoreRepository(); // Replace with a mock or concrete implementation
    }

    @Test
    void testOpenAuctionSuccess() {
        Map<AuctionKey, List<Offer>> offers = auctionPurchase.getOffers();
        String storeId = "store1";
        String productId = "prod1";
        double startingPrice = 100;
        long endTimeMillis = System.currentTimeMillis() + 1 * 60 * 1000; // one minute from now
        ShipmentService shipmentService = new ShipmentService();
        PaymentService paymentService = new PaymentService();
        assertDoesNotThrow(() -> {
            AuctionPurchase.openAuction(this.storeRepository, storeId, productId, startingPrice, endTimeMillis, shipmentService, paymentService);
        });
    }

    @Test
    void testSubmitOfferSuccess() {
        // TODO: implement
    }

    @Test
    void testGetAuctionStatusSuccess() {
        // TODO: implement
    }

    @Test
    void testCloseAuctionSuccess() {
        // TODO: implement
    }

    @Test
    void testPurchaseCreation() {
        // TODO: implement
    }
}
