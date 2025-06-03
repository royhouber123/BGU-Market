package market.application;

import market.application.External.IPaymentService;
import market.application.External.IShipmentService;
import market.application.External.PaymentService;
import market.application.External.ShipmentService;
import market.domain.Role.IRoleRepository;
import market.domain.purchase.IPurchaseRepository;
import market.domain.store.IListingRepository;
import market.domain.store.IStoreRepository;
import market.domain.user.ISuspensionRepository;
import market.domain.user.IUserRepository;
import market.infrastructure.ListingRepository;
import market.infrastructure.PurchaseRepository;
import market.infrastructure.RoleRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration class that defines all service beans.
 */
@Configuration
public class AppConfig {

    @Bean
    public IListingRepository listingRepository() {
        return new ListingRepository();
    }

    @Bean
    public IPurchaseRepository purchaseRepository() {
        return new PurchaseRepository();
    }

    @Bean
    public IRoleRepository roleRepository() {
        return new RoleRepository();
    }

    @Bean
    public IPaymentService paymentService() {
        return new PaymentService();
    }

    @Bean
    public IShipmentService shipmentService() {
        return new ShipmentService();
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
                                   ISuspensionRepository suspensionRepository) {
        return new StoreService(storeRepository, userRepository, listingRepository, suspensionRepository);
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
                                         ISuspensionRepository suspensionRepository) {
        return new PurchaseService(storeRepository, purchaseRepository, listingRepository, 
                                 userRepository, paymentService, shipmentService, suspensionRepository);
    }

    @Bean
    public StorePoliciesService storePoliciesService(IStoreRepository storeRepository, ISuspensionRepository suspensionRepository) {
        return new StorePoliciesService(storeRepository, suspensionRepository);
    }

    @Bean
    public AdminService adminService(IUserRepository userRepository,
                                   IStoreRepository storeRepository,
                                   IRoleRepository roleRepository, 
                                   ISuspensionRepository suspensionRepository) {
        return new AdminService(userRepository, storeRepository, roleRepository, suspensionRepository);
    }
} 