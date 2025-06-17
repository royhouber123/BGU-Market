package market.application;

import market.application.External.IPaymentService;
import market.application.External.IShipmentService;
import market.domain.Role.IRoleRepository;
import market.domain.notification.INotificationRepository;
import market.domain.purchase.IPurchaseRepository;
import market.domain.store.IListingRepository;
import market.domain.store.IStoreRepository;
import market.domain.user.ISuspensionRepository;
import market.domain.user.IUserRepository;
import market.infrastructure.RoleRepository;
import market.infrastructure.PersistenceRepositories.UserRepositoryPersistance;
import market.infrastructure.PersistenceRepositories.SuspensionRepositoryPersistance;
import market.infrastructure.StoreRepository;
import market.notification.INotifier;
import market.notification.WebSocketBroadcastNotifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Spring configuration class that defines all service beans.
 */
@Configuration
@EnableJpaRepositories(basePackages = "market.infrastructure.IJpaRepository")
@ComponentScan(basePackages = {"market.infrastructure.PersistenceRepositories", "market.infrastructure", "market.application.External"})
public class AppConfig {

    // @Bean
    // public ISuspensionRepository suspensionRepository(IUserRepository userRepository) {
    //     return new SuspensionRepository(userRepository);
    // }

    @Bean
    public IRoleRepository roleRepository() {
        return new RoleRepository();
    }

    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public AuthService authService(IUserRepository userRepository) {
        return new AuthService(userRepository);
    }

    @Bean
    public UserService userService(IUserRepository userRepository, AuthService authService, ISuspensionRepository suspensionRepository) {
        return new UserService(userRepository, authService, suspensionRepository);
    }

    @Bean
    public StoreService storeService(IStoreRepository storeRepository, 
                                   IUserRepository userRepository, 
                                   IListingRepository listingRepository,
                                   ISuspensionRepository suspensionRepository,
                                   NotificationService notificationService) {
        return new StoreService(storeRepository, userRepository, listingRepository, suspensionRepository, notificationService);
    }

    @Bean
    public ProductService productService(IListingRepository listingRepository) {
        return new ProductService(listingRepository);
    }

    @Bean
    public PurchaseService purchaseService(IStoreRepository storeRepository,
                                         IPurchaseRepository purchaseRepository,
                                         IListingRepository listingRepository,
                                         IUserRepository userRepository,
                                         IPaymentService paymentService,
                                         IShipmentService shipmentService,
                                         ISuspensionRepository suspensionRepository,
                                         NotificationService notificationService) {
        // Add this debugging
        System.out.println("🔍 PurchaseService initialized with:");
        System.out.println("  Payment service: " + paymentService.getClass().getSimpleName());
        System.out.println("  Shipment service: " + shipmentService.getClass().getSimpleName());
        
        return new PurchaseService(storeRepository, purchaseRepository, listingRepository, 
                                 userRepository, paymentService, shipmentService, suspensionRepository, notificationService);
    }

    @Bean
    public StorePoliciesService storePoliciesService(IStoreRepository storeRepository, ISuspensionRepository suspensionRepository) {
        return new StorePoliciesService(storeRepository, suspensionRepository);
    }

    @Bean
    public AdminService adminService(IUserRepository userRepository,
                                   IStoreRepository storeRepository,
                                   IRoleRepository roleRepository, 
                                   ISuspensionRepository suspensionRepository,
                                   NotificationService notificationService) {
        return new AdminService(userRepository, storeRepository, roleRepository, suspensionRepository, notificationService);
    }

    @Bean
    public INotifier notifier(org.springframework.messaging.simp.SimpMessagingTemplate simpMessagingTemplate) {
        return new WebSocketBroadcastNotifier(simpMessagingTemplate);
    }

    @Bean
    public NotificationService notificationService(INotificationRepository notificationRepository,
                                              INotifier notifier) {
        return new NotificationService(notificationRepository, notifier);
    }
}