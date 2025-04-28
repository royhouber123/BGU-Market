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
// import user repository
import market.infrastracture.UserRepository;
import market.infrastracture.StoreRepository;
//import subscriber
import market.domain.user.Subscriber;

public abstract class AcceptanceTestBase {

    
    protected UserService userService;
    protected StoreService storeService;
    protected AuthService authService;
    protected PaymentService paymentService;
    protected ShipmentService shipmentService;
    
    @BeforeEach
    void setup() {
      
        IUserRepository userRepository = new UserRepository();
        userService = new UserService(userRepository);
        authService = new AuthService(userRepository);
        IStoreRepository storerepo = new StoreRepository(); // Use the real implementation

        storeService = new StoreService(storerepo,userRepository); // Use the real implementation
        paymentService = mock(PaymentService.class); // Mock external service
        shipmentService = mock(ShipmentService.class); // Mock external service
        
        // Initialize the bridge with the mocked services
        // This allows the bridge to interact with the mocked services during tests.
        
    }
    /* 1️⃣  register a user and log in – returns the tokens */
    protected AuthService.AuthTokens registarAndLogin(String userName) throws Exception {
        userService.register(userName, "pw");
        return authService.login(userName, "pw");
    }


    /* 2️⃣  create an empty store – returns the new store-ID */
    protected int createStore(String userName, String storeName) throws Exception {
        AuthService.AuthTokens auth = registarAndLogin("founder");
        this.storeService.createStore(storeName, 1);
        return storeService.getStore(storeName).getStoreID();
    }
  
}
