package support;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;


/**
 * Base class for Spring-based Acceptance tests that use real Spring configuration.
 * Use this when you want to test with the actual AppConfig beans.
 */
@SpringBootTest(classes = market.BguMarketApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public abstract class AcceptanceTestSpringBase {

    // All the services you need, injected from Spring context
    @Autowired
    protected IUserRepository userRepository;

    @Autowired
    protected IStoreRepository storeRepository;

    @Autowired
    protected IListingRepository listingRepository;

    @Autowired
    protected IPurchaseRepository purchaseRepository;

    @Autowired
    protected ISuspensionRepository suspensionRepository;

    @Autowired
    protected IRoleRepository roleRepository;

    @Autowired
    protected INotificationRepository notificationRepository;

    @Autowired
    protected UserService userService;

    @Autowired
    protected StoreService storeService;

    @Autowired
    protected AuthService authService;

    @Autowired
    protected PurchaseService purchaseService;

    @Autowired
    protected StorePoliciesService storePoliciesService;

    @Autowired
    protected NotificationService notificationService;

    @Autowired
    protected IPaymentService paymentService;

    @Autowired
    protected IShipmentService shipmentService;

    @Autowired
    protected ProductService productService;
    
    @BeforeEach
    void setup() {
        // No need to setup anything here
    }

    

    /* 1️⃣  register a user and log in – returns the tokens */
    protected AuthService.AuthToken registarAndLogin(String userName) throws Exception {
        userService.register(userName, "pw");
        return authService.login(userName, "pw"); 
    }  



   
} 