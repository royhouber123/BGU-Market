package support;

import market.application.UserService;
import market.application.StoreService;
import market.application.AuthenticationService;
import market.application.PaymentService;
import market.application.ShipmentService;
import org.junit.jupiter.api.BeforeEach;

import static org.mockito.Mockito.*;

public abstract class AcceptanceTestBase {

    protected Bridge bridge;
    protected UserService userService;
    protected StoreService storeService;
    protected AuthenticationService authService;
    protected PaymentService paymentService;
    protected ShipmentService shipmentService;

    @BeforeEach
    void setup() {
        userService = new InMemoryUserService();
        storeService = new InMemoryStoreService();

        authService = mock(AuthenticationService.class);
        paymentService = mock(PaymentService.class);
        shipmentService = mock(ShipmentService.class);

        bridge = new RealBridge(userService, storeService, authService, paymentService, shipmentService);
    }
}
