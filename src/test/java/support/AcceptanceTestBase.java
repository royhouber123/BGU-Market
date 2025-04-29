package support;

import market.application.UserService;
import market.application.StoreService;
import market.application.AuthService;
import market.application.PurchaseService;
import market.application.External.PaymentService;
import market.application.External.ShipmentService;
import static org.mockito.Mockito.mock;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import market.domain.user.IUserRepository;
import market.infrastructure.UserRepository; // Ensure this is the correct package path
import market.infrastructure.PurchaseRepository;
import market.infrastructure.StoreRepository; // Ensure this is the correct package path
import market.domain.purchase.IPurchaseRepository;
import market.domain.store.IStoreRepository;
import market.domain.user.Subscriber;
import market.*;

public abstract class AcceptanceTestBase {

    
    protected UserService userService;
    protected StoreService storeService;
    protected AuthService authService;
    protected PaymentService paymentService;
    protected ShipmentService shipmentService;
    protected PurchaseService purchaseService;
    
    @BeforeEach
    void setup() {
      
        IUserRepository userRepository = new UserRepository();
        
        authService = new AuthService(userRepository);
        userService = new UserService(userRepository,authService);
        IStoreRepository storerepo = new StoreRepository(); // Use the real implementation

        storeService = new StoreService(storerepo,userRepository); // Use the real implementation
        paymentService = mock(PaymentService.class); // Mock external service
        shipmentService = mock(ShipmentService.class); // Mock external service
        IPurchaseRepository prep = new PurchaseRepository();

        purchaseService = new PurchaseService(storerepo, prep , userRepository,paymentService,shipmentService);
        // Initialize the bridge with the mocked services
        // This allows the bridge to interact with the mocked services during tests.
        
    }
    /* 1️⃣  register a user and log in – returns the tokens */
    protected AuthService.AuthTokens registarAndLogin(String userName) throws Exception {
        userService.register(userName, "pw");
        return authService.login(userName, "pw");
    }


   
  
}
