package market.application;

import market.domain.store.Listing;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Commit;

import support.AcceptanceTestSpringBase;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIf("market.application.UserServiceTests#isMySQLAvailable")
public class ListingConcurrencyAcceptanceTests extends AcceptanceTestSpringBase {

    @BeforeEach
    void setUp() {
        userService.register("owner", "pw");
        userService.register("buyer1", "pw");
        userService.register("buyer2", "pw");
    }

    @AfterEach
    void tearDown() {
        userService.deleteUser("owner");
        userService.deleteUser("buyer1");
        userService.deleteUser("buyer2");
    }

    @Commit
    @Test
    void concurrentPurchase_shouldRespectStockAndVersionControl() throws Exception {
        String storeId = storeService.createStore("ConcurrentStore", "owner").storeId();
        String listingId = storeService.addNewListing("owner", storeId, "p1", "Gaming Mouse", "Electronics", "Precise mouse", 10, 99.99, "REGULAR");

        int threadCount = 20;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    Map<String, Map<String, Integer>> cart = new HashMap<>();
                    cart.put(storeId, Map.of(listingId, 1));
                    if (listingRepository.updateOrRestoreStock(cart, false)) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        assertEquals(10, successCount.get(), "Only 10 successful purchases should occur due to stock limit");
        Listing listing = listingRepository.getListingById(listingId);
        assertEquals(0, listing.getQuantityAvailable(), "Stock should be fully consumed");
    }
}
