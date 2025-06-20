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
import market.infrastructure.NotificationRepository;
import market.infrastructure.RoleRepository;
import market.notification.INotifier;
import market.notification.WebSocketBroadcastNotifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Spring configuration class that defines all service beans.
 */
@Configuration
@EnableJpaRepositories(basePackages = "market.infrastructure.IJpaRepository")
public class AppConfig {

  
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
    
    @Bean
    public INotificationRepository notificationRepository() {
        return new NotificationRepository();
    }

}