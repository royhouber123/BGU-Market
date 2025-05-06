package support;

import org.junit.jupiter.api.BeforeEach;
import static org.mockito.Mockito.mock;

import market.application.AuthService;
import market.application.External.IPaymentService;
import market.application.External.IShipmentService;
import market.application.PurchaseService;
import market.application.StoreService;
import market.application.UserService;
import market.domain.purchase.IPurchaseRepository;
import market.domain.store.IListingRepository;
import market.domain.store.IStoreRepository; // Ensure this is the correct package path
import market.domain.user.IUserRepository;
import market.infrastructure.ListingRepository;
import market.infrastructure.PurchaseRepository; // Ensure this is the correct package path
import market.infrastructure.StoreRepository;
import market.infrastructure.UserRepository;


public abstract class AcceptanceTestBase {

    
    protected UserService userService;
    protected StoreService storeService;
    protected AuthService authService;
    protected IPaymentService paymentService;
    protected IShipmentService shipmentService;
    protected PurchaseService purchaseService;
    
    @BeforeEach
    void setup() {
      
        IUserRepository userRepository = new UserRepository();
        IListingRepository listingRepository = new ListingRepository();
        
        authService = new AuthService(userRepository);
        userService = new UserService(userRepository,authService);
        IStoreRepository storerepo = new StoreRepository(); // Use the real implementation

        storeService = new StoreService(storerepo,userRepository,listingRepository); // Use the real implementation
        paymentService = mock(IPaymentService.class); // Mock external service
        shipmentService = mock(IShipmentService.class); // Mock external service
        IPurchaseRepository prep = new PurchaseRepository();

        purchaseService = new PurchaseService(storerepo, prep , userRepository,paymentService,shipmentService);
        // Initialize the bridge with the mocked services
        // This allows the bridge to interact with the mocked services during tests.
        
    }
    /* 1️⃣  register a user and log in – returns the tokens */
    protected AuthService.AuthToken registarAndLogin(String userName) throws Exception {
        userService.register(userName, "pw");
        return authService.login(userName, "pw");
    }


   
  
}
