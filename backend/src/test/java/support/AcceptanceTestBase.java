package support;

import org.junit.jupiter.api.BeforeEach;
import static org.mockito.Mockito.mock;

import market.application.AuthService;
import market.application.External.IPaymentService;
import market.application.External.IShipmentService;
import market.application.ProductService;
import market.application.PurchaseService;
import market.application.StoreService;
import market.application.UserService;
import market.domain.Role.IRoleRepository;
import market.domain.purchase.IPurchaseRepository;
import market.domain.store.IListingRepository;
import market.domain.store.IStoreRepository;
import market.domain.user.IUserRepository;
import market.infrastructure.ListingRepository;
import market.infrastructure.PurchaseRepository;
import market.infrastructure.StoreRepository;
import market.infrastructure.UserRepository;

/**
 * Base class for Acceptance tests.
 */
public abstract class AcceptanceTestBase {

    /**
     * User service instance.
     */
    protected UserService userService;
    protected StoreService storeService;
    protected AuthService authService;
    protected ProductService productService; 
    protected IPaymentService paymentService;
    protected IShipmentService shipmentService;
    protected PurchaseService purchaseService;
    protected StoreRepository storeRepository;
    protected IListingRepository listingRepository;
    
    
    @BeforeEach
    void setup() {
      
        IUserRepository userRepository = new UserRepository();
        listingRepository = new ListingRepository();
        IRoleRepository roleRepository = mock(IRoleRepository.class); // Mock role repository
        storeRepository = new StoreRepository();
         
        authService = new AuthService(userRepository);
        userService = new UserService(userRepository, authService);
        IStoreRepository storerepo = new StoreRepository(); // Use the real implementation

        storeService = new StoreService(storerepo,userRepository,listingRepository); // Use the real implementation
        productService = new ProductService(listingRepository);
        paymentService = mock(IPaymentService.class); // Mock external service
        shipmentService = mock(IShipmentService.class); // Mock external service
        IPurchaseRepository prep = new PurchaseRepository();

        purchaseService = new PurchaseService(storerepo, prep , listingRepository, userRepository,paymentService,shipmentService);
        // Initialize the bridge with the mocked services
        // This allows the bridge to interact with the mocked services during tests.
        
    }
    /* 1️⃣  register a user and log in – returns the tokens */
    protected AuthService.AuthToken registarAndLogin(String userName) throws Exception {
        userService.register(userName, "pw");

        return authService.login(userName, "pw").getData(); 
    }  

    // restore after deletion

  
}
