package support;

import market.application.UserService;
import market.application.StoreService;
import market.application.AuthService;
import market.application.External.PaymentService;
import market.application.External.ShipmentService;
import static org.mockito.Mockito.mock;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import market.domain.user.IUserRepository;
//import IStoreRepository
import market.domain.store.IStoreRepository;
;

public abstract class AcceptanceTestBase {

    protected Bridge bridge;
    protected UserService userService;
    protected StoreService storeService;
    protected AuthService authService;
    protected PaymentService paymentService;
    protected ShipmentService shipmentService;
    
    

    @BeforeEach
    void setup() {
      
        
        userService = new UserService(mock(IUserRepository.class));
        storeService = new StoreService(mock(IStoreRepository.class)); // Use the real implementation
        authService = mock(AuthService.class); // Use the real implementation
        paymentService = mock(PaymentService.class); // Mock external service
        shipmentService = mock(ShipmentService.class); // Mock external service
        
        // Initialize the bridge with the mocked services
        // This allows the bridge to interact with the mocked services during tests.
        bridge = new RealBridge(userService, storeService, authService, paymentService, shipmentService);
    }
}
