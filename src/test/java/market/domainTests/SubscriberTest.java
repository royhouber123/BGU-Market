package market.domainTests;

import market.domain.user.Subscriber;
import market.domain.user.roles.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SubscriberTest {

    private Subscriber subscriber;

    @BeforeEach
    public void setUp() {
        subscriber = new Subscriber("user123");
    }

    @Test
    public void testSetAndGetShippingAddress() {
        subscriber.setShippingAddress("123 Main St");
        assertEquals("123 Main St", subscriber.getShippingAddress());
        subscriber.setShippingAddress("");
    }

    @Test
    public void testSetAndGetStoreRole() throws Exception {
        subscriber.setStoreRole("store1", "Owner");
        Role role = subscriber.getStoreRole("store1", "Owner");
        assertNotNull(role);
        assertEquals("Owner", role.getRoleName());
        // verify isOwner and hasStoreRole
        assertTrue(subscriber.isOwner("store1"));
        subscriber.removeStoreRole("store1", "Owner");
    }

    @Test
    public void testIsManager() throws Exception {
        subscriber.setStoreRole("storeX", "Manager");
        assertTrue(subscriber.isManager("storeX"));
        subscriber.removeStoreRole("storeX", "Manager");
    }

    @Test
    public void testIsFounder() throws Exception {
        subscriber.setStoreRole("storeY", "Founder");
        assertTrue(subscriber.isFounder("storeY"));
        subscriber.removeStoreRole("storeX", "Founder");
    }

    @Test
    public void testIsOwner() throws Exception {
        subscriber.setStoreRole("storeY", "Owner");
        assertTrue(subscriber.isOwner("storeY"));
        subscriber.removeStoreRole("storeY", "Owner");
    }

    @Test
    public void testSetInvalidRoleThrowsException() {
        Exception exception = assertThrows(
            Exception.class,
            () -> subscriber.setStoreRole("store1", "InvalidRole")
        );
        assertTrue(exception.getMessage().contains("does not exist"));
    }

    @Test
    public void testGetStoreRoleThrowsExceptionWhenRoleNotSet() {
        Exception exception = assertThrows(
            Exception.class,
            () -> subscriber.getStoreRole("store1", "Owner")
        );
        assertTrue(exception.getMessage().contains("User 'user123' is not 'Owner' at store 'store1'"));
    }
}
