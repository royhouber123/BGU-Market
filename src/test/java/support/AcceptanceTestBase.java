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
import market.domain.purchase.Purchase;
//import IStoreRepository
import market.domain.store.IStoreRepository;
// import user repository
import market.infrastracture.UserRepository;
import market.infrastracture.StoreRepository;
//import subscriber
import market.domain.user.Subscriber;
//import purchased service
import market.infrastracture.InMemoryPurchaseRepository;

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
        userService = new UserService(userRepository);
        authService = new AuthService(userRepository);
        StoreRepository storerepo = new StoreRepository(); // Use the real implementation
        storeService = new StoreService(storerepo); // Use the real implementation 
        UserRepository userRepository2_for_purchese = new UserRepository();
        purchaseService = new PurchaseService(storerepo, new InMemoryPurchaseRepository(), userRepository2_for_purchese);



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


   
  
}
