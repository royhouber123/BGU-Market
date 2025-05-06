package market.domain;

import market.domain.user.Subscriber;
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
}
