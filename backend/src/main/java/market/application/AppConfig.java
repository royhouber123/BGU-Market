package market.application;

import market.application.External.IPaymentService;
import market.application.External.IShipmentService;
import org.springframework.beans.factory.annotation.Value;
import market.application.External.ExternalPaymentService;
import market.application.External.ExternalShipmentService;
import market.application.External.PaymentService;
import market.application.External.ShipmentService;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; // ✅ Add this
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary; // ✅ Add this
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
    @ConditionalOnProperty(name = "external.services.payment.type", havingValue = "external", matchIfMissing = false)
    @Primary
    public IPaymentService externalPaymentService(RestTemplate restTemplate,
                                                  @Value("${external.payment.url:https://damp-lynna-wsep-1984852e.koyeb.app/}") String paymentUrl) {
        return new ExternalPaymentService(restTemplate, paymentUrl);
    }

    @Bean
    @ConditionalOnProperty(name = "external.services.payment.type", havingValue = "mock", matchIfMissing = true)
    public IPaymentService mockPaymentService() {
        return new PaymentService();
    }

    @Bean
    @ConditionalOnProperty(name = "external.services.shipment.type", havingValue = "external", matchIfMissing = false)
    @Primary
    public IShipmentService externalShipmentService(RestTemplate restTemplate,
                                                    @Value("${external.shipment.url:https://damp-lynna-wsep-1984852e.koyeb.app/}") String shipmentUrl) {
        return new ExternalShipmentService(restTemplate, shipmentUrl);
    }

    @Bean
    @ConditionalOnProperty(name = "external.services.shipment.type", havingValue = "mock", matchIfMissing = true)
    public IShipmentService mockShipmentService() {
        return new ShipmentService();
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
}