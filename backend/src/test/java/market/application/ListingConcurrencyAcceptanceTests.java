// package market.application;

// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.concurrent.CountDownLatch;
// import java.util.concurrent.atomic.AtomicInteger;

// import org.junit.jupiter.api.AfterEach;
// import static org.junit.jupiter.api.Assertions.assertEquals;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.condition.EnabledIf;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.ApplicationContext;
// import org.springframework.test.annotation.Commit;
// import org.springframework.test.annotation.DirtiesContext;

// import market.domain.store.IListingRepository;
// import market.domain.store.Listing;
// import support.AcceptanceTestSpringBase;

// @EnabledIf("market.application.UserServiceTests#isMySQLAvailable")
// @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
// public class ListingConcurrencyAcceptanceTests extends AcceptanceTestSpringBase {

//     @Autowired
//     private ApplicationContext context;

//     @Autowired
//     private IListingRepository listingRepository;

//     @BeforeEach
//     void setUp() {
//         userService.register("owner", "pw");
//         userService.register("buyer1", "pw");
//         userService.register("buyer2", "pw");
//     }

//     @AfterEach
//     void tearDown() {
//         userService.deleteUser("owner");
//         userService.deleteUser("buyer1");
//         userService.deleteUser("buyer2");
//     }



//     @Commit
//     @Test
//     void concurrentPurchase_shouldRespectStockAndVersionControl1() throws Exception {
//         String storeId = storeService.createStore("ConcurrentStore", "owner").storeId();
//         String listingId = storeService.addNewListing("owner", storeId, "p1", "Gaming Mouse", "Electronics", "Precise mouse", 7, 99.99, "REGULAR");
//         List<String> l = new ArrayList<>();
        

//         int threadCount = 20;
//         CountDownLatch latch = new CountDownLatch(threadCount);
//         AtomicInteger successCount = new AtomicInteger(0);


//         for (int i = 0; i < threadCount; i++) {
//             Thread.sleep(5);
//             new Thread(() -> {
//                 try {
//                     Map<String, Map<String, Integer>> cart = new HashMap<>();
//                     cart.put(storeId, Map.of(listingId, 2));
//                     if (listingRepository.updateOrRestoreStock(cart, false)) {
//                         successCount.incrementAndGet();
//                     }
//                 } catch (Exception e) {
//                     l.add(e.getMessage());
//                 } finally {
//                     latch.countDown();
//                 }
//             }).start();
//         }

//         latch.await();

//         assertEquals(3, successCount.get(), "Only 3 successful purchases should occur due to stock limit");
//         Listing listing = listingRepository.getListingById(listingId);
//         assertEquals(1, listing.getQuantityAvailable(), "Stock should be fully consumed");
//     }

//       @Commit
//     @Test
//     void concurrentPurchase_notenoughquantityAllOfThemShouldFail() throws Exception {
//         String storeId = storeService.createStore("ConcurrentStore1", "owner").storeId();
//         String listingId = storeService.addNewListing("owner", storeId, "p1", "Gaming Mouse", "Electronics", " mouse", 1, 99.99, "REGULAR");
//         List<String> l = new ArrayList<>();
        

//         int threadCount = 20;
//         CountDownLatch latch = new CountDownLatch(threadCount);
//         AtomicInteger successCount = new AtomicInteger(0);


//         for (int i = 0; i < threadCount; i++) {
//             Thread.sleep(5);
//             new Thread(() -> {
//                 try {
//                     Map<String, Map<String, Integer>> cart = new HashMap<>();
//                     cart.put(storeId, Map.of(listingId, 2));
//                     if (listingRepository.updateOrRestoreStock(cart, false)) {
//                         successCount.incrementAndGet();
//                     }
//                 } catch (Exception e) {
//                     l.add(e.getMessage());
//                 } finally {
//                     latch.countDown();
//                 }
//             }).start();
//         }

//         latch.await();

//         assertEquals(0, successCount.get(), "Only 10 successful purchases should occur due to stock limit");
//         Listing listing = listingRepository.getListingById(listingId);
//         assertEquals(1, listing.getQuantityAvailable(), "Stock should be fully consumed");
//     }



    
// }
