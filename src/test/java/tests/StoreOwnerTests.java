package tests;

import org.junit.jupiter.api.Test;

import support.AcceptanceTestBase;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

public class StoreOwnerTests extends AcceptanceTestBase {

    @Test
    void subscriber_opens_new_store_successfully() {
        bridge.register("owner1", "pass", "owner1@email.com", "City");
        bridge.login("owner1", "pass");
        String result = bridge.openStore("GadgetStore", "ELECTRONICS");
        assertEquals("Store created", result);
    }

    @Test
    void owner_adds_new_product_to_store() {
        bridge.openStore("GadgetStore", "ELECTRONICS");
        String result = bridge.addProductToStore("GadgetStore", "Tablet", "Tech", 899.99, 10);
        assertEquals("Product added", result);
    }

    @Test
    void owner_edits_existing_product_details() {
        bridge.addProductToStore("GadgetStore", "Tablet", "Tech", 899.99, 10);
        String result = bridge.editProduct("GadgetStore", "Tablet", "price", "799.99");
        assertEquals("Product updated", result);
    }

    @Test
    void owner_removes_product_from_store() {
        bridge.addProductToStore("GadgetStore", "Tablet", "Tech", 899.99, 10);
        String result = bridge.removeProduct("GadgetStore", "Tablet");
        assertEquals("Product removed", result);
    }

    @Test
    void owner_views_store_purchase_history() {
        String history = bridge.viewStorePurchaseHistory("GadgetStore");
        assertTrue(history.contains("Product"));
    }

    @Test
    void owner_sets_discount_policy() {
        String result = bridge.editDiscountPolicy("GadgetStore", "Tablet", "10% off all", "all");
        assertEquals("Discount policy updated", result);
    }

    @Test
    void owner_sets_purchase_policy() {
        String result = bridge.editPurchasePolicy("GadgetStore", "Tablet", "age > 18");
        assertEquals("Purchase policy updated", result);
    }

    @Test
    void owner_appoints_another_owner() {
        bridge.register("owner2", "pass", "owner2@email.com", "City");
        String result = bridge.appointOwner("GadgetStore", "owner2");
        assertEquals("Appointment request sent", result);
    }

    @Test
    void owner_removes_owner_successfully() {
        bridge.appointOwner("GadgetStore", "owner2");
        String result = bridge.removeOwner("GadgetStore", "owner2");
        assertEquals("Owner removed", result);
    }

    @Test
    void owner_responds_to_user_message() {
        String result = bridge.respondToMessage("GadgetStore", 1, "Thanks for your feedback");
        assertEquals("Response submitted", result);
    }
}
