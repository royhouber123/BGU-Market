package tests;

import org.junit.jupiter.api.Test;

import support.AcceptanceTestBase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ParallelTests extends AcceptanceTestBase {

    //talk with the team if we want to do those tests?



    @Test
    void concurrent_guest_registrations() throws InterruptedException {
        int threadCount = 2;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        executor.execute(() -> {
            bridge.register("user1", "pass", "u1@email.com", "City");
            latch.countDown();
        });
        executor.execute(() -> {
            bridge.register("user2", "pass", "u2@email.com", "City");
            latch.countDown();
        });

        latch.await(5, TimeUnit.SECONDS);

        assertTrue(userService.userExists("user1"));
        assertTrue(userService.userExists("user2"));
    }

    @Test
    void concurrent_purchases_on_same_product() throws InterruptedException {
        storeService.addStore("RaceStore");
        storeService.addProductToStore("RaceStore", "GPU", 1000.0, 1); // only 1 item

        bridge.register("buyer1", "pass", "b1@email.com", "City");
        bridge.register("buyer2", "pass", "b2@email.com", "City");

        CountDownLatch latch = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.execute(() -> {
            bridge.login("buyer1", "pass");
            bridge.addProductToCart("RaceStore", "GPU", 1);
            String result = bridge.purchaseCart("City", "4111111111111111", "12/26", "123");
            System.out.println("Buyer1: " + result);
            latch.countDown();
        });

        executor.execute(() -> {
            bridge.login("buyer2", "pass");
            bridge.addProductToCart("RaceStore", "GPU", 1);
            String result = bridge.purchaseCart("City", "4111111111111111", "12/26", "123");
            System.out.println("Buyer2: " + result);
            latch.countDown();
        });

        latch.await(5, TimeUnit.SECONDS);

        int available = storeService.getProductStock("RaceStore", "GPU");
        assertEquals(0, available);
    }

    @Test
    void concurrent_appointment_of_same_owner() throws InterruptedException {
        bridge.register("mainowner", "pass", "main@email.com", "City");
        bridge.login("mainowner", "pass");
        bridge.openStore("ConflictStore", "GENERAL");

        bridge.register("candidate", "pass", "candidate@email.com", "City");
        bridge.register("otherowner", "pass", "other@email.com", "City");
        bridge.login("otherowner", "pass");

        CountDownLatch latch = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.execute(() -> {
            String result1 = bridge.appointOwner("ConflictStore", "candidate");
            System.out.println("MainOwner appoints: " + result1);
            latch.countDown();
        });

        executor.execute(() -> {
            String result2 = bridge.appointOwner("ConflictStore", "candidate");
            System.out.println("OtherOwner appoints: " + result2);
            latch.countDown();
        });

        latch.await(5, TimeUnit.SECONDS);

        int ownerCount = storeService.countOwners("ConflictStore");
        assertEquals(1, ownerCount); // should not have been appointed twice
    }
}
