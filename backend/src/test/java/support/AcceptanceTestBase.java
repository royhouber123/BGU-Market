package support;

import org.junit.jupiter.api.BeforeEach;
import static org.mockito.Mockito.mock;

import javax.management.Notification;

import market.application.AuthService;
import market.application.NotificationService;
import market.application.External.IPaymentService;
import market.application.External.IShipmentService;
import market.application.ProductService;
import market.application.PurchaseService;
import market.application.StorePoliciesService;
import market.application.StoreService;
import market.application.UserService;
import market.domain.Role.IRoleRepository;
import market.domain.notification.INotificationRepository;
import market.domain.purchase.IPurchaseRepository;
import market.domain.store.IListingRepository;
import market.domain.store.IStoreRepository;
import market.domain.user.IUserRepository;
import market.domain.user.ISuspensionRepository;
import market.infrastructure.ListingRepository;
import market.infrastructure.ListingRepositoryPersistence;
import market.infrastructure.PurchaseRepository;
import market.infrastructure.StoreRepository;
import market.infrastructure.SuspensionRepository;
import market.infrastructure.UserRepository;
import market.notification.INotifier; // Updated import



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
    protected StorePoliciesService storePoliciesService;
    protected StoreRepository storeRepository;
    protected IListingRepository listingRepository;
    
    
    @BeforeEach
    void setup() {
      
        IUserRepository userRepository = new UserRepository();
        listingRepository = new ListingRepository(); // Use the real implementation
        ISuspensionRepository susRepo = new SuspensionRepository(userRepository);
        IRoleRepository roleRepository = mock(IRoleRepository.class); // Mock role repository
        storeRepository = new StoreRepository();
         
        authService = new AuthService(userRepository);
        userService = new UserService(userRepository, authService, susRepo);
        IStoreRepository storerepo = new StoreRepository(); // Use the real implementation

        INotificationRepository notificationRepository = mock(INotificationRepository.class); // Mock notification repository
        INotifier notifier = mock(INotifier.class);   // Mock notifier interface
        NotificationService notificationService = new NotificationService(notificationRepository, notifier);

        storeService = new StoreService(storerepo, userRepository, listingRepository, susRepo, notificationService); // Use the real implementation
        storePoliciesService = new StorePoliciesService(storerepo,susRepo);
        productService = new ProductService(listingRepository);
        paymentService = mock(IPaymentService.class); // Mock external service
        shipmentService = mock(IShipmentService.class); // Mock external service
        IPurchaseRepository prep = new PurchaseRepository();

        INotificationRepository notificationRepo = mock(INotificationRepository.class); // Mock notification repository
        INotifier notifier2 = mock(INotifier.class); // Mock notifier interface
        NotificationService notificationServiceMock = new NotificationService(notificationRepo, notifier2);

        purchaseService = new PurchaseService(storerepo, prep , listingRepository, userRepository,paymentService,shipmentService,susRepo,notificationServiceMock);
        // Initialize the bridge with the mocked services
        // This allows the bridge to interact with the mocked services during tests.
        
    }
    /* 1️⃣  register a user and log in – returns the tokens */
    protected AuthService.AuthToken registarAndLogin(String userName) throws Exception {
        userService.register(userName, "pw");

        return authService.login(userName, "pw"); 
    }  

    // restore after deletion

  
}
